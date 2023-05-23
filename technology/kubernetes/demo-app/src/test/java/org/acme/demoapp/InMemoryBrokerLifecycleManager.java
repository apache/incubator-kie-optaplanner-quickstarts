package org.acme.demoapp;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import io.smallrye.reactive.messaging.memory.InMemoryConnector;

public class InMemoryBrokerLifecycleManager implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> solverRequestSinkProperties = InMemoryConnector.switchOutgoingChannelsToInMemory("solver_request");
        env.putAll(solverRequestSinkProperties);
        return env;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}

