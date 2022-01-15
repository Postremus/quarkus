package io.quarkus.vertx.http.runtime.devmode;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.HttpUtils;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.net.impl.URIDecoder;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;

/**
 * A Handler to serve static files from jar files or from a local directory.
 */
public class InMemoryStaticHandler implements Handler<RoutingContext> {

    private static final String DEFAULT_CONTENT_ENCODING = Charset.defaultCharset().name();

    private Map<String, byte[]> files;

    public InMemoryStaticHandler() {

    }

    public InMemoryStaticHandler(Map<String, byte[]> files) {
        this.files = files;
    }

    public Map<String, byte[]> getFiles() {
        return files;
    }

    public void setFiles(Map<String, byte[]> files) {
        this.files = files;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();
        if (request.method() != HttpMethod.GET && request.method() != HttpMethod.HEAD) {
            context.next();
            return;
        }

        // decode URL path
        String uriDecodedPath = URIDecoder.decodeURIComponent(context.normalizedPath(), false);
        // if the normalized path is null it cannot be resolved
        if (uriDecodedPath == null) {
            context.next();
            return;
        }
        // will normalize and handle all paths as UNIX paths
        String path = HttpUtils.removeDots(uriDecodedPath.replace('\\', '/'));
        path = Utils.pathOffset(path, context);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        try {
            sendStatic(context, path);
        } catch (IOException e) {
            context.fail(e);
            return;
        }
    }

    /**
     *
     * @param context
     * @param path
     * @throws IOException if an I/O error occurs
     */
    private void sendStatic(RoutingContext context, String path) throws IOException {
        byte[] content = null;
        content = files.get(path);

        if (content == null) {
            context.next();
            return;
        }

        final HttpServerResponse response = context.response();
        String contentType = MimeMapping.getMimeTypeForFilename(path);
        if (contentType != null) {
            if (contentType.startsWith("text")) {
                response.putHeader(HttpHeaders.CONTENT_TYPE, contentType + ";charset=" + DEFAULT_CONTENT_ENCODING);
            } else {
                response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
            }
        }

        if (path.endsWith("/")) {
            // directory listing is not supported
            context.next();
            return;
        } else {
            context.end(Buffer.buffer(content));
        }
    }
}
