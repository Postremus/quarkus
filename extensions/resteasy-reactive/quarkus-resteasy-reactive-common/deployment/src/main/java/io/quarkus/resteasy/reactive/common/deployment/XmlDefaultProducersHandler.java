package io.quarkus.resteasy.reactive.common.deployment;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.common.processor.DefaultProducesHandler;

public class XmlDefaultProducersHandler implements DefaultProducesHandler {

    private static final List<MediaType> PRODUCES_APPLICATION_XML = Collections.singletonList(MediaType.APPLICATION_XML_TYPE);

    @Override
    public List<MediaType> handle(Context context) {
        if (context.config().isDefaultProduces()) {
            return PRODUCES_APPLICATION_XML;
        }
        return Collections.emptyList();
    }
}
