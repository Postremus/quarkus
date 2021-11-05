package io.quarkus.paths;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import org.jboss.logging.Logger;

public abstract class PathTreeWithManifest implements PathTree {

    private static final String META_INF_VERSIONS = "META-INF/versions/";
    public static final int JAVA_VERSION;

    static {
        int version = 8;
        try {
            Method versionMethod = Runtime.class.getMethod("version");
            Object v = versionMethod.invoke(null);
            List<Integer> list = (List<Integer>) v.getClass().getMethod("version").invoke(v);
            version = list.get(0);
        } catch (Exception e) {
            //version 8
        }
        JAVA_VERSION = version;
    }

    protected volatile transient Manifest manifest;
    protected volatile transient boolean manifestInitialized;
    protected volatile Map<String, String> multiReleaseMapping;

    public PathTreeWithManifest() {
    }

    protected PathTreeWithManifest(Manifest manifest, boolean manifestInitialized, Map<String, String> multiReleaseMapping) {
        this.manifest = manifest;
        this.manifestInitialized = manifestInitialized;
    }

    @Override
    public Manifest getManifest() {
        if (manifestInitialized) {
            return manifest;
        }
        synchronized (this) {
            if (manifestInitialized) {
                return manifest;
            }
            manifest = processPath("META-INF/MANIFEST.MF", ManifestReader.INSTANCE, false);
            manifestInitialized = true;
        }
        return manifest;
    }

    public boolean isMultiReleaseJar() {
        return isMultiReleaseJar(getManifest());
    }

    protected Map<String, String> getMultiReleaseMapping() {
        if (multiReleaseMapping == null) {
            synchronized (this) {
                if (multiReleaseMapping == null) {
                    multiReleaseMapping = isMultiReleaseJar()
                            ? processPath(META_INF_VERSIONS, MultiReleaseMappingReader.INSTANCE, false)
                            : Collections.emptyMap();
                }
            }
        }
        return multiReleaseMapping;
    }

    protected String toMultiReleaseRelativePath(String relativePath) {
        return getMultiReleaseMapping().getOrDefault(relativePath, relativePath);
    }

    private static boolean isMultiReleaseJar(final Manifest m) {
        return m != null && Boolean.parseBoolean(m.getMainAttributes().getValue("Multi-Release"));
    }

    private static class MultiReleaseMappingReader implements Function<PathVisit, Map<String, String>> {

        private static final MultiReleaseMappingReader INSTANCE = new MultiReleaseMappingReader();

        @Override
        public Map<String, String> apply(PathVisit visit) {
            if (visit == null) {
                return Collections.emptyMap();
            }
            final Path versionsDir = visit.getPath();
            if (!Files.isDirectory(versionsDir)) {
                return Collections.emptyMap();
            }
            final Path root = visit.getPath().getRoot();
            final TreeMap<Integer, Consumer<Map<String, String>>> versionContentMap = new TreeMap<>();
            try (Stream<Path> versions = Files.list(versionsDir)) {
                versions.forEach(versionDir -> {
                    if (!Files.isDirectory(versionDir)) {
                        return;
                    }
                    final int version;
                    try {
                        version = Integer.parseInt(versionDir.getFileName().toString());
                        if (version > JAVA_VERSION) {
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Logger.getLogger(PathTreeWithManifest.class)
                                .debug("Failed to parse " + versionDir + " entry", e);
                        return;
                    }
                    versionContentMap.put(version, new Consumer<Map<String, String>>() {
                        @Override
                        public void accept(Map<String, String> map) {
                            try (Stream<Path> versionContent = Files.walk(versionDir)) {
                                versionContent.forEach(p -> {
                                    final String relativePath = versionDir.relativize(p).toString();
                                    if (!relativePath.isEmpty()) {
                                        map.put(relativePath, root.relativize(p).toString());
                                    }
                                });
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                    });

                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            final Map<String, String> multiReleaseMapping = new HashMap<>();
            for (Consumer<Map<String, String>> c : versionContentMap.values()) {
                c.accept(multiReleaseMapping);
            }
            return multiReleaseMapping;
        }
    }

    private static class ManifestReader implements Function<PathVisit, Manifest> {
        private static final ManifestReader INSTANCE = new ManifestReader();

        @Override
        public Manifest apply(PathVisit visit) {
            if (visit == null) {
                return null;
            }
            try (InputStream is = Files.newInputStream(visit.getPath())) {
                return new Manifest(is);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
