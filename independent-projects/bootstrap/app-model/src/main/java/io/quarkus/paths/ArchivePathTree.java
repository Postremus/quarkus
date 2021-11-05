package io.quarkus.paths;

import io.quarkus.fs.util.ZipUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.jar.Manifest;

public class ArchivePathTree extends PathTreeWithManifest implements PathTree {

    private final Path archive;
    private final PathFilter pathFilter;

    public ArchivePathTree(Path archive) {
        this(archive, null);
    }

    ArchivePathTree(Path archive, PathFilter pathFilter) {
        this.archive = archive;
        this.pathFilter = pathFilter;
    }

    @Override
    public Collection<Path> getRoots() {
        return Collections.singletonList(archive);
    }

    @Override
    public URL getUrl(String name) {
        if (!PathFilter.isVisible(pathFilter, name)) {
            return null;
        }
        try (FileSystem fs = openFs()) {
            final Path path = fs.getPath(toMultiReleaseRelativePath(name));
            if (!Files.exists(path)) {
                return null;
            }
            try {
                return path.toUri().toURL();
            } catch (MalformedURLException e) {
                throw new UncheckedIOException("Failed to translate " + path + " to URL", e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + archive, e);
        }
    }

    @Override
    public void walk(PathVisitor visitor) {
        try (FileSystem fs = openFs()) {
            final Path dir = fs.getPath("/");
            PathTreeVisit.walk(archive, dir, pathFilter, getMultiReleaseMapping(), visitor);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + archive, e);
        }
    }

    @Override
    public <T> T processPath(String relativePath, Function<PathVisit, T> func, boolean multiReleaseSupport) {
        if (!PathFilter.isVisible(pathFilter, relativePath)) {
            return func.apply(null);
        }
        if (multiReleaseSupport) {
            relativePath = toMultiReleaseRelativePath(relativePath);
        }
        try (FileSystem fs = openFs()) {
            for (Path root : fs.getRootDirectories()) {
                final Path path = root.resolve(relativePath);
                if (!Files.exists(path)) {
                    continue;
                }
                return PathTreeVisit.processPath(archive, root, path, pathFilter, func);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + archive, e);
        }
        return func.apply(null);
    }

    private FileSystem openFs() throws IOException {
        return ZipUtils.newFileSystem(archive);
    }

    @Override
    public OpenPathTree openTree() {
        final FileSystem fs;
        try {
            fs = openFs();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        synchronized (this) {
            return new DirectoryPathTree(fs.getPath("/"), pathFilter, manifest, manifestInitialized, multiReleaseMapping) {

                @Override
                public Manifest getManifest() {
                    final Manifest m = super.getManifest();
                    if (!ArchivePathTree.this.manifestInitialized) {
                        synchronized (ArchivePathTree.this) {
                            if (!ArchivePathTree.this.manifestInitialized) {
                                ArchivePathTree.this.manifest = m;
                                ArchivePathTree.this.manifestInitialized = true;
                            }
                        }
                    }
                    return m;
                }

                @Override
                public Map<String, String> getMultiReleaseMapping() {
                    final Map<String, String> mrp = super.getMultiReleaseMapping();
                    if (ArchivePathTree.this.multiReleaseMapping == null) {
                        synchronized (ArchivePathTree.this) {
                            if (ArchivePathTree.this.multiReleaseMapping == null) {
                                ArchivePathTree.this.multiReleaseMapping = mrp;
                            }
                        }
                    }
                    return mrp;
                }

                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        fs.close();
                    }
                }

                @Override
                public PathTree getOriginalTree() {
                    return ArchivePathTree.this;
                }
            }.openTree();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(archive, pathFilter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ArchivePathTree other = (ArchivePathTree) obj;
        return Objects.equals(archive, other.archive) && Objects.equals(pathFilter, other.pathFilter);
    }
}
