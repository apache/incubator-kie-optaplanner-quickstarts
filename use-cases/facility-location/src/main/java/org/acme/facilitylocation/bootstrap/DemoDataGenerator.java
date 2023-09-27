/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.acme.facilitylocation.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;
import org.acme.facilitylocation.persistence.FacilityLocationProblemRepository;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DemoDataGenerator {

    private final FacilityLocationProblemRepository repository;

    public DemoDataGenerator(FacilityLocationProblemRepository repository) {
        this.repository = repository;
    }

    public void generateDemoData(@Observes StartupEvent startupEvent) {
        FacilityLocationProblem problem = DemoDataBuilder.builder()
                .setCapacity(4500)
                .setDemand(900)
                .setFacilityCount(30)
                .setConsumerCount(60)
                .setSouthWestCorner(new Location(51.44, -0.16))
                .setNorthEastCorner(new Location(51.56, -0.01))
                .setAverageSetupCost(50_000)
                .setSetupCostStandardDeviation(10_000)
                .build();
        repository.update(problem);
    }
}
