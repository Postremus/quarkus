package io.quarkus.bootstrap.model;

import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.maven.dependency.ResolvedDependency;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DefaultApplicationModel implements ApplicationModel, Externalizable, Serializable {

    private static final long serialVersionUID = -1707496684727083628L;

    private ResolvedDependency appArtifact;
    private List<ResolvedDependency> dependencies;
    private PlatformImports platformImports;
    private List<ExtensionCapabilities> capabilityContracts;
    private Set<ArtifactKey> parentFirstArtifacts;
    private Set<ArtifactKey> runnerParentFirstArtifacts;
    private Set<ArtifactKey> lesserPriorityArtifacts;
    private Set<ArtifactKey> localProjectArtifacts;

    public DefaultApplicationModel() {

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

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            appArtifact = (ResolvedDependency) in.readObject();
        }
        if (in.readBoolean()) {
            dependencies = (List<ResolvedDependency>) in.readObject();
        }
        if (in.readBoolean()) {
            platformImports = (PlatformImports) in.readObject();
        }
        if (in.readBoolean()) {
            capabilityContracts = (List<ExtensionCapabilities>) in.readObject();
        }
        if (in.readBoolean()) {
            parentFirstArtifacts = (Set<ArtifactKey>) in.readObject();
        }
        if (in.readBoolean()) {
            runnerParentFirstArtifacts = (Set<ArtifactKey>) in.readObject();
        }
        if (in.readBoolean()) {
            lesserPriorityArtifacts = (Set<ArtifactKey>) in.readObject();
        }
        if (in.readBoolean()) {
            localProjectArtifacts = (Set<ArtifactKey>) in.readObject();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (appArtifact == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(appArtifact);
        }
        if (dependencies == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(dependencies);
        }
        if (platformImports == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(platformImports);
        }
        if (capabilityContracts == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(capabilityContracts);
        }
        if (parentFirstArtifacts == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(parentFirstArtifacts);
        }
        if (runnerParentFirstArtifacts == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(runnerParentFirstArtifacts);
        }
        if (lesserPriorityArtifacts == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(lesserPriorityArtifacts);
        }
        if (localProjectArtifacts == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(localProjectArtifacts);
        }
    }
}
