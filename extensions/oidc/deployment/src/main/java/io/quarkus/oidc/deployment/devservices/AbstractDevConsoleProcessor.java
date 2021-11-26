package io.quarkus.oidc.deployment.devservices;

import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.devconsole.runtime.spi.DevConsolePostHandler;
import io.quarkus.devconsole.spi.DevConsoleRouteBuildItem;
import io.quarkus.devconsole.spi.DevConsoleTemplateInfoBuildItem;

public abstract class AbstractDevConsoleProcessor {
    protected void produceDevConsoleTemplateItems(Capabilities capabilities,
            BuildProducer<DevConsoleTemplateInfoBuildItem> devConsoleTemplate,
            String oidcProviderName,
            String oidcApplicationType,
            String oidcGrantType,
            String authorizationUrl,
            String tokenUrl,
            String logoutUrl,
            boolean introspectionIsAvailable) {
        if (oidcProviderName != null) {
            devConsoleTemplate
                    .produce(new DevConsoleTemplateInfoBuildItem("oidcProviderName", oidcProviderName, this.getClass()));
        }
        devConsoleTemplate
                .produce(new DevConsoleTemplateInfoBuildItem("oidcApplicationType", oidcApplicationType, this.getClass()));
        devConsoleTemplate.produce(new DevConsoleTemplateInfoBuildItem("oidcGrantType", oidcGrantType, this.getClass()));

        devConsoleTemplate.produce(new DevConsoleTemplateInfoBuildItem("authorizationUrl", authorizationUrl, this.getClass()));
        devConsoleTemplate.produce(new DevConsoleTemplateInfoBuildItem("tokenUrl", tokenUrl, this.getClass()));
        if (logoutUrl != null) {
            devConsoleTemplate.produce(new DevConsoleTemplateInfoBuildItem("logoutUrl", logoutUrl, this.getClass()));
        }
        if (capabilities.isPresent(Capability.SMALLRYE_OPENAPI)) {
            devConsoleTemplate.produce(new DevConsoleTemplateInfoBuildItem("swaggerIsAvailable", true, this.getClass()));
        }
        if (capabilities.isPresent(Capability.SMALLRYE_GRAPHQL)) {
            devConsoleTemplate.produce(new DevConsoleTemplateInfoBuildItem("graphqlIsAvailable", true, this.getClass()));
        }
        devConsoleTemplate.produce(
                new DevConsoleTemplateInfoBuildItem("introspectionIsAvailable", introspectionIsAvailable, this.getClass()));
    }

    protected void produceDevConsoleRouteItems(BuildProducer<DevConsoleRouteBuildItem> devConsoleRoute,
            DevConsolePostHandler testServiceWithToken,
            DevConsolePostHandler exchangeCodeForTokens,
            DevConsolePostHandler passwordClientCredHandler) {
        devConsoleRoute
                .produce(new DevConsoleRouteBuildItem("testServiceWithToken", "POST", testServiceWithToken, this.getClass()));
        devConsoleRoute
                .produce(new DevConsoleRouteBuildItem("exchangeCodeForTokens", "POST", exchangeCodeForTokens, this.getClass()));
        devConsoleRoute
                .produce(new DevConsoleRouteBuildItem("testService", "POST", passwordClientCredHandler, this.getClass()));
    }
}
