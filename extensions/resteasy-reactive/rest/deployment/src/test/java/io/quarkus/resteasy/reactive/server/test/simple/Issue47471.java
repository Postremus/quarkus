package io.quarkus.resteasy.reactive.server.test.simple;

import static io.restassured.RestAssured.when;

import java.lang.reflect.InvocationTargetException;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

class Issue47471 {

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addClasses(
                            ParametersResource.class))
            .withAdditionalDependency((jar) -> jar
                    .addClasses(
                            StringType.class));

    @Test
    void basicTest() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

        when()
                .post("/parameters/stringTypePath/hello")
                .then()
                .body(Matchers.is("hello"));
    }

    @Path("/parameters")
    public static class ParametersResource {

        @Path("/stringTypePath/{stringValue}")
        @POST
        @Produces(MediaType.TEXT_PLAIN)
        public String stringTypePath(@PathParam("stringValue") final StringType stringValue) {
            return stringValue.getValue();
        }
    }

    public static class StringType {

        private String value;

        protected StringType() {
        }

        private StringType(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        protected static StringType fromString(final String s) {
            return new StringType(s);
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
