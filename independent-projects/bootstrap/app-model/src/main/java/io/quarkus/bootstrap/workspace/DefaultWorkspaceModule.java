package io.quarkus.bootstrap.workspace;

import io.quarkus.bootstrap.model.CustomSerDerUtil;
import io.quarkus.bootstrap.model.CustomSerDeriable;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.maven.dependency.GAV;
import io.quarkus.paths.PathCollection;
import io.quarkus.paths.PathList;
import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultWorkspaceModule implements WorkspaceModule, Serializable, CustomSerDeriable {

    public static final String MAIN = "";
    public static final String TEST = "tests";

    private final WorkspaceModuleId id;
    private final File moduleDir;
    private final File buildDir;
    private PathCollection buildFiles;
    private final Map<String, ArtifactSources> sourcesSets = new HashMap<>();
    private List<Dependency> directDepConstraints = Collections.emptyList();
    private List<Dependency> directDeps = Collections.emptyList();

    public DefaultWorkspaceModule(WorkspaceModuleId id, File moduleDir, File buildDir) {
        super();
        this.id = id;
        this.moduleDir = moduleDir;
        this.buildDir = buildDir;
    }

    @Override
    public WorkspaceModuleId getId() {
        return id;
    }

    @Override
    public File getModuleDir() {
        return moduleDir;
    }

    @Override
    public File getBuildDir() {
        return buildDir;
    }

    public void addArtifactSources(ArtifactSources src) {
        sourcesSets.put(src.getClassifier(), src);
    }

    @Override
    public boolean hasSources(String classifier) {
        return sourcesSets.containsKey(classifier);
    }

    @Override
    public ArtifactSources getSources(String name) {
        return sourcesSets.get(name);
    }

    @Override
    public Collection<String> getSourceClassifiers() {
        return sourcesSets.keySet();
    }

    public void setBuildFiles(PathCollection buildFiles) {
        this.buildFiles = buildFiles;
    }

    @Override
    public PathCollection getBuildFiles() {
        return buildFiles == null ? PathList.empty() : buildFiles;
    }

    @Override
    public Collection<Dependency> getDirectDependencyConstraints() {
        return directDepConstraints;
    }

    public void setDirectDependencyConstraints(List<Dependency> directDepConstraints) {
        this.directDepConstraints = directDepConstraints;
    }

    @Override
    public Collection<Dependency> getDirectDependencies() {
        return directDeps;
    }

    public void setDirectDependencies(List<Dependency> directDeps) {
        this.directDeps = directDeps;
    }

    public void addDirectDependency(Dependency directDep) {
        if (directDeps.isEmpty()) {
            directDeps = new ArrayList<>();
        }
        this.directDeps.add(directDep);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(id);
        buf.append(" ").append(moduleDir);
        sourcesSets.values().forEach(a -> {
            buf.append(" ").append(a);
        });
        return buf.toString();
    }

    @Override
    public void serialize(OutputStream out) {
        ((GAV) id).serialize(out);
        CustomSerDerUtil.writeFile(moduleDir, out);
        CustomSerDerUtil.writeFile(buildDir, out);
        CustomSerDerUtil.writePathCollection(buildFiles, out);
        CustomSerDerUtil.writeCollection(sourcesSets.values(), out, (r, o) -> ((DefaultArtifactSources) r).serialize(o));
        CustomSerDerUtil.writeCollection(directDepConstraints, out, CustomSerDerUtil::writeObject);
        CustomSerDerUtil.writeCollection(directDeps, out, CustomSerDerUtil::writeObject);
    }

    public static DefaultWorkspaceModule deserialize(ByteBuffer buffer) {
        GAV id = GAV.deserialize(buffer);
        File moduleDir = CustomSerDerUtil.readFile(buffer);
        File buildDir = CustomSerDerUtil.readFile(buffer);

        DefaultWorkspaceModule module = new DefaultWorkspaceModule(id, moduleDir, buildDir);
        module.setBuildFiles(CustomSerDerUtil.readPathCollection(buffer));
        List<ArtifactSources> sources = CustomSerDerUtil.readCollection(buffer, DefaultArtifactSources::deserialize,
                ArrayList::new);
        for (ArtifactSources source : sources) {
            module.addArtifactSources(source);
        }

        module.directDepConstraints = CustomSerDerUtil.readCollection(buffer, CustomSerDerUtil::readObject, ArrayList::new);
        module.directDeps = CustomSerDerUtil.readCollection(buffer, CustomSerDerUtil::readObject, ArrayList::new);
        return module;
    }
}
