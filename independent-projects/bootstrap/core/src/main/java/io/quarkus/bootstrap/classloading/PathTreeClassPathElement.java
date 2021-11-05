package io.quarkus.bootstrap.classloading;

import io.quarkus.bootstrap.classloading.JarClassPathElement.ZipFileMayHaveChangedException;
import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.paths.OpenPathTree;
import io.quarkus.paths.PathTree;
import io.quarkus.paths.PathVisit;
import io.quarkus.paths.PathVisitor;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.jar.Manifest;
import org.jboss.logging.Logger;

public class PathTreeClassPathElement extends AbstractClassPathElement {

    static class ZipFileMayHaveChangedException extends RuntimeException {
        public ZipFileMayHaveChangedException(Throwable cause) {
            super(cause);
        }
    }

    static {
        //force this class to be loaded
        //if quarkus is recompiled it needs to have already
        //been loaded
        //this is just a convenience for quarkus devs that means exit
        //should work properly if you recompile while quarkus is running
        new ZipFileMayHaveChangedException(null);
    }

    private static final Logger log = Logger.getLogger(PathTreeClassPathElement.class);

    private final Lock readLock;
    private final Lock writeLock;
    private final OpenPathTree pathTree;
    private final boolean runtime;
    private final ArtifactKey dependencyKey;
    private volatile boolean closed;
    private volatile Set<String> resources;

    public PathTreeClassPathElement(PathTree pathTree, boolean runtime) {
        this(pathTree, runtime, null);
    }

    public PathTreeClassPathElement(PathTree pathTree, boolean runtime, ArtifactKey dependencyKey) {
        this.pathTree = Objects.requireNonNull(pathTree, "Path tree is null").openTree();
        final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
        this.runtime = runtime;
        this.dependencyKey = dependencyKey;
    }

    @Override
    public boolean isRuntime() {
        return runtime;
    }

    @Override
    public ArtifactKey getDependencyKey() {
        return dependencyKey;
    }

    @Override
    public Path getRoot() {
        return pathTree.getOriginalTree().getRoots().iterator().next();
    }

    @Override
    public ClassPathResource getResource(String name) {
        if (!getProvidedResources().contains(name)) {
            return null;
        }
        return withOpenTree(tree -> tree.processPath(name, new ClassPathResourceProducer()));
    }

    @Override
    public <T> T withOpenTree(Function<OpenPathTree, T> func) {
        readLock.lock();
        try {
            if (closed) {
                //we still need this to work if it is closed, so shutdown hooks work
                //once it is closed it simply does not hold on to any resources
                try (OpenPathTree openTree = pathTree.getOriginalTree().openTree()) {
                    return func.apply(openTree);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                return func.apply(pathTree);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<String> getProvidedResources() {
        if (resources == null) {
            synchronized (this) {
                if (resources != null) {
                    return resources;
                }
                this.resources = withOpenTree(tree -> {
                    final Set<String> resources = new HashSet<>();
                    tree.walk(new PathVisitor() {
                        @Override
                        public void visitPath(PathVisit visit) {
                            final String relativePath = visit.getRelativePath("/");
                            if (relativePath.isEmpty()) {
                                return;
                            }
                            resources.add(relativePath);
                        }
                    });
                    return resources;
                });
            }
        }
        return resources;
    }

    @Override
    protected Manifest readManifest() {
        return withOpenTree(tree -> {
            return tree.getManifest();
        });
    }

    @Override
    public ProtectionDomain getProtectionDomain(ClassLoader classLoader) {
        URL url = null;
        final Path root = getRoot();
        try {
            url = root.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to create protection domain for " + root, e);
        }
        CodeSource codesource = new CodeSource(url, (Certificate[]) null);
        ProtectionDomain protectionDomain = new ProtectionDomain(codesource, null, classLoader, null);
        return protectionDomain;
    }

    @Override
    public void close() throws IOException {
        writeLock.lock();
        resources = null;
        try {
            pathTree.close();
            closed = true;
        } finally {
            writeLock.unlock();
        }
    }

    private class ClassPathResourceProducer implements Function<PathVisit, ClassPathResource> {
        @Override
        public ClassPathResource apply(PathVisit visit) {
            if (visit == null) {
                return null;
            }
            return new ClassPathResource() {

                final String name = visit.getRelativePath("/");
                final Path path = visit.getPath();
                volatile URL url;

                @Override
                public ClassPathElement getContainingElement() {
                    return PathTreeClassPathElement.this;
                }

                @Override
                public String getPath() {
                    return name;
                }

                @Override
                public URL getUrl() {
                    if (url != null) {
                        return url;
                    }
                    if (closed) {
                        return url = withOpenTree(
                                tree -> tree.processPath(name, visit -> visit == null ? null : visit.getUrl()));
                    }
                    try {
                        return url = path.toUri().toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Failed to translate " + path + " to URL", e);
                    }
                }

                @Override
                public byte[] getData() {
                    if (closed) {
                        return withOpenTree(tree -> tree.processPath(name, new ResourcePathReader()));
                    }
                    return readPath(path);
                }

                @Override
                public boolean isDirectory() {
                    if (closed) {
                        return withOpenTree(tree -> tree.processPath(name,
                                visit -> visit == null ? null : Files.isDirectory(visit.getPath())));
                    }
                    return Files.isDirectory(path);
                }
            };
        }
    }

    private class ResourcePathReader implements Function<PathVisit, byte[]> {
        @Override
        public byte[] apply(PathVisit visit) {
            if (visit == null) {
                return null;
            }
            return readPath(visit.getPath());
        }
    }

    private byte[] readPath(Path path) {
        try {
            try {
                return Files.readAllBytes(path);
            } catch (InterruptedIOException e) {
                // if we are interrupted reading data we finish the op, then just
                // re-interrupt the thread state
                final byte[] bytes = Files.readAllBytes(path);
                Thread.currentThread().interrupt();
                return bytes;
            }
        } catch (IOException e) {
            if (!closed) {
                throw new ZipFileMayHaveChangedException(e);
            }
            throw new RuntimeException("Unable to read " + path.toUri(), e);
        }
    }
}
