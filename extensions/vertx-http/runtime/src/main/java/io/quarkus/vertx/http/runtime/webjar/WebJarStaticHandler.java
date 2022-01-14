package io.quarkus.vertx.http.runtime.webjar;

import java.io.IOException;
import java.util.List;

import io.quarkus.vertx.http.runtime.devmode.FileSystemStaticHandler;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class WebJarStaticHandler implements Handler<RoutingContext> {
    private String finalDestination;
    private String path;

    private List<FileSystemStaticHandler.StaticWebRootConfiguration> webRootConfigurations;

    public WebJarStaticHandler() {
    }

    public WebJarStaticHandler(String finalDestination, String path,
            List<FileSystemStaticHandler.StaticWebRootConfiguration> webRootConfigurations) {
        this.finalDestination = finalDestination;
        this.path = path;
        this.webRootConfigurations = webRootConfigurations;
    }

    public String getFinalDestination() {
        return finalDestination;
    }

    public void setFinalDestination(String finalDestination) {
        this.finalDestination = finalDestination;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FileSystemStaticHandler.StaticWebRootConfiguration> getWebRootConfigurations() {
        return webRootConfigurations;
    }

    public void setWebRootConfigurations(List<FileSystemStaticHandler.StaticWebRootConfiguration> webRootConfigurations) {
        this.webRootConfigurations = webRootConfigurations;
    }

    @Override
    public void handle(RoutingContext event) {

        if (event.normalizedPath().length() == path.length()) {
            event.response().setStatusCode(302);
            event.response().headers().set(HttpHeaders.LOCATION, path + "/");
            event.response().end();
            return;
        } else if (event.normalizedPath().length() == path.length() + 1) {
            event.reroute(path + "/index.html");
            return;
        }

        if (!finalDestination.startsWith("META-INF")) {
            FileSystemStaticHandler handler = new FileSystemStaticHandler(webRootConfigurations);
            handler.handle(event);
            try {
                handler.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            StaticHandler staticHandler = StaticHandler.create().setAllowRootFileSystemAccess(true)
                    .setWebRoot(finalDestination)
                    .setDefaultContentEncoding("UTF-8");
            staticHandler.handle(event);
        }

    }
}
