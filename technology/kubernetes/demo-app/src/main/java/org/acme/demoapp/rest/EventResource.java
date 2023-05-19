package org.acme.demoapp.rest;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;

import jakarta.annotation.PreDestroy;

@Path("events")
@ApplicationScoped
public class EventResource {

    private Sse sse;

    private SseBroadcaster sseBroadcaster;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void listen(@Context SseEventSink eventSink, @Context Sse sse) {
        if (this.sseBroadcaster == null) {
            this.sse = sse;
            this.sseBroadcaster = sse.newBroadcaster();
        }
        this.sseBroadcaster.register(eventSink);
    }

    public void sendEvent(Long planningId) {
        if (sseBroadcaster != null) {
            sseBroadcaster.broadcast(sse.newEventBuilder()
                    .name("message")
                    .id(UUID.randomUUID().toString())
                    .mediaType(MediaType.TEXT_PLAIN_TYPE)
                    .reconnectDelay(3000)
                    .data(planningId)
                    .comment("solver job update")
                    .build());
        }
    }

    @PreDestroy
    public void closeBroadcaster() {
        if (sseBroadcaster != null) {
            sseBroadcaster.close();
        }
    }
}
