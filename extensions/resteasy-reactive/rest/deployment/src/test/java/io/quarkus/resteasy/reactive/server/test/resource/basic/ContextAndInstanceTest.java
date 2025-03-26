package io.quarkus.resteasy.reactive.server.test.resource.basic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.Unremovable;
import io.quarkus.test.QuarkusUnitTest;

public class ContextAndInstanceTest {

    @RegisterExtension
    static QuarkusUnitTest testExtension = new QuarkusUnitTest()
            .setArchiveProducer(new Supplier<>() {
                @Override
                public JavaArchive get() {
                    JavaArchive war = ShrinkWrap.create(JavaArchive.class);
                    war.addClasses(GreetingGenerator.class);
                    war.addClasses(GermanGreetingGenerator.class);
                    war.addClasses(GreetingGeneratorInt.class);
                    war.addClasses(GermanGreetingGeneratorInt.class);
                    return war;
                }
            });

    @Test
    void testContextOnSubclassOfAbstract() {
        {
            Instance<GreetingGenerator> greetingGenerators = CDI.current()
                    .select(ContextAndInstanceTest.GreetingGenerator.class);
            assertThat(greetingGenerators.isResolvable(), equalTo(true));
        }
    }

    @Test
    void testContextOnImplOfInterface() {
        {
            Instance<GreetingGeneratorInt> greetingGenerators = CDI.current()
                    .select(ContextAndInstanceTest.GreetingGeneratorInt.class);
            assertThat(greetingGenerators.isResolvable(), equalTo(true));
        }
    }

    public abstract static class GreetingGenerator {
    }

    @Unremovable
    @ApplicationScoped
    public static class GermanGreetingGenerator extends GreetingGenerator {

        @Context
        HttpHeaders headers;
    }

    public interface GreetingGeneratorInt {
    }

    @Unremovable
    @ApplicationScoped
    public static class GermanGreetingGeneratorInt implements GreetingGeneratorInt {

        @Context
        HttpHeaders headers;
    }
}
