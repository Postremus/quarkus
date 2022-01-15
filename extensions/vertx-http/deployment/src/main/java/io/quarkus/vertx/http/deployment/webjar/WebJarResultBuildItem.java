package io.quarkus.vertx.http.deployment.webjar;

import java.nio.file.Path;
import java.util.Map;

import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.maven.dependency.ResolvedDependency;

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
        private ResolvedDependency dependency;

        /**
         * Path to either the created directory on disk (dev and test), or the same value as
         * {@link WebJarBuildItem#getFinalDestination()} (prod mode)
         */
        private String finalDestination;

        public WebJarResult(ResolvedDependency dependency, String finalDestination) {
            this.dependency = dependency;
            this.finalDestination = finalDestination;
        }

        public ResolvedDependency getDependency() {
            return dependency;
        }

        public String getFinalDestination() {
            return finalDestination;
        }
    }
}
