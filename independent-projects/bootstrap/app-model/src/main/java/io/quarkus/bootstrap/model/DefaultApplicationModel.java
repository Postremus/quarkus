package io.quarkus.bootstrap.model;

import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.maven.dependency.ResolvedDependency;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DefaultApplicationModel implements ApplicationModel, Serializable {

    private ResolvedDependency appArtifact;
    private List<ResolvedDependency> dependencies;
    private PlatformImports platformImports;
    private List<ExtensionCapabilities> capabilityContracts;
    private Set<ArtifactKey> parentFirstArtifacts;
    private Set<ArtifactKey> runnerParentFirstArtifacts;
    private Set<ArtifactKey> lesserPriorityArtifacts;
    private Set<ArtifactKey> localProjectArtifacts;

    private DefaultApplicationModel() {

    }

    public DefaultApplicationModel(ApplicationModelBuilder builder) {
        this.appArtifact = builder.appArtifact;
        this.dependencies = builder.filter(builder.dependencies.values());
        this.platformImports = builder.platformImports;
        this.capabilityContracts = builder.extensionCapabilities;
        this.parentFirstArtifacts = builder.parentFirstArtifacts;
        this.runnerParentFirstArtifacts = builder.runnerParentFirstArtifacts;
        this.lesserPriorityArtifacts = builder.lesserPriorityArtifacts;
        this.localProjectArtifacts = builder.reloadableWorkspaceModules;
    }

    @Override
    public ResolvedDependency getAppArtifact() {
        return appArtifact;
    }

    @Override
    public Collection<ResolvedDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public PlatformImports getPlatforms() {
        return platformImports;
    }

    @Override
    public Collection<ExtensionCapabilities> getExtensionCapabilities() {
        return capabilityContracts;
    }

    @Override
    public Set<ArtifactKey> getParentFirst() {
        return parentFirstArtifacts;
    }

    @Override
    public Set<ArtifactKey> getRunnerParentFirst() {
        return runnerParentFirstArtifacts;
    }

    @Override
    public Set<ArtifactKey> getLowerPriorityArtifacts() {
        return lesserPriorityArtifacts;
    }

    @Override
    public Set<ArtifactKey> getReloadableWorkspaceDependencies() {
        return localProjectArtifacts;
    }
}
