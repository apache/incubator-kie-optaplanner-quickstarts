package org.acme.demoapp;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;

public class KafkaTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> solverRequestSinkProperties = InMemoryConnector.switchOutgoingChannelsToInMemory("solver_request");
/*        Map<String, String> solverResponseSourceProperties =
                InMemoryConnector.switchIncomingChannelsToInMemory("solver_response");*/
        env.putAll(solverRequestSinkProperties);
//        env.putAll(solverResponseSourceProperties);
        return env;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}

