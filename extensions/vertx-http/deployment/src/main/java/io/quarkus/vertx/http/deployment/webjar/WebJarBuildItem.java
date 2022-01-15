package io.quarkus.vertx.http.deployment.webjar;

import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.maven.dependency.GACT;

/**
 * BuildItem for deploying a webjar.
 */
public final class WebJarBuildItem extends MultiBuildItem {
    /**
     * ArtifactKey pointing to the web jar. Has to be one of the applications dependencies.
     */
    private final GACT artifactKey;

    /**
     * Root inside the webJar starting from which resources are unpacked.
     */
    private final String root;

    /**
     * Only copy resources of the webjar which are either user overridden, or contain variables.
     */
    private final boolean onlyCopyNonArtifactFiles;

    /**
     * Defines whether quarkus can override resources of the webjar with quarkus internal files.
     */
    private final boolean useDefaultQuarkusBranding;

    /**
     * Path where the webjar content should be unpacked to. For dev and test mode, the files while be unpacked to a temp
     * directory, with this path as parent. In Prod Mode, the files will be available as generated resources inside this path.
     */
    private final String finalDestination;

    private final WebJarResourcesFilter filter;

    public WebJarBuildItem(GACT artifactKey, String webjarRoot, String finalDestination, WebJarResourcesFilter filter) {
        this(artifactKey, webjarRoot, true, false, finalDestination, filter);
    }

    public WebJarBuildItem(GACT webJarKey, String webjarRoot, boolean useDefaultQuarkusBranding,
            boolean onlyCopyNonArtifactFiles, String finalDestination, WebJarResourcesFilter filter) {
        this.artifactKey = webJarKey;
        this.root = webjarRoot;
        this.useDefaultQuarkusBranding = useDefaultQuarkusBranding;
        this.onlyCopyNonArtifactFiles = onlyCopyNonArtifactFiles;
        this.finalDestination = finalDestination;
        this.filter = filter;
    }

    public GACT getArtifactKey() {
        return artifactKey;
    }

    public String getRoot() {
        return root;
    }

    public boolean getUseDefaultQuarkusBranding() {
        return useDefaultQuarkusBranding;
    }

    public boolean getOnlyCopyNonArtifactFiles() {
        return onlyCopyNonArtifactFiles;
    }

    public String getFinalDestination() {
        return finalDestination;
    }

    public WebJarResourcesFilter getFilter() {
        return filter;
    }
}
