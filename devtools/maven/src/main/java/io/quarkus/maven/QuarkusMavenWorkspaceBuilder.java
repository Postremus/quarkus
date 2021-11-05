package io.quarkus.maven;

import java.io.File;

import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;

import io.quarkus.bootstrap.model.ApplicationModelBuilder;
import io.quarkus.bootstrap.workspace.DefaultArtifactSources;
import io.quarkus.bootstrap.workspace.DefaultSourceDir;
import io.quarkus.bootstrap.workspace.DefaultWorkspaceModule;
import io.quarkus.bootstrap.workspace.WorkspaceModule;
import io.quarkus.bootstrap.workspace.WorkspaceModuleId;
import io.quarkus.maven.dependency.GAV;
import io.quarkus.paths.PathList;

class QuarkusMavenWorkspaceBuilder {

    static void loadModules(MavenProject project, ApplicationModelBuilder modelBuilder) {
    }

    static WorkspaceModule toProjectModule(MavenProject project) {
        final Build build = project.getBuild();
        final DefaultWorkspaceModule module = new DefaultWorkspaceModule(getId(project), project.getBasedir(),
                new File(build.getDirectory()));

        final DefaultArtifactSources sourceSet = new DefaultArtifactSources(DefaultWorkspaceModule.MAIN);
        final File classesDir = new File(build.getOutputDirectory());
        project.getCompileSourceRoots()
                .forEach(s -> sourceSet.addSources(new DefaultSourceDir(new File(s), classesDir)));
        for (Resource r : build.getResources()) {
            sourceSet.addResources(new DefaultSourceDir(new File(r.getDirectory()),
                    r.getTargetPath() == null ? classesDir : new File(r.getTargetPath())));
        }
        module.addArtifactSources(sourceSet);

        final DefaultArtifactSources testSourceSet = new DefaultArtifactSources(DefaultWorkspaceModule.TEST);
        final File testClassesDir = new File(build.getTestOutputDirectory());
        project.getTestCompileSourceRoots()
                .forEach(s -> testSourceSet.addSources(new DefaultSourceDir(new File(s), testClassesDir)));
        for (Resource r : build.getTestResources()) {
            testSourceSet.addResources(new DefaultSourceDir(new File(r.getDirectory()),
                    r.getTargetPath() == null ? testClassesDir : new File(r.getTargetPath())));
        }
        module.addArtifactSources(testSourceSet);

        module.setBuildFiles(PathList.of(project.getFile().toPath()));

        return module;
    }

    private static WorkspaceModuleId getId(MavenProject project) {
        return new GAV(project.getGroupId(), project.getArtifactId(), project.getVersion());
    }
}
