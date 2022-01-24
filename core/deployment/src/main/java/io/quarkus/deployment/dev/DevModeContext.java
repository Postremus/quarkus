package io.quarkus.deployment.dev;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.paths.PathCollection;
import io.quarkus.paths.PathList;

/**
 * Object that is used to pass context data from the plugin doing the invocation
 * into the dev mode process using java serialization.
 *
 * There is no need to worry about compat as both sides will always be using the same version
 */
public class DevModeContext implements Externalizable {

    public static final CompilationUnit EMPTY_COMPILATION_UNIT = new CompilationUnit(PathList.of(), null, null, null);

    public static final String ENABLE_PREVIEW_FLAG = "--enable-preview";

    private static final long serialVersionUID = 9020101669614103954L;

    private ModuleInfo applicationRoot;
    private List<ModuleInfo> additionalModules = new ArrayList<>();
    private Map<String, String> systemProperties = new HashMap<>();
    private Map<String, String> buildSystemProperties = new HashMap<>();
    private String sourceEncoding;

    private List<URL> additionalClassPathElements = new ArrayList<>();
    private File cacheDir;
    private File projectDir;
    private boolean test;
    private boolean abortOnFailedStart;
    // the jar file which is used to launch the DevModeMain
    private File devModeRunnerJarFile;
    private boolean localProjectDiscovery = true;
    // args of the main-method
    private String[] args;

    private List<String> compilerOptions;
    private String releaseJavaVersion;
    private String sourceJavaVersion;
    private String targetJvmVersion;

    private List<String> compilerPluginArtifacts;
    private List<String> compilerPluginsOptions;

    private String alternateEntryPoint;
    private QuarkusBootstrap.Mode mode = QuarkusBootstrap.Mode.DEV;
    private String baseName;
    private Set<ArtifactKey> localArtifacts = new HashSet<>();

    public DevModeContext() {
    }

    public boolean isLocalProjectDiscovery() {
        return localProjectDiscovery;
    }

    public DevModeContext setLocalProjectDiscovery(boolean localProjectDiscovery) {
        this.localProjectDiscovery = localProjectDiscovery;
        return this;
    }

    public String getAlternateEntryPoint() {
        return alternateEntryPoint;
    }

    public DevModeContext setAlternateEntryPoint(String alternateEntryPoint) {
        this.alternateEntryPoint = alternateEntryPoint;
        return this;
    }

    public ModuleInfo getApplicationRoot() {
        return applicationRoot;
    }

    public DevModeContext setApplicationRoot(ModuleInfo applicationRoot) {
        this.applicationRoot = applicationRoot;
        return this;
    }

    public List<ModuleInfo> getAdditionalModules() {
        return additionalModules;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public Map<String, String> getBuildSystemProperties() {
        return buildSystemProperties;
    }

    public String getSourceEncoding() {
        return sourceEncoding;
    }

    public void setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    public List<URL> getAdditionalClassPathElements() {
        return additionalClassPathElements;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public boolean isAbortOnFailedStart() {
        return abortOnFailedStart;
    }

    public void setAbortOnFailedStart(boolean abortOnFailedStart) {
        this.abortOnFailedStart = abortOnFailedStart;
    }

    public List<String> getCompilerOptions() {
        return compilerOptions;
    }

    public void setCompilerOptions(List<String> compilerOptions) {
        this.compilerOptions = compilerOptions;
    }

    public String getReleaseJavaVersion() {
        return releaseJavaVersion;
    }

    public void setReleaseJavaVersion(String releaseJavaVersion) {
        this.releaseJavaVersion = releaseJavaVersion;
    }

    public String getSourceJavaVersion() {
        return sourceJavaVersion;
    }

    public void setSourceJavaVersion(String sourceJavaVersion) {
        this.sourceJavaVersion = sourceJavaVersion;
    }

    public String getTargetJvmVersion() {
        return targetJvmVersion;
    }

    public void setTargetJvmVersion(String targetJvmVersion) {
        this.targetJvmVersion = targetJvmVersion;
    }

    public List<String> getCompilerPluginArtifacts() {
        return compilerPluginArtifacts;
    }

    public void setCompilerPluginArtifacts(List<String> compilerPluginArtifacts) {
        this.compilerPluginArtifacts = compilerPluginArtifacts;
    }

    public List<String> getCompilerPluginsOptions() {
        return compilerPluginsOptions;
    }

    public void setCompilerPluginsOptions(List<String> compilerPluginsOptions) {
        this.compilerPluginsOptions = compilerPluginsOptions;
    }

    public File getDevModeRunnerJarFile() {
        return devModeRunnerJarFile;
    }

    public void setDevModeRunnerJarFile(final File devModeRunnerJarFile) {
        this.devModeRunnerJarFile = devModeRunnerJarFile;
    }

    public File getProjectDir() {
        return projectDir;
    }

    public DevModeContext setProjectDir(File projectDir) {
        this.projectDir = projectDir;
        return this;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public List<ModuleInfo> getAllModules() {
        List<ModuleInfo> ret = new ArrayList<>();
        ret.add(applicationRoot);
        ret.addAll(additionalModules);
        return ret;
    }

    public QuarkusBootstrap.Mode getMode() {
        return mode;
    }

    public void setMode(QuarkusBootstrap.Mode mode) {
        this.mode = mode;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public Set<ArtifactKey> getLocalArtifacts() {
        return localArtifacts;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            applicationRoot = (ModuleInfo) in.readObject();
        }
        if (in.readBoolean()) {
            additionalModules = (List<ModuleInfo>) in.readObject();
        }
        if (in.readBoolean()) {
            systemProperties = (Map<String, String>) in.readObject();
        }
        if (in.readBoolean()) {
            buildSystemProperties = (Map<String, String>) in.readObject();
        }
        if (in.readBoolean()) {
            sourceEncoding = in.readUTF();
        }
        if (in.readBoolean()) {
            additionalClassPathElements = (List<URL>) in.readObject();
        }
        if (in.readBoolean()) {
            cacheDir = (File) in.readObject();
        }
        if (in.readBoolean()) {
            projectDir = (File) in.readObject();
        }
        test = in.readBoolean();
        abortOnFailedStart = in.readBoolean();
        if (in.readBoolean()) {
            devModeRunnerJarFile = (File) in.readObject();
        }
        localProjectDiscovery = in.readBoolean();
        if (in.readBoolean()) {
            args = (String[]) in.readObject();
        }
        if (in.readBoolean()) {
            compilerOptions = (List<String>) in.readObject();
        }
        if (in.readBoolean()) {
            releaseJavaVersion = in.readUTF();
        }
        if (in.readBoolean()) {
            sourceJavaVersion = in.readUTF();
        }
        if (in.readBoolean()) {
            targetJvmVersion = in.readUTF();
        }
        if (in.readBoolean()) {
            compilerPluginArtifacts = (List<String>) in.readObject();
        }
        if (in.readBoolean()) {
            compilerPluginsOptions = (List<String>) in.readObject();
        }
        if (in.readBoolean()) {
            alternateEntryPoint = in.readUTF();
        }
        if (in.readBoolean()) {
            mode = QuarkusBootstrap.Mode.values()[in.readShort()];
        }
        if (in.readBoolean()) {
            baseName = in.readUTF();
        }
        if (in.readBoolean()) {
            localArtifacts = (Set<ArtifactKey>) in.readObject();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (applicationRoot == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(applicationRoot);
        }
        if (additionalModules == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(additionalModules);
        }
        if (systemProperties == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(systemProperties);
        }
        if (buildSystemProperties == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(buildSystemProperties);
        }
        if (sourceEncoding == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(sourceEncoding);
        }
        if (additionalClassPathElements == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(additionalClassPathElements);
        }
        if (cacheDir == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(cacheDir);
        }
        if (projectDir == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(projectDir);
        }
        out.writeBoolean(test);
        out.writeBoolean(abortOnFailedStart);
        if (devModeRunnerJarFile == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(devModeRunnerJarFile);
        }
        out.writeBoolean(localProjectDiscovery);
        if (args == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(args);
        }
        if (compilerOptions == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(compilerOptions);
        }
        if (releaseJavaVersion == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(releaseJavaVersion);
        }
        if (sourceJavaVersion == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(sourceJavaVersion);
        }
        if (targetJvmVersion == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(targetJvmVersion);
        }
        if (compilerPluginArtifacts == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(compilerPluginArtifacts);
        }
        if (compilerPluginsOptions == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(compilerPluginsOptions);
        }
        if (alternateEntryPoint == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(alternateEntryPoint);
        }
        if (mode == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeShort((short) mode.ordinal());
        }
        if (baseName == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(baseName);
        }
        if (localArtifacts == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeObject(localArtifacts);
        }
    }

    public static class ModuleInfo implements Externalizable, Serializable {

        private static final long serialVersionUID = 7811355178873828181L;

        private ArtifactKey appArtifactKey;
        private String name;
        private String projectDirectory;
        private CompilationUnit main;
        private CompilationUnit test;

        private String preBuildOutputDir;
        private PathCollection sourceParents;
        private String targetDir;

        ModuleInfo(Builder builder) {
            this.appArtifactKey = builder.appArtifactKey;
            this.name = builder.name == null ? builder.appArtifactKey.toGacString() : builder.name;
            this.projectDirectory = builder.projectDirectory;
            this.main = new CompilationUnit(builder.sourcePaths, builder.classesPath,
                    builder.resourcePaths,
                    builder.resourcesOutputPath);

            if (builder.testClassesPath != null) {
                this.test = new CompilationUnit(builder.testSourcePaths,
                        builder.testClassesPath, builder.testResourcePaths, builder.testResourcesOutputPath);
            } else {
                this.test = null;
            }
            this.sourceParents = builder.sourceParents;
            this.preBuildOutputDir = builder.preBuildOutputDir;
            this.targetDir = builder.targetDir;
        }

        public ModuleInfo() {
        }

        public String getName() {
            return name;
        }

        public String getProjectDirectory() {
            return projectDirectory;
        }

        public PathCollection getSourceParents() {
            return sourceParents;
        }

        //TODO: why isn't this immutable?
        public void addSourcePaths(Collection<String> additionalPaths) {
            this.main.sourcePaths = this.main.sourcePaths.add(
                    additionalPaths.stream()
                            .map(p -> Paths.get(p).isAbsolute() ? p : (projectDirectory + File.separator + p))
                            .map(Paths::get)
                            .toArray(Path[]::new));
        }

        public String getPreBuildOutputDir() {
            return preBuildOutputDir;
        }

        public String getTargetDir() {
            return targetDir;
        }

        public ArtifactKey getArtifactKey() {
            return appArtifactKey;
        }

        public CompilationUnit getMain() {
            return main;
        }

        public Optional<CompilationUnit> getTest() {
            return Optional.ofNullable(test);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            if (in.readBoolean()) {
                appArtifactKey = (ArtifactKey) in.readObject();
            }
            if (in.readBoolean()) {
                name = in.readUTF();
            }
            if (in.readBoolean()) {
                projectDirectory = in.readUTF();
            }
            if (in.readBoolean()) {
                main = (CompilationUnit) in.readObject();
            }
            if (in.readBoolean()) {
                test = (CompilationUnit) in.readObject();
            }
            if (in.readBoolean()) {
                preBuildOutputDir = in.readUTF();
            }
            if (in.readBoolean()) {
                sourceParents = (PathCollection) in.readObject();
            }
            if (in.readBoolean()) {
                targetDir = in.readUTF();
            }
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            if (appArtifactKey == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeObject(appArtifactKey);
            }
            if (name == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeUTF(name);
            }
            if (projectDirectory == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeUTF(projectDirectory);
            }
            if (main == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeObject(main);
            }
            if (test == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeObject(test);
            }
            if (preBuildOutputDir == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeUTF(preBuildOutputDir);
            }
            if (sourceParents == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeObject(sourceParents);
            }
            if (targetDir == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeUTF(targetDir);
            }
        }

        public static class Builder {

            private ArtifactKey appArtifactKey;
            private String name;
            private String projectDirectory;
            private PathCollection sourcePaths = PathList.of();
            private String classesPath;
            private PathCollection resourcePaths = PathList.of();
            private String resourcesOutputPath;

            private String preBuildOutputDir;
            private PathCollection sourceParents = PathList.of();
            private String targetDir;

            private PathCollection testSourcePaths = PathList.of();
            private String testClassesPath;
            private PathCollection testResourcePaths = PathList.of();
            private String testResourcesOutputPath;

            public Builder setArtifactKey(ArtifactKey appArtifactKey) {
                this.appArtifactKey = appArtifactKey;
                return this;
            }

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public Builder setProjectDirectory(String projectDirectory) {
                this.projectDirectory = projectDirectory;
                return this;
            }

            public Builder setSourcePaths(PathCollection sourcePaths) {
                this.sourcePaths = sourcePaths;
                return this;
            }

            public Builder setClassesPath(String classesPath) {
                this.classesPath = classesPath;
                return this;
            }

            public Builder setResourcePaths(PathCollection resourcePaths) {
                this.resourcePaths = resourcePaths;
                return this;
            }

            public Builder setResourcesOutputPath(String resourcesOutputPath) {
                this.resourcesOutputPath = resourcesOutputPath;
                return this;
            }

            public Builder setPreBuildOutputDir(String preBuildOutputDir) {
                this.preBuildOutputDir = preBuildOutputDir;
                return this;
            }

            public Builder setSourceParents(PathCollection sourceParents) {
                this.sourceParents = sourceParents;
                return this;
            }

            public Builder setTargetDir(String targetDir) {
                this.targetDir = targetDir;
                return this;
            }

            public Builder setTestSourcePaths(PathCollection testSourcePaths) {
                this.testSourcePaths = testSourcePaths;
                return this;
            }

            public Builder setTestClassesPath(String testClassesPath) {
                this.testClassesPath = testClassesPath;
                return this;
            }

            public Builder setTestResourcePaths(PathCollection testResourcePaths) {
                this.testResourcePaths = testResourcePaths;
                return this;
            }

            public Builder setTestResourcesOutputPath(String testResourcesOutputPath) {
                this.testResourcesOutputPath = testResourcesOutputPath;
                return this;
            }

            public ModuleInfo build() {
                return new ModuleInfo(this);
            }
        }
    }

    public static class CompilationUnit implements Externalizable, Serializable {

        private static final long serialVersionUID = -2376878905527960968L;

        private PathCollection sourcePaths;
        private String classesPath;
        private PathCollection resourcePaths;
        private String resourcesOutputPath;

        public CompilationUnit(PathCollection sourcePaths, String classesPath, PathCollection resourcePaths,
                String resourcesOutputPath) {
            this.sourcePaths = sourcePaths;
            this.classesPath = classesPath;
            this.resourcePaths = resourcePaths;
            this.resourcesOutputPath = resourcesOutputPath;
        }

        public CompilationUnit() {
        }

        public PathCollection getSourcePaths() {
            return sourcePaths;
        }

        public String getClassesPath() {
            return classesPath;
        }

        public PathCollection getResourcePaths() {
            return resourcePaths;
        }

        public String getResourcesOutputPath() {
            return resourcesOutputPath;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            if (in.readBoolean()) {
                sourcePaths = (PathCollection) in.readObject();
            }
            if (in.readBoolean()) {
                classesPath = in.readUTF();
            }
            if (in.readBoolean()) {
                resourcePaths = (PathCollection) in.readObject();
            }
            if (in.readBoolean()) {
                resourcesOutputPath = in.readUTF();
            }
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            if (sourcePaths == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeObject(sourcePaths);
            }
            if (classesPath == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeUTF(classesPath);
            }
            if (resourcePaths == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeObject(resourcePaths);
            }
            if (resourcesOutputPath == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeUTF(resourcesOutputPath);
            }
        }
    }

    public boolean isEnablePreview() {
        if (compilerOptions == null) {
            return false;
        }
        return compilerOptions.contains(ENABLE_PREVIEW_FLAG);
    }
}
