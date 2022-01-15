package io.quarkus.vertx.http.runtime.webjar;

import java.util.Map;

import io.quarkus.vertx.http.runtime.devmode.InMemoryStaticHandler;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class WebJarStaticHandler implements Handler<RoutingContext> {
    private String finalDestination;
    private String path;

    private Map<String, byte[]> files;

    public WebJarStaticHandler() {
    }

    public WebJarStaticHandler(String finalDestination, String path,
            Map<String, byte[]> files) {
        this.finalDestination = finalDestination;
        this.path = path;
        this.files = files;
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

    public Map<String, byte[]> getFiles() {
        return files;
    }

    public void setFiles(Map<String, byte[]> files) {
        this.files = files;
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

        if (finalDestination == null) {
            InMemoryStaticHandler handler = new InMemoryStaticHandler(files);
            handler.handle(event);
        } else {
            StaticHandler staticHandler = StaticHandler.create().setAllowRootFileSystemAccess(true)
                    .setWebRoot(finalDestination)
                    .setDefaultContentEncoding("UTF-8");
            staticHandler.handle(event);
        }

    }
}
