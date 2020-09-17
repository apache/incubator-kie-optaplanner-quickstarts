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

package org.acme.kotlin.schooltimetabling.domain

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty
import org.optaplanner.core.api.domain.solution.PlanningScore
import org.optaplanner.core.api.domain.solution.PlanningSolution
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.api.solver.SolverStatus

@PlanningSolution
class TimeTable {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeslotRange")
    lateinit var timeslotList: List<Timeslot>
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "roomRange")
    lateinit var roomList: List<Room>
    @PlanningEntityCollectionProperty
    lateinit var lessonList: List<Lesson>

    @PlanningScore
    var score: HardSoftScore? = null

    // Ignored by OptaPlanner, used by the UI to display solve or stop solving button
    var solverStatus: SolverStatus? = null

    constructor(timeslotList: List<Timeslot>, roomList: List<Room>, lessonList: List<Lesson>) {
        this.timeslotList = timeslotList
        this.roomList = roomList
        this.lessonList = lessonList
    }

    // No-arg constructor required for OptaPlanner
    constructor() {}

}
