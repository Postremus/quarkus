package io.quarkus.vertx.http.deployment.webjar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.maven.dependency.ArtifactKey;
import io.quarkus.maven.dependency.ResolvedDependency;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.vertx.http.runtime.devmode.FileSystemStaticHandler;
import io.quarkus.vertx.http.runtime.webjar.WebJarRecorder;

public class WebJarProcessor {
    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep(onlyIfNot = IsNormal.class)
    WebJarResultBuildItem processWebJarDevMode(WebJarRecorder recorder, List<WebJarBuildItem> webJars,
            CurateOutcomeBuildItem curateOutcomeBuildItem,
            ShutdownContextBuildItem shutdownContext,
            ApplicationConfig applicationConfig) throws IOException {

        Map<ArtifactKey, WebJarResultBuildItem.WebJarResult> results = new HashMap<>();

        Path deploymentBasePath = Files.createTempDirectory("quarkus-webjar");
        recorder.shutdownTask(shutdownContext, deploymentBasePath.toString());

        for (WebJarBuildItem webJar : webJars) {
            Path resourcesDirectory = deploymentBasePath.resolve(webJar.getFinalDestination());
            ResolvedDependency dependency = WebJarUtil.getAppArtifact(curateOutcomeBuildItem, webJar.getArtifactKey());

            Map<String, byte[]> files = WebJarUtil.copyResourcesForDevOrTest(curateOutcomeBuildItem, applicationConfig, webJar,
                    dependency, resourcesDirectory);

            results.put(webJar.getArtifactKey(),
                    new WebJarResultBuildItem.WebJarResult(dependency, null,
                            null, files));
        }

        return new WebJarResultBuildItem(results);
    }

    @BuildStep(onlyIf = IsNormal.class)
    WebJarResultBuildItem processWebJarProdMode(List<WebJarBuildItem> webJars,
            CurateOutcomeBuildItem curateOutcomeBuildItem,
            BuildProducer<GeneratedResourceBuildItem> generatedResources,
            BuildProducer<NativeImageResourceBuildItem> nativeImageResourceBuildItemBuildProducer,
            ApplicationConfig applicationConfig) {

        Map<ArtifactKey, WebJarResultBuildItem.WebJarResult> results = new HashMap<>();

        for (WebJarBuildItem webJar : webJars) {

            ResolvedDependency dependency = WebJarUtil.getAppArtifact(curateOutcomeBuildItem, webJar.getArtifactKey());

            Map<String, byte[]> files = WebJarUtil.copyResourcesForProduction(
                    curateOutcomeBuildItem, applicationConfig, webJar, dependency);

            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                String fileName = webJar.getFinalDestination() + "/" + file.getKey();
                byte[] fileContent = file.getValue();

                generatedResources
                        .produce(new GeneratedResourceBuildItem(fileName, fileContent));
                nativeImageResourceBuildItemBuildProducer.produce(new NativeImageResourceBuildItem(fileName));
            }

            List<FileSystemStaticHandler.StaticWebRootConfiguration> webRootConfigurations = new ArrayList<>();
            webRootConfigurations.add(
                    new FileSystemStaticHandler.StaticWebRootConfiguration(webJar.getFinalDestination(),
                            ""));

            results.put(webJar.getArtifactKey(),
                    new WebJarResultBuildItem.WebJarResult(dependency, webJar.getFinalDestination(), webRootConfigurations,
                            files));
        }

        return new WebJarResultBuildItem(results);
    }
}
