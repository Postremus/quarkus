package io.quarkus.paths;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public interface PathVisit {

    Path getRoot();

    Path getPath();

    default URL getUrl() {
        try {
            return getPath().toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to translate " + getPath().toUri() + " to " + URL.class.getName(), e);
        }
    }

    Path getRelativePath();

    default String getRelativePath(String separator) {
        return PathUtils.asString(getRelativePath(), separator);
    }

    void stopWalking();
}
