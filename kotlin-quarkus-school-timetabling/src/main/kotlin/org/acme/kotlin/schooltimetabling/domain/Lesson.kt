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

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.lookup.PlanningId
import org.optaplanner.core.api.domain.variable.PlanningVariable
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne


@PlanningEntity
@Entity
class Lesson {

    @PlanningId
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    lateinit var subject: String
    lateinit var teacher: String
    lateinit var studentGroup: String

    @PlanningVariable(valueRangeProviderRefs = ["timeslotRange"])
    @ManyToOne
    var timeslot: Timeslot? = null
    @PlanningVariable(valueRangeProviderRefs = ["roomRange"])
    @ManyToOne
    var room: Room? = null

    // No-arg constructor required for Hibernate and OptaPlanner
    constructor()

    constructor(subject: String, teacher: String, studentGroup: String) {
        this.subject = subject.trim()
        this.teacher = teacher.trim()
        this.studentGroup = studentGroup.trim()
    }

    constructor(id: Long?, subject: String, teacher: String, studentGroup: String, timeslot: Timeslot?, room: Room?) {
        this.id = id
        this.subject = subject
        this.teacher = teacher
        this.studentGroup = studentGroup
        this.timeslot = timeslot
        this.room = room
    }


    override fun toString(): String = "$subject($id)"

}
