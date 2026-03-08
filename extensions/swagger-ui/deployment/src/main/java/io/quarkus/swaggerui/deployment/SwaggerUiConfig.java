package io.quarkus.swaggerui.deployment;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot
@ConfigMapping(prefix = "quarkus.swagger-ui")
public interface SwaggerUiConfig {

    /**
     * The path where Swagger UI is available.
     * <p>
     * The value `/` is not allowed as it blocks the application from serving anything else.
     * By default, this value will be resolved as a path relative to `${quarkus.http.non-application-root-path}`.
     */
    @WithDefault("swagger-ui")
    String path();

    /**
     * If this should be included every time. By default, this is only included when the application is running
     * in dev mode.
     */
    @WithDefault("false")
    boolean alwaysInclude();
}
