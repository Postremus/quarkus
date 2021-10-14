package io.quarkus.resteasy.reactive.jaxb.deployment.test.sse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

import org.jboss.resteasy.reactive.RestSseElementType;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;

@Path("sse")
public class SseResource {

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void sse(Sse sse, SseEventSink sink) {
        if (sink == null) {
            throw new IllegalStateException("No client connected.");
        }
        SseBroadcaster sseBroadcaster = sse.newBroadcaster();

        sseBroadcaster.register(sink);
        sseBroadcaster.broadcast(sse.newEventBuilder().data("hello").build()).thenAccept(v -> sseBroadcaster.close());
    }

    @Path("multi")
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> multiText() {
        return Multi.createFrom().items("hello");
    }

    @Path("xml")
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_XML)
    public void sseXml(Sse sse, SseEventSink sink) {
        if (sink == null) {
            throw new IllegalStateException("No client connected.");
        }
        SseBroadcaster sseBroadcaster = sse.newBroadcaster();

        sseBroadcaster.register(sink);
        sseBroadcaster.broadcast(sse.newEventBuilder().data(new Message("hello")).build())
                .thenAccept(v -> sseBroadcaster.close());
    }

    @Blocking
    @Path("blocking/xml")
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_XML)
    public void blockingSseXml(Sse sse, SseEventSink sink) {
        if (sink == null) {
            throw new IllegalStateException("No client connected.");
        }
        SseBroadcaster sseBroadcaster = sse.newBroadcaster();

        sseBroadcaster.register(sink);
        sseBroadcaster.broadcast(sse.newEventBuilder().data(new Message("hello")).build())
                .thenAccept(v -> sseBroadcaster.close());
    }

    @Path("xml2")
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void sseXml2(Sse sse, SseEventSink sink) {
        if (sink == null) {
            throw new IllegalStateException("No client connected.");
        }
        SseBroadcaster sseBroadcaster = sse.newBroadcaster();

        // Same as sseXml but set mediaType in builder
        sseBroadcaster.register(sink);
        sseBroadcaster
                .broadcast(sse.newEventBuilder().data(new Message("hello")).mediaType(MediaType.APPLICATION_XML_TYPE).build())
                .thenAccept(v -> sseBroadcaster.close());
    }

}
