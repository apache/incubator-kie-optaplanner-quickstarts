/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.vehiclerouting.solver;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.vehiclerouting.bootstrap.DemoDataBuilder;
import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.solver.SolverManager;

@QuarkusTest
public class SolverManagerTest {

    @Inject
    SolverManager<VehicleRoutingSolution, Long> solverManager;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        VehicleRoutingSolution problem = DemoDataBuilder.builder().setMinDemand(1).setMaxDemand(2)
                .setVehicleCapacity(15).setCustomerCount(77).setVehicleCount(6).setDepotCount(2)
                .setSouthWestCorner(new Location(0L, 43.751466, 11.177210))
                .setNorthEastCorner(new Location(0L, 43.809291, 11.290195)).build();
        solverManager.solve(0L, id -> problem, SolverManagerTest::printSolution).getFinalBestSolution();
    }

    static void printSolution(VehicleRoutingSolution solution) {
        solution.getVehicleList().forEach(vehicle -> System.out.println(vehicle.toString()));
    }
}
