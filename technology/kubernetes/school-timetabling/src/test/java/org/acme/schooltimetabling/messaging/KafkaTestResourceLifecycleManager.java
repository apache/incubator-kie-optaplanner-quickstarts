package org.acme.schooltimetabling.messaging;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;

public class KafkaTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> solverInProperties =
                InMemoryConnector.switchIncomingChannelsToInMemory("solver_in");
        Map<String, String> solverOutProperties = InMemoryConnector.switchOutgoingChannelsToInMemory("solver_out");
        env.putAll(solverInProperties);
        env.putAll(solverOutProperties);
        return env;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}

