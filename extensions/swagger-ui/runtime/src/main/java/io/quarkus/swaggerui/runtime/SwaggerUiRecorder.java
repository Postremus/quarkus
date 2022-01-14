package io.quarkus.swaggerui.runtime;

import java.util.List;

import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.vertx.http.runtime.devmode.FileSystemStaticHandler;
import io.quarkus.vertx.http.runtime.webjar.WebJarNotFoundHandler;
import io.quarkus.vertx.http.runtime.webjar.WebJarStaticHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class SwaggerUiRecorder {

    public Handler<RoutingContext> handler(String swaggerUiFinalDestination, String swaggerUiPath,
            List<FileSystemStaticHandler.StaticWebRootConfiguration> webRootConfigurations,
            SwaggerUiRuntimeConfig runtimeConfig) {

        if (runtimeConfig.enable) {
            return new WebJarStaticHandler(swaggerUiFinalDestination, swaggerUiPath, webRootConfigurations);
        } else {
            return new WebJarNotFoundHandler();
        }
    }
}
