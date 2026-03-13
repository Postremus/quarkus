package io.quarkus.swaggerui.runtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import io.smallrye.openapi.ui.IndexHtmlCreator;
import io.smallrye.openapi.ui.Option;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class IndexHtmlHandler implements Handler<RoutingContext> {

    private static final Logger LOG = Logger.getLogger(IndexHtmlHandler.class);
    private static final String OIDC_NONCE_KEY = "nonce";

    private final String swaggerUiPath;
    private final String selfHref;
    private final String defaultTitle;
    private final Map<String, String> buildTimeDefaultUrls;
    private final Map<String, String> buildItemUrls;
    private final String devServicesOidcClientId;
    private final boolean isDevOrTest;
    private final SwaggerUiRuntimeConfig config;

    private volatile byte[] cachedIndexHtml;

    public IndexHtmlHandler(String swaggerUiPath, String selfHref, String defaultTitle,
            Map<String, String> buildTimeDefaultUrls,
            Map<String, String> buildItemUrls,
            String devServicesOidcClientId,
            boolean isDevOrTest,
            SwaggerUiRuntimeConfig config) {
        this.swaggerUiPath = swaggerUiPath;
        this.selfHref = selfHref;
        this.defaultTitle = defaultTitle;
        this.buildTimeDefaultUrls = buildTimeDefaultUrls;
        this.buildItemUrls = buildItemUrls;
        this.devServicesOidcClientId = devServicesOidcClientId;
        this.isDevOrTest = isDevOrTest;
        this.config = config;
    }

    @Override
    public void handle(RoutingContext event) {
        if (event.normalizedPath().endsWith("/index.html")) {
            serveIndexHtml(event);
            return;
        }
        event.next();
    }

    private void serveIndexHtml(RoutingContext event) {
        if (cachedIndexHtml == null) {
            synchronized (this) {
                if (cachedIndexHtml == null) {
                    try {
                        cachedIndexHtml = generateIndexHtml();
                    } catch (IOException e) {
                        event.fail(e);
                        return;
                    }
                }
            }
        }
        event.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8")
                .end(Buffer.buffer(cachedIndexHtml));
    }

    private byte[] generateIndexHtml() throws IOException {
        Map<Option, String> options = new HashMap<>();
        Map<String, String> urlsMap = new HashMap<>();

        options.put(Option.selfHref, selfHref);
        options.put(Option.backHref, selfHref);

        if (config.urls() != null && !config.urls().isEmpty()) {
            urlsMap.putAll(config.urls());
        }

        for (Map.Entry<String, String> entry : buildItemUrls.entrySet()) {
            urlsMap.putIfAbsent(entry.getKey(), entry.getValue());
        }

        if (urlsMap.isEmpty()) {
            if (buildTimeDefaultUrls.size() > 1) {
                urlsMap = buildTimeDefaultUrls;
            } else if (!buildTimeDefaultUrls.isEmpty()) {
                options.put(Option.url, buildTimeDefaultUrls.values().iterator().next());
            }
        }

        options.put(Option.title, config.title().orElse(defaultTitle));

        if (config.theme().isPresent()) {
            options.put(Option.themeHref, config.theme().get().toString());
        }

        if (config.footer().isPresent()) {
            options.put(Option.footer, config.footer().get());
        }

        if (config.deepLinking().isPresent()) {
            options.put(Option.deepLinking, config.deepLinking().get().toString());
        }

        if (config.displayOperationId().isPresent()) {
            options.put(Option.displayOperationId, config.displayOperationId().get().toString());
        }

        if (config.defaultModelsExpandDepth().isPresent()) {
            options.put(Option.defaultModelsExpandDepth,
                    String.valueOf(config.defaultModelsExpandDepth().getAsInt()));
        }

        if (config.defaultModelExpandDepth().isPresent()) {
            options.put(Option.defaultModelExpandDepth,
                    String.valueOf(config.defaultModelExpandDepth().getAsInt()));
        }

        if (config.defaultModelRendering().isPresent()) {
            options.put(Option.defaultModelRendering, config.defaultModelRendering().get());
        }

        if (config.displayRequestDuration().isPresent()) {
            options.put(Option.displayRequestDuration, config.displayRequestDuration().get().toString());
        }

        if (config.docExpansion().isPresent()) {
            options.put(Option.docExpansion, config.docExpansion().get().toString());
        }

        if (config.filter().isPresent()) {
            options.put(Option.filter, config.filter().get());
        }

        if (config.maxDisplayedTags().isPresent()) {
            options.put(Option.maxDisplayedTags, String.valueOf(config.maxDisplayedTags().getAsInt()));
        }

        if (config.operationsSorter().isPresent()) {
            options.put(Option.operationsSorter, config.operationsSorter().get());
        }

        if (config.showExtensions().isPresent()) {
            options.put(Option.showExtensions, config.showExtensions().get().toString());
        }

        if (config.showCommonExtensions().isPresent()) {
            options.put(Option.showCommonExtensions, config.showCommonExtensions().get().toString());
        }

        if (config.tagsSorter().isPresent()) {
            options.put(Option.tagsSorter, config.tagsSorter().get());
        }

        if (config.onComplete().isPresent()) {
            options.put(Option.onComplete, config.onComplete().get());
        }

        if (config.syntaxHighlight().isPresent()) {
            options.put(Option.syntaxHighlight, config.syntaxHighlight().get());
        }

        if (config.oauth2RedirectUrl().isPresent()) {
            options.put(Option.oauth2RedirectUrl, config.oauth2RedirectUrl().get());
        } else {
            options.put(Option.oauth2RedirectUrl, selfHref + "/oauth2-redirect.html");
        }

        if (config.requestInterceptor().isPresent()) {
            options.put(Option.requestInterceptor, config.requestInterceptor().get());
        }

        if (config.requestCurlOptions().isPresent()) {
            options.put(Option.requestCurlOptions, config.requestCurlOptions().get().toString());
        }

        if (config.responseInterceptor().isPresent()) {
            options.put(Option.responseInterceptor, config.responseInterceptor().get());
        }

        if (config.showMutatedRequest().isPresent()) {
            options.put(Option.showMutatedRequest, config.showMutatedRequest().get().toString());
        }

        if (config.supportedSubmitMethods().isPresent()) {
            options.put(Option.supportedSubmitMethods, config.supportedSubmitMethods().get().toString());
        }

        if (config.validatorUrl().isPresent()) {
            options.put(Option.validatorUrl, config.validatorUrl().get());
        }

        if (config.withCredentials().isPresent()) {
            options.put(Option.withCredentials, config.withCredentials().get().toString());
        }

        if (config.modelPropertyMacro().isPresent()) {
            options.put(Option.modelPropertyMacro, config.modelPropertyMacro().get());
        }

        if (config.parameterMacro().isPresent()) {
            options.put(Option.parameterMacro, config.parameterMacro().get());
        }

        if (config.persistAuthorization().isPresent()) {
            options.put(Option.persistAuthorization, config.persistAuthorization().get().toString());
        } else if (isDevOrTest) {
            options.put(Option.persistAuthorization, String.valueOf(true));
        }

        if (config.layout().isPresent()) {
            options.put(Option.layout, config.layout().get());
        }

        if (config.plugins().isPresent()) {
            options.put(Option.plugins, config.plugins().get().toString());
        }

        if (config.scripts().isPresent()) {
            options.put(Option.scripts, String.join(",", config.scripts().get()));
        }

        if (config.presets().isPresent()) {
            options.put(Option.presets, config.presets().get().toString());
        }

        if (config.oauthClientId().isPresent()) {
            options.put(Option.oauthClientId, config.oauthClientId().get());
        } else if (devServicesOidcClientId != null) {
            options.put(Option.oauthClientId, devServicesOidcClientId);
        }

        if (config.oauthClientSecret().isPresent()) {
            options.put(Option.oauthClientSecret, config.oauthClientSecret().get());
        }

        if (config.oauthRealm().isPresent()) {
            options.put(Option.oauthRealm, config.oauthRealm().get());
        }

        if (config.oauthAppName().isPresent()) {
            options.put(Option.oauthAppName, config.oauthAppName().get());
        }

        if (config.oauthScopeSeparator().isPresent()) {
            options.put(Option.oauthScopeSeparator, config.oauthScopeSeparator().get());
        }

        if (config.oauthScopes().isPresent()) {
            options.put(Option.oauthScopes, config.oauthScopes().get());
        }

        if (config.queryConfigEnabled()) {
            options.put(Option.queryConfigEnabled, "true");
        }

        JsonObject oauthAdditionalQueryStringParamMap = new JsonObject();
        if (config.oauthAdditionalQueryStringParams().isPresent()) {
            String oauthAdditionalQueryStringParams = config.oauthAdditionalQueryStringParams().get();
            try {
                JsonObject parsed = new JsonObject(oauthAdditionalQueryStringParams);
                if (parsed.isEmpty()) {
                    LOG.warn(
                            "Property 'quarkus.swagger-ui.oauth-additional-query-string-params' should be a map, example: quarkus.swagger-ui.oauth-additional-query-string-params='{\"foo\": \"bar\"}' ");
                } else {
                    oauthAdditionalQueryStringParamMap = parsed;
                }
            } catch (Exception e) {
                LOG.warn(
                        "Property 'quarkus.swagger-ui.oauth-additional-query-string-params' should be a map, example: quarkus.swagger-ui.oauth-additional-query-string-params='{\"foo\": \"bar\"}' ");
            }
        }

        if (!oauthAdditionalQueryStringParamMap.containsKey(OIDC_NONCE_KEY)) {
            oauthAdditionalQueryStringParamMap.put(OIDC_NONCE_KEY, UUID.randomUUID().toString());
        }
        options.put(Option.oauthAdditionalQueryStringParams, oauthAdditionalQueryStringParamMap.encode());

        if (config.oauthUseBasicAuthenticationWithAccessCodeGrant().isPresent()) {
            options.put(Option.oauthUseBasicAuthenticationWithAccessCodeGrant,
                    config.oauthUseBasicAuthenticationWithAccessCodeGrant().get().toString());
        }

        if (config.oauthUsePkceWithAuthorizationCodeGrant().isPresent()) {
            options.put(Option.oauthUsePkceWithAuthorizationCodeGrant,
                    config.oauthUsePkceWithAuthorizationCodeGrant().get().toString());
        }

        if (config.preauthorizeBasicAuthDefinitionKey().isPresent()) {
            options.put(Option.preauthorizeBasicAuthDefinitionKey,
                    config.preauthorizeBasicAuthDefinitionKey().get());
        }

        if (config.preauthorizeBasicUsername().isPresent()) {
            options.put(Option.preauthorizeBasicUsername, config.preauthorizeBasicUsername().get());
        }

        if (config.preauthorizeBasicPassword().isPresent()) {
            options.put(Option.preauthorizeBasicPassword, config.preauthorizeBasicPassword().get());
        }

        if (config.preauthorizeApiKeyAuthDefinitionKey().isPresent()) {
            options.put(Option.preauthorizeApiKeyAuthDefinitionKey,
                    config.preauthorizeApiKeyAuthDefinitionKey().get());
        }

        if (config.preauthorizeApiKeyApiKeyValue().isPresent()) {
            options.put(Option.preauthorizeApiKeyApiKeyValue,
                    config.preauthorizeApiKeyApiKeyValue().get());
        }

        if (config.tryItOutEnabled()) {
            options.put(Option.tryItOutEnabled, "true");
        }

        return IndexHtmlCreator.createIndexHtml(urlsMap, config.urlsPrimaryName().orElse(null), options);
    }
}
