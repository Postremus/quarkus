package io.quarkus.swaggerui.runtime;

import java.util.List;
import java.util.Map;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.vertx.http.runtime.devmode.FileSystemStaticHandler;
import io.quarkus.vertx.http.runtime.webjar.WebJarNotFoundHandler;
import io.quarkus.vertx.http.runtime.webjar.WebJarStaticHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class SwaggerUiRecorder {
    private final RuntimeValue<SwaggerUiRuntimeConfig> runtimeConfig;

    public SwaggerUiRecorder(final RuntimeValue<SwaggerUiRuntimeConfig> runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
    }

    public Handler<RoutingContext> staticHandler(String swaggerUiFinalDestination, String swaggerUiPath,
            List<FileSystemStaticHandler.StaticWebRootConfiguration> webRootConfigurations,
            ShutdownContext shutdownContext) {
        if (!isEnabled()) {
            return new WebJarNotFoundHandler();
        }

        WebJarStaticHandler handler = new WebJarStaticHandler(swaggerUiFinalDestination, swaggerUiPath,
                webRootConfigurations);
        shutdownContext.addShutdownTask(new ShutdownContext.CloseRunnable(handler));
        return handler;
    }

    public Handler<RoutingContext> indexHtmlHandler(String swaggerUiPath,
            String selfHref, String defaultTitle,
            Map<String, String> buildTimeDefaultUrls,
            Map<String, String> buildItemUrls,
            String devServicesOidcClientId,
            boolean isDevOrTest) {
        if (!isEnabled()) {
            return new WebJarNotFoundHandler();
        }

        SwaggerUiRuntimeConfig config = runtimeConfig.getValue();

        IndexHtmlHandler handler = new IndexHtmlHandler(swaggerUiPath, selfHref, defaultTitle,
                buildTimeDefaultUrls, buildItemUrls, devServicesOidcClientId, isDevOrTest, config);

        return handler;
    }

    private boolean isEnabled() {
        SwaggerUiRuntimeConfig config = runtimeConfig.getValue();
        return config.enable().orElse(config.enabled());
    }
}
