package io.quarkus.paths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Manifest;

class FilePathTree implements OpenPathTree {

    private final Path file;
    private final PathFilter pathFilter;
    private BasicFileAttributes fileAttributes;

    FilePathTree(Path file) {
        this(file, null);
    }

    FilePathTree(Path file, PathFilter pathFilter) {
        this.file = file;
        this.pathFilter = pathFilter;

        try {
            this.fileAttributes = Files.readAttributes(file, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
            this.fileAttributes = null;
        }
    }

    @Override
    public Collection<Path> getRoots() {
        return Collections.singletonList(file);
    }

    @Override
    public Manifest getManifest() {
        return null;
    }

    @Override
    public void walk(PathVisitor visitor) {
        if (pathFilter != null) {
            final String pathStr = file.getFileSystem().getSeparator().equals("/") ? file.toString()
                    : file.toString().replace('\\', '/');
            if (!pathFilter.isVisible(pathStr)) {
                return;
            }
            return;
        }
        visitor.visitPath(new PathVisit() {

            @Override
            public Path getRoot() {
                return file;
            }

            @Override
            public Path getPath() {
                return file;
            }

            @Override
            public void stopWalking() {
            }

            @Override
            public String getRelativePath(String separator) {
                return "";
            }

            @Override
            public BasicFileAttributes getFileAttributes() {
                try {
                    return Files.readAttributes(file, BasicFileAttributes.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public <T> T apply(String relativePath, Function<PathVisit, T> func) {
        if (relativePath.isEmpty()) {
            return PathTreeVisit.process(file, file, file, fileAttributes, pathFilter, func);
        }
        return func.apply(null);
    }

    @Override
    public void accept(String relativePath, Consumer<PathVisit> func) {
        if (relativePath.isEmpty()) {
            PathTreeVisit.consume(file, file, file, fileAttributes, pathFilter, func);
            return;
        }
        func.accept(null);
    }

    @Override
    public boolean contains(String relativePath) {
        return false;
    }

    @Override
    public Path getPath(String relativePath) {
        return null;
    }

    @Override
    public OpenPathTree open() {
        return this;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public PathTree getOriginalTree() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, pathFilter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FilePathTree other = (FilePathTree) obj;
        return Objects.equals(file, other.file) && Objects.equals(pathFilter, other.pathFilter);
    }
}
