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
package org.acme.employeescheduling.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.Shift;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.optaplanner.core.api.solver.SolverStatus;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class EmployeeScheduleResourceTest {

    @Inject
    EmployeeScheduleResource employeeScheduleResource;

    @Test
    @Timeout(600_000)
    public void solveDemoDataUntilFeasible() throws InterruptedException {
        employeeScheduleResource.solve();
        EmployeeSchedule employeeSchedule = employeeScheduleResource.getSchedule();
        while (employeeSchedule.getSolverStatus() != SolverStatus.NOT_SOLVING
                || !employeeSchedule.getScore().isFeasible()) {
            // Quick polling (not a Test Thread Sleep anti-pattern)
            // Test is still fast on fast machines and doesn't randomly fail on slow machines.
            Thread.sleep(20L);
            employeeSchedule = employeeScheduleResource.getSchedule();
        }
        assertFalse(employeeSchedule.getShiftList().isEmpty());
        for (Shift shift : employeeSchedule.getShiftList()) {
            assertNotNull(shift.getEmployee());
        }
        assertTrue(employeeSchedule.getScore().isFeasible());
    }
}
