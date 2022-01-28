package io.quarkus.deployment.dev;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.bootstrap.model.AppArtifactKey;
import io.quarkus.bootstrap.model.PathsCollection;
import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.paths.PathCollection;
import io.quarkus.paths.PathList;

/**
 * Object that is used to pass context data from the plugin doing the invocation
 * into the dev mode process using java serialization.
 * <p>
 * There is no need to worry about compat as both sides will always be using the same version
 */
public class DevModeContext {

    private static final long serialVersionUID = 4688502145533897982L;

    public static final CompilationUnit EMPTY_COMPILATION_UNIT = new CompilationUnit(PathList.of(), null, null, null);

    public static final String ENABLE_PREVIEW_FLAG = "--enable-preview";

    private ModuleInfo applicationRoot;
    private final List<ModuleInfo> additionalModules = new ArrayList<>();
    private final Map<String, String> systemProperties = new HashMap<>();
    private final Map<String, String> buildSystemProperties = new HashMap<>();
    private String sourceEncoding;

    private File cacheDir;
    private File projectDir;
    private boolean test;
    private boolean abortOnFailedStart;
    // the jar file which is used to launch the DevModeMain
    private File devModeRunnerJarFile;
    private boolean localProjectDiscovery = true;
    // args of the main-method
    private List<String> args;

    private List<String> compilerOptions;
    private String releaseJavaVersion;
    private String sourceJavaVersion;
    private String targetJvmVersion;

    private List<String> compilerPluginArtifacts;
    private List<String> compilerPluginsOptions;

    private String alternateEntryPoint;
    private QuarkusBootstrap.Mode mode = QuarkusBootstrap.Mode.DEV;
    private String baseName;
    private final Set<ArtifactKey> localArtifacts = new HashSet<>();

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
        return args.toArray(new String[args.size()]);
    }

    public void setArgs(String[] args) {
        this.args = Arrays.asList(args);
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

    public static class ModuleInfo implements Serializable {

        private static final long serialVersionUID = -1376678003747618410L;

        private final ArtifactKey appArtifactKey;
        private final String name;
        private final String projectDirectory;
        private final CompilationUnit main;
        private final CompilationUnit test;

        private final String preBuildOutputDir;
        private final PathCollection sourceParents;
        private final String targetDir;

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

    public static class CompilationUnit implements Serializable {

        private static final long serialVersionUID = -511238068393954948L;

        private PathCollection sourcePaths;
        private final String classesPath;
        private final PathCollection resourcePaths;
        private final String resourcesOutputPath;

        public CompilationUnit(PathCollection sourcePaths, String classesPath, PathCollection resourcePaths,
                String resourcesOutputPath) {
            this.sourcePaths = sourcePaths;
            this.classesPath = classesPath;
            this.resourcePaths = resourcePaths;
            this.resourcesOutputPath = resourcesOutputPath;
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
    }

    public boolean isEnablePreview() {
        if (compilerOptions == null) {
            return false;
        }
        return compilerOptions.contains(ENABLE_PREVIEW_FLAG);
    }

    //serialization methods

    public static byte[] serialize(DevModeContext context) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeModuleInfo(context.applicationRoot, out);
        writeCollection(context.additionalModules, out, DevModeContext::writeModuleInfo);
        writeMap(context.systemProperties, out, DevModeContext::writeString, DevModeContext::writeString);
        writeMap(context.buildSystemProperties, out, DevModeContext::writeString, DevModeContext::writeString);
        writeString(context.sourceEncoding, out);
        writeFile(context.cacheDir, out);
        writeFile(context.projectDir, out);
        writeBoolean(context.test, out);
        writeBoolean(context.abortOnFailedStart, out);
        writeFile(context.devModeRunnerJarFile, out);
        writeBoolean(context.localProjectDiscovery, out);
        writeCollection(context.args, out, DevModeContext::writeString);
        writeCollection(context.compilerOptions, out, DevModeContext::writeString);
        writeString(context.releaseJavaVersion, out);
        writeString(context.sourceJavaVersion, out);
        writeString(context.targetJvmVersion, out);
        writeCollection(context.compilerPluginArtifacts, out, DevModeContext::writeString);
        writeCollection(context.compilerPluginsOptions, out, DevModeContext::writeString);
        writeString(context.alternateEntryPoint, out);
        writeString(context.mode.name(), out);
        writeString(context.baseName, out);
        writeCollection(context.localArtifacts, out, DevModeContext::writeArtifactKey);
        return out.toByteArray();
    }

    private static <T> void writeArtifactKey(ArtifactKey t, OutputStream outputStream) {
        writeString(t.getGroupId(), outputStream);
        writeString(t.getArtifactId(), outputStream);
        writeString(t.getClassifier(), outputStream);
        writeString(t.getType(), outputStream);
    }

    private static <K, V> void writeMap(Map<K, V> list, OutputStream out, BiConsumer<K, OutputStream> keyHandler,
            BiConsumer<V, OutputStream> valueHandler) {
        try {
            writeInt(list.size(), out);
            for (var e : list.entrySet()) {
                keyHandler.accept(e.getKey(), out);
                valueHandler.accept(e.getValue(), out);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void writeCollection(Collection<T> list, OutputStream out, BiConsumer<T, OutputStream> handler) {
        try {
            if (list == null) {
                writeInt(0, out);
                return;
            }
            writeInt(list.size(), out);
            for (T i : list) {
                handler.accept(i, out);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeModuleInfo(ModuleInfo m, OutputStream out) {
        writeArtifactKey(m.appArtifactKey, out);
        writeString(m.name, out);
        writeString(m.projectDirectory, out);
        writeCompilationUnit(m.main, out);
        writeCompilationUnit(m.test, out);

        writeString(m.preBuildOutputDir, out);
        writePathCollection(m.sourceParents, out);
        writeString(m.targetDir, out);
    }

    private static void writePathCollection(PathCollection sourceParents, OutputStream out) {
        try {
            writeInt(sourceParents.size(), out);
            for (var i : sourceParents) {
                writeFile(i.toFile(), out);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeCompilationUnit(CompilationUnit c, OutputStream out) {
        if (c == null) {
            writeBoolean(false, out);
            return;
        }
        writeBoolean(true, out);
        writePathCollection(c.sourcePaths, out);
        writeString(c.classesPath, out);
        writePathCollection(c.resourcePaths, out);
        writeString(c.resourcesOutputPath, out);
    }

    private static void writeFile(File s, OutputStream out) {
        writeString(s.getAbsolutePath(), out);
    }

    private static void writeString(String s, OutputStream out) {

        try {
            if (s == null) {
                writeInt(-1, out);
                return;
            }
            byte[] data = s.getBytes(StandardCharsets.UTF_8);
            writeInt(data.length, out);
            out.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final void writeInt(int val, OutputStream os)
            throws IOException {
        os.write(val >> 24);
        os.write(val >> 16);
        os.write(val >> 8);
        os.write(val);
    }

    public static final void writeBoolean(boolean val, OutputStream os) {
        try {
            os.write(val ? 1 : 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DevModeContext deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        DevModeContext context = new DevModeContext();
        context.applicationRoot = readModuleInfo(buffer);
        context.additionalModules.addAll(readCollection(buffer, DevModeContext::readModuleInfo, ArrayList::new));
        context.systemProperties.putAll(readMap(buffer, DevModeContext::readString, DevModeContext::readString));
        context.buildSystemProperties.putAll(readMap(buffer, DevModeContext::readString, DevModeContext::readString));
        context.sourceEncoding = readString(buffer);
        context.cacheDir = readFile(buffer);
        context.projectDir = readFile(buffer);
        context.test = readBoolean(buffer);
        context.abortOnFailedStart = readBoolean(buffer);
        context.devModeRunnerJarFile = readFile(buffer);
        context.localProjectDiscovery = readBoolean(buffer);
        context.args = readCollection(buffer, DevModeContext::readString, ArrayList::new);
        context.compilerOptions = readCollection(buffer, DevModeContext::readString, ArrayList::new);
        context.releaseJavaVersion = readString(buffer);
        context.sourceJavaVersion = readString(buffer);
        context.targetJvmVersion = readString(buffer);
        context.compilerPluginArtifacts = readCollection(buffer, DevModeContext::readString, ArrayList::new);
        context.compilerPluginsOptions = readCollection(buffer, DevModeContext::readString, ArrayList::new);
        context.alternateEntryPoint = readString(buffer);
        context.mode = QuarkusBootstrap.Mode.valueOf(readString(buffer));
        context.baseName = readString(buffer);
        context.localArtifacts.addAll(readCollection(buffer, DevModeContext::readArtifactKey, HashSet::new));
        return context;
    }

    private static ArtifactKey readArtifactKey(ByteBuffer buffer) {
        String groupId = readString(buffer);
        String artifactId = readString(buffer);
        String classifier = readString(buffer);
        String type = readString(buffer);
        return new AppArtifactKey(groupId, artifactId, classifier, type);
    }

    private static <K, V> Map<K, V> readMap(ByteBuffer buffer, Function<ByteBuffer, K> keyHandler,
            Function<ByteBuffer, V> valueHandler) {
        int size = readInt(buffer);
        Map<K, V> ret = new HashMap<>();
        for (int i = 0; i < size; ++i) {
            K key = keyHandler.apply(buffer);
            V value = valueHandler.apply(buffer);
            ret.put(key, value);
        }
        return ret;
    }

    private static <T, C extends Collection<T>> C readCollection(ByteBuffer buffer, Function<ByteBuffer, T> handler,
            Supplier<C> supplier) {
        int size = readInt(buffer);
        C ret = supplier.get();
        for (int i = 0; i < size; ++i) {
            ret.add(handler.apply(buffer));
        }
        return ret;
    }

    private static ModuleInfo readModuleInfo(ByteBuffer buffer) {
        ModuleInfo.Builder m = new ModuleInfo.Builder();
        m.appArtifactKey = readArtifactKey(buffer);
        m.name = readString(buffer);
        m.projectDirectory = readString(buffer);
        var main = readCompilationUnit(buffer);
        var test = readCompilationUnit(buffer);
        if (main != null) {
            m.setSourcePaths(main.sourcePaths);
            m.setClassesPath(main.classesPath);
            m.setResourcePaths(main.resourcePaths);
            m.setResourcesOutputPath(main.resourcesOutputPath);
        }
        if (test != null) {
            m.setTestSourcePaths(test.sourcePaths);
            m.setTestClassesPath(test.classesPath);
            m.setTestResourcePaths(test.resourcePaths);
            m.setTestResourcesOutputPath(test.resourcesOutputPath);
        }

        m.preBuildOutputDir = readString(buffer);
        m.sourceParents = readPathCollection(buffer);
        m.targetDir = readString(buffer);
        return m.build();
    }

    private static PathCollection readPathCollection(ByteBuffer buffer) {
        int size = readInt(buffer);
        List<Path> ret = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            ret.add(readFile(buffer).toPath());
        }
        return PathsCollection.from(ret);
    }

    private static CompilationUnit readCompilationUnit(ByteBuffer buffer) {
        boolean exists = readBoolean(buffer);
        if (!exists) {
            return null;
        }
        PathCollection sourcePaths = readPathCollection(buffer);
        String classesPath = readString(buffer);
        PathCollection resourcePaths = readPathCollection(buffer);
        String resourcesOutputPath = readString(buffer);
        return new CompilationUnit(sourcePaths, classesPath, resourcePaths, resourcesOutputPath);
    }

    private static File readFile(ByteBuffer buffer) {
        return new File(readString(buffer));
    }

    private static String readString(ByteBuffer buffer) {
        int length = readInt(buffer);
        if (length == -1) {
            return null;
        }
        byte[] data = new byte[length];
        buffer.get(data);
        return new String(data, StandardCharsets.UTF_8);
    }

    private static int readInt(ByteBuffer b) {
        int ret = 0;
        ret += (b.get() & 0xFF) << 24;
        ret += (b.get() & 0xFF) << 16;
        ret += (b.get() & 0xFF) << 8;
        ret += (b.get() & 0xFF);
        return ret;
    }

    public static boolean readBoolean(ByteBuffer b) {
        return b.get() > 0;
    }

}
