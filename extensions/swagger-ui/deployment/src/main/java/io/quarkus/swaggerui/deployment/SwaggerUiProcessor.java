package io.quarkus.swaggerui.deployment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;

import io.quarkus.builder.Version;
import io.quarkus.deployment.Feature;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.DevServicesLauncherConfigResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.HotDeploymentWatchedFileBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.devui.spi.DevContextBuildItem;
import io.quarkus.maven.dependency.GACT;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.smallrye.openapi.common.deployment.SmallRyeOpenApiConfig;
import io.quarkus.swaggerui.runtime.SwaggerUiRecorder;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.deployment.webjar.WebJarBuildItem;
import io.quarkus.vertx.http.deployment.webjar.WebJarResultsBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class SwaggerUiProcessor {
    private static final Logger LOG = Logger.getLogger(SwaggerUiProcessor.class);

    private static final GACT SWAGGER_UI_WEBJAR_ARTIFACT_KEY = new GACT("io.smallrye", "smallrye-open-api-ui", null, "jar");
    private static final String SWAGGER_UI_WEBJAR_STATIC_RESOURCES_PATH = "META-INF/resources/openapi-ui/";

    // Branding files to monitor for changes
    private static final String BRANDING_DIR = "META-INF/branding/";
    private static final String BRANDING_LOGO_GENERAL = BRANDING_DIR + "logo.png";
    private static final String BRANDING_LOGO_MODULE = BRANDING_DIR + "smallrye-open-api-ui.png";
    private static final String BRANDING_STYLE_GENERAL = BRANDING_DIR + "style.css";
    private static final String BRANDING_STYLE_MODULE = BRANDING_DIR + "smallrye-open-api-ui.css";
    private static final String BRANDING_FAVICON_GENERAL = BRANDING_DIR + "favicon.ico";
    private static final String BRANDING_FAVICON_MODULE = BRANDING_DIR + "smallrye-open-api-ui.ico";

    // To autoset some security config from OIDC
    private static final String OIDC_CLIENT_ID = "quarkus.oidc.client-id";

    @BuildStep
    void feature(BuildProducer<FeatureBuildItem> feature,
            LaunchModeBuildItem launchMode,
            SwaggerUiConfig swaggerUiConfig) {
        if (shouldInclude(launchMode, swaggerUiConfig)) {
            feature.produce(new FeatureBuildItem(Feature.SWAGGER_UI));
        }
    }

    @BuildStep
    List<HotDeploymentWatchedFileBuildItem> brandingFiles() {
        return Stream.of(BRANDING_LOGO_GENERAL,
                BRANDING_STYLE_GENERAL,
                BRANDING_FAVICON_GENERAL,
                BRANDING_LOGO_MODULE,
                BRANDING_STYLE_MODULE,
                BRANDING_FAVICON_MODULE).map(HotDeploymentWatchedFileBuildItem::new)
                .collect(Collectors.toList());
    }

    @BuildStep
    public void getSwaggerUiFinalDestination(
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            LaunchModeBuildItem launchMode,
            SwaggerUiConfig swaggerUiConfig,
            SmallRyeOpenApiConfig openapi,
            BuildProducer<WebJarBuildItem> webJarBuildProducer) throws Exception {

        if (shouldInclude(launchMode, swaggerUiConfig)) {
            if ("/".equals(swaggerUiConfig.path())) {
                throw new ConfigurationException(
                        "quarkus.swagger-ui.path was set to \"/\", this is not allowed as it blocks the application from serving anything else.",
                        Set.of("quarkus.swagger-ui.path"));
            }

            openapi.documents().forEach((documentName, documentConfig) -> {
                String documentPath = documentConfig.path();
                if (documentPath.equalsIgnoreCase(swaggerUiConfig.path())) {
                    throw new ConfigurationException(
                            "quarkus.smallrye-openapi.path and quarkus.swagger-ui.path was set to the same value, this is not allowed as the paths needs to be unique ["
                                    + documentPath + "].",
                            Set.of(documentPath, "quarkus.swagger-ui.path"));
                }
            });

            webJarBuildProducer.produce(
                    WebJarBuildItem.builder().artifactKey(SWAGGER_UI_WEBJAR_ARTIFACT_KEY)
                            .root(SWAGGER_UI_WEBJAR_STATIC_RESOURCES_PATH)
                            .build());
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public void registerSwaggerUiHandler(SwaggerUiRecorder recorder,
            BuildProducer<RouteBuildItem> routes,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            Optional<DevContextBuildItem> devContextBuildItem,
            WebJarResultsBuildItem webJarResultsBuildItem,
            LaunchModeBuildItem launchMode,
            SwaggerUiConfig swaggerUiConfig,
            SmallRyeOpenApiConfig openapi,
            Optional<DevServicesLauncherConfigResultBuildItem> devServicesLauncherConfig,
            List<SwaggerUiUrlBuildItem> swaggerUiUrls,
            BuildProducer<SwaggerUiBuildItem> swaggerUiBuildProducer,
            ShutdownContextBuildItem shutdownContext) {

        WebJarResultsBuildItem.WebJarResult result = webJarResultsBuildItem.byArtifactKey(SWAGGER_UI_WEBJAR_ARTIFACT_KEY);
        if (result == null) {
            return;
        }

        if (shouldInclude(launchMode, swaggerUiConfig)) {
            String devUIContextRoot = devContextBuildItem.map(DevContextBuildItem::getDevUIContextRoot).orElse("");
            String swaggerUiPath = nonApplicationRootPathBuildItem.resolvePath(swaggerUiConfig.path());
            String selfHref = devUIContextRoot + swaggerUiPath;

            Map<String, String> buildTimeDefaultUrls = new HashMap<>();
            openapi.documents().forEach((documentName, documentConfig) -> {
                String openApiPath = devUIContextRoot + nonApplicationRootPathBuildItem.resolvePath(documentConfig.path());
                buildTimeDefaultUrls.put(documentName, openApiPath);
            });

            Map<String, String> buildItemUrls = new HashMap<>();
            for (SwaggerUiUrlBuildItem urlBuildItem : swaggerUiUrls) {
                buildItemUrls.put(urlBuildItem.getName(), urlBuildItem.getUrl());
            }

            String devServicesOidcClientId = null;
            if (devServicesLauncherConfig.isPresent()) {
                Map<String, String> devServiceConfig = devServicesLauncherConfig.get().getConfig();
                if (devServiceConfig != null) {
                    devServicesOidcClientId = devServiceConfig.get(OIDC_CLIENT_ID);
                }
            }

            boolean isDevOrTest = launchMode.getLaunchMode().isDevOrTest();
            String defaultTitle = "OpenAPI UI (Powered by Quarkus " + Version.getVersion() + ")";

            swaggerUiBuildProducer.produce(new SwaggerUiBuildItem(result.getFinalDestination(), swaggerUiPath));

            Consumer<Handler<RoutingContext>> handlerRegistrar = (handler) -> {
                routes.produce(nonApplicationRootPathBuildItem.routeBuilder()
                        .management("quarkus.smallrye-openapi.management.enabled")
                        .route(swaggerUiConfig.path())
                        .displayOnNotFoundPage("Open API UI")
                        .routeConfigKey("quarkus.swagger-ui.path")
                        .handler(handler)
                        .build());

                routes.produce(nonApplicationRootPathBuildItem.routeBuilder()
                        .management("quarkus.smallrye-openapi.management.enabled")
                        .route(swaggerUiConfig.path() + "*")
                        .handler(handler)
                        .build());
            };

            Handler<RoutingContext> staticHandler = recorder.staticHandler(result.getFinalDestination(), swaggerUiPath,
                    result.getWebRootConfigurations(), shutdownContext);
            handlerRegistrar.accept(staticHandler);

            Handler<RoutingContext> indexHtmlHandler = recorder.indexHtmlHandler(swaggerUiPath,
                    selfHref, defaultTitle, buildTimeDefaultUrls, buildItemUrls, devServicesOidcClientId, isDevOrTest);
            handlerRegistrar.accept(indexHtmlHandler);

        }
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, SwaggerUiConfig swaggerUiConfig) {
        return launchMode.getLaunchMode().isDevOrTest() || swaggerUiConfig.alwaysInclude();
    }
}
