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

package org.acme.kotlin.schooltimetabling.rest

import io.quarkus.test.junit.QuarkusTest
import org.acme.kotlin.schooltimetabling.domain.TimeTable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.optaplanner.core.api.solver.SolverStatus
import javax.inject.Inject


@QuarkusTest
class TimeTableResourceTest {

    @Inject
    lateinit var timeTableResource: TimeTableResource

    @Test
    @Timeout(600000)
    @Throws(InterruptedException::class)
    fun solveDemoDataUntilFeasible() {
        timeTableResource.solve()
        var timeTable: TimeTable = timeTableResource.getTimeTable()
        while (timeTable.solverStatus != SolverStatus.NOT_SOLVING) {
            // Quick polling (not a Test Thread Sleep anti-pattern)
            // Test is still fast on fast machines and doesn't randomly fail on slow machines.
            Thread.sleep(20L)
            timeTable = timeTableResource.getTimeTable()
        }
        Assertions.assertFalse(timeTable.lessonList.isEmpty())
        for (lesson in timeTable.lessonList) {
            Assertions.assertNotNull(lesson.timeslot)
            Assertions.assertNotNull(lesson.room)
        }
        Assertions.assertTrue(timeTable.score!!.isFeasible)
    }
    
}
