package org.acme.kotlin.schooltimetabling.domain

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.lookup.PlanningId
import org.optaplanner.core.api.domain.variable.PlanningVariable
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne


@PlanningEntity
@Entity
class Lesson {

    @PlanningId
    @Id
    @GeneratedValue
    var id: Long? = null

    lateinit var subject: String
    lateinit var teacher: String
    lateinit var studentGroup: String

    @PlanningVariable
    @ManyToOne
    var timeslot: Timeslot? = null
    @PlanningVariable
    @ManyToOne
    var room: Room? = null

    // No-arg constructor required for Hibernate and OptaPlanner
    constructor()

    constructor(subject: String, teacher: String, studentGroup: String) {
        this.subject = subject.trim()
        this.teacher = teacher.trim()
        this.studentGroup = studentGroup.trim()
    }

    constructor(id: Long?, subject: String, teacher: String, studentGroup: String, timeslot: Timeslot?, room: Room?)
            : this(subject, teacher, studentGroup) {
        this.id = id
        this.timeslot = timeslot
        this.room = room
    }


    override fun toString(): String = "$subject($id)"

}
