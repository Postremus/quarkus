package io.quarkus.paths;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

class PathTreeVisit implements PathVisit {

    static void walk(Path root, Path rootDir, PathFilter pathFilter, Map<String, String> multiReleaseMapping,
            PathVisitor visitor) {
        final PathTreeVisit visit = new PathTreeVisit(root, rootDir, pathFilter, multiReleaseMapping);

        try {
            Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!visit.setCurrent(dir, attrs)) {
                        return FileVisitResult.CONTINUE;
                    }
                    visitor.visitPath(visit);
                    if (visit.isStopWalking()) {
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!visit.setCurrent(file, attrs)) {
                        return FileVisitResult.CONTINUE;
                    }
                    visitor.visitPath(visit);
                    if (visit.isStopWalking()) {
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        visit.visitMultiReleasePaths(visitor);
    }

    static <T> T process(Path root, Path rootDir, Path path, BasicFileAttributes fileAttributes, PathFilter pathFilter,
            Function<PathVisit, T> func) {
        final PathTreeVisit visit = new PathTreeVisit(root, rootDir, pathFilter, Collections.emptyMap());
        if (visit.setCurrent(path, fileAttributes)) {
            return func.apply(visit);
        }
        return func.apply(null);
    }

    static void consume(Path root, Path rootDir, Path path, BasicFileAttributes fileAttributes, PathFilter pathFilter,
            Consumer<PathVisit> func) {
        final PathTreeVisit visit = new PathTreeVisit(root, rootDir, pathFilter, Collections.emptyMap());
        if (visit.setCurrent(path, fileAttributes)) {
            func.accept(visit);
        } else {
            func.accept(null);
        }
    }

    private final Path root;
    private final Path baseDir;
    private final PathFilter pathFilter;
    private final Map<String, String> multiReleaseMapping;

    private Path current;
    private volatile BasicFileAttributes currentFileAttributes;
    private String relativePath;
    private boolean stopWalking;

    private PathTreeVisit(Path root, Path rootDir, PathFilter pathFilter, Map<String, String> multiReleaseMapping) {
        this.root = root;
        this.baseDir = rootDir;
        this.pathFilter = pathFilter;
        this.multiReleaseMapping = multiReleaseMapping == null || multiReleaseMapping.isEmpty() ? Collections.emptyMap()
                : new HashMap<>(multiReleaseMapping);
    }

    @Override
    public Path getRoot() {
        return root;
    }

    @Override
    public Path getPath() {
        return current;
    }

    @Override
    public BasicFileAttributes getFileAttributes() {
        if (currentFileAttributes == null) {
            try {
                currentFileAttributes = Files.readAttributes(current, BasicFileAttributes.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return currentFileAttributes;
    }

    @Override
    public void stopWalking() {
        stopWalking = true;
    }

    boolean isStopWalking() {
        return stopWalking;
    }

    @Override
    public String getRelativePath(String separator) {
        if (relativePath == null) {
            return PathUtils.asString(baseDir.relativize(current), separator);
        }
        if (!current.getFileSystem().getSeparator().equals(separator)) {
            return relativePath.replace(current.getFileSystem().getSeparator(), separator);
        }
        return relativePath;
    }

    private boolean setCurrent(Path path, BasicFileAttributes fileAttributes) {
        current = path;
        this.currentFileAttributes = fileAttributes;
        relativePath = null;
        if (pathFilter != null) {
            relativePath = baseDir.relativize(path).toString();
            if (!PathFilter.isVisible(pathFilter, relativePath)) {
                return false;
            }
        }
        if (!multiReleaseMapping.isEmpty()) {
            if (relativePath == null) {
                relativePath = baseDir.relativize(path).toString();
            }
            final String mrPath = multiReleaseMapping.remove(relativePath);
            if (mrPath != null) {
                current = baseDir.resolve(mrPath);
            }
        }
        return true;
    }

    private void visitMultiReleasePaths(PathVisitor visitor) {
        for (Map.Entry<String, String> mrEntry : multiReleaseMapping.entrySet()) {
            relativePath = mrEntry.getKey();
            if (pathFilter != null && !PathFilter.isVisible(pathFilter, relativePath)) {
                continue;
            }
            current = baseDir.resolve(mrEntry.getValue());
            visitor.visitPath(this);
        }
    }
}
