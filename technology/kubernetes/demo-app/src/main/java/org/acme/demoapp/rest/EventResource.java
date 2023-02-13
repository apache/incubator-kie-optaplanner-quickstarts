package org.acme.demoapp.rest;

import java.util.UUID;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

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
