package io.quarkus.vertx.http.deployment.webjar;

import java.util.List;
import java.util.Map;

import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.maven.dependency.ResolvedDependency;
import io.quarkus.vertx.http.runtime.devmode.FileSystemStaticHandler;

public final class WebJarResultBuildItem extends SimpleBuildItem {
    private final Map<ArtifactKey, WebJarResult> results;

    public WebJarResultBuildItem(Map<ArtifactKey, WebJarResult> results) {
        this.results = results;
    }

    public WebJarResult byArtifactKey(ArtifactKey artifactKey) {
        return results.get(artifactKey);
    }

    public static class WebJarResult {
        /**
         * Resolved dependency of the webjar
         */
        private final ResolvedDependency dependency;

        /**
         * Path to either the created directory on disk (dev and test), or the same value as
         * {@link WebJarBuildItem#getFinalDestination()} (prod mode)
         */
        private final String finalDestination;

        private final List<FileSystemStaticHandler.StaticWebRootConfiguration> webRootConfigurations;

        private final Map<String, byte[]> files;

        public WebJarResult(ResolvedDependency dependency, String finalDestination,
                List<FileSystemStaticHandler.StaticWebRootConfiguration> webRootConfigurations, Map<String, byte[]> files) {
            this.dependency = dependency;
            this.finalDestination = finalDestination;
            this.webRootConfigurations = webRootConfigurations;
            this.files = files;
        }

        public ResolvedDependency getDependency() {
            return dependency;
        }

        public String getFinalDestination() {
            return finalDestination;
        }

        public List<FileSystemStaticHandler.StaticWebRootConfiguration> getWebRootConfigurations() {
            return webRootConfigurations;
        }

        public Map<String, byte[]> getFiles() {
            return files;
        }
    }
}
