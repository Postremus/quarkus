package io.quarkus.bootstrap.model;

import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.maven.dependency.GACT;
import io.quarkus.maven.dependency.ResolvedArtifactDependency;
import io.quarkus.maven.dependency.ResolvedDependency;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultApplicationModel implements ApplicationModel, CustomSerDeriable {

    private final ResolvedDependency appArtifact;
    private final List<ResolvedDependency> dependencies;
    private final PlatformImports platformImports;
    private final List<ExtensionCapabilities> capabilityContracts;
    private final Set<ArtifactKey> parentFirstArtifacts;
    private final Set<ArtifactKey> runnerParentFirstArtifacts;
    private final Set<ArtifactKey> lesserPriorityArtifacts;
    private final Set<ArtifactKey> localProjectArtifacts;

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
    public void serialize(OutputStream out) {
        ((ResolvedArtifactDependency) appArtifact).serialize(out);
        CustomSerDerUtil.writeCollection(dependencies, out, (r, o) -> ((ResolvedArtifactDependency) r).serialize(o));
        CustomSerDerUtil.writeObject(platformImports, out);
        CustomSerDerUtil.writeCollection(capabilityContracts, out, CustomSerDerUtil::writeObject);
        CustomSerDerUtil.writeCollection(parentFirstArtifacts, out, (r, o) -> ((GACT) r).serialize(o));
        CustomSerDerUtil.writeCollection(runnerParentFirstArtifacts, out, (r, o) -> ((GACT) r).serialize(o));
        CustomSerDerUtil.writeCollection(lesserPriorityArtifacts, out, (r, o) -> ((GACT) r).serialize(o));
        CustomSerDerUtil.writeCollection(localProjectArtifacts, out, (r, o) -> ((GACT) r).serialize(o));
    }

    public static DefaultApplicationModel deserialize(ByteBuffer buffer) {
        ResolvedDependency appArtifact = ResolvedArtifactDependency.deserialize(buffer);
        List<ResolvedDependency> dependencies = CustomSerDerUtil.readCollection(buffer, ResolvedArtifactDependency::deserialize,
                ArrayList::new);
        PlatformImports platformImports = CustomSerDerUtil.readObject(buffer);
        List<ExtensionCapabilities> capabilityContracts = CustomSerDerUtil.readCollection(buffer, CustomSerDerUtil::readObject,
                ArrayList::new);
        Set<ArtifactKey> parentFirstArtifacts = CustomSerDerUtil.readCollection(buffer, GACT::deserialize,
                HashSet::new);
        Set<ArtifactKey> runnerParentFirstArtifacts = CustomSerDerUtil.readCollection(buffer, GACT::deserialize,
                HashSet::new);
        Set<ArtifactKey> lesserPriorityArtifacts = CustomSerDerUtil.readCollection(buffer, GACT::deserialize,
                HashSet::new);
        Set<ArtifactKey> localProjectArtifacts = CustomSerDerUtil.readCollection(buffer, GACT::deserialize,
                HashSet::new);

        ApplicationModelBuilder builder = new ApplicationModelBuilder();
        builder.appArtifact = appArtifact;
        builder.addDependencies(dependencies);
        builder.platformImports = platformImports;
        for (ExtensionCapabilities capabilityContract : capabilityContracts) {
            builder.addExtensionCapabilities(capabilityContract);
        }
        builder.addParentFirstArtifacts(parentFirstArtifacts);
        builder.addRunnerParentFirstArtifacts(runnerParentFirstArtifacts);
        builder.addLesserPriorityArtifacts(lesserPriorityArtifacts);
        builder.addReloadableWorkspaceModules(localProjectArtifacts);

        return new DefaultApplicationModel(builder);
    }
}
