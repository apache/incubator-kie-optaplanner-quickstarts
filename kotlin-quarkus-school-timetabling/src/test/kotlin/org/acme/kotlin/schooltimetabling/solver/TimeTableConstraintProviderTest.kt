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

package org.acme.kotlin.schooltimetabling.solver

import org.acme.kotlin.schooltimetabling.domain.Lesson
import org.acme.kotlin.schooltimetabling.domain.Room
import org.acme.kotlin.schooltimetabling.domain.TimeTable
import org.acme.kotlin.schooltimetabling.domain.Timeslot
import org.junit.jupiter.api.Test
import org.optaplanner.test.api.score.stream.ConstraintVerifier
import java.time.DayOfWeek
import java.time.LocalTime

internal class TimeTableConstraintProviderTest {
    
    val ROOM1: Room = Room(1, "Room1")
    val ROOM2: Room = Room(2, "Room2")
    val TIMESLOT1: Timeslot = Timeslot(1, DayOfWeek.MONDAY, LocalTime.NOON, LocalTime.NOON.plusMinutes(50))
    val TIMESLOT2: Timeslot = Timeslot(2, DayOfWeek.TUESDAY, LocalTime.NOON, LocalTime.NOON.plusMinutes(50))
    val TIMESLOT3: Timeslot = Timeslot(3, DayOfWeek.TUESDAY, LocalTime.NOON.plusHours(1), LocalTime.NOON.plusHours(1).plusMinutes(50))
    val TIMESLOT4: Timeslot = Timeslot(4, DayOfWeek.TUESDAY, LocalTime.NOON.plusHours(3), LocalTime.NOON.plusHours(3).plusMinutes(50))

    val constraintVerifier: ConstraintVerifier<TimeTableConstraintProvider, TimeTable> = ConstraintVerifier.build(TimeTableConstraintProvider(), TimeTable::class.java, Lesson::class.java)

    @Test
    fun roomConflict() {
        val firstLesson = Lesson(1, "Subject1", "Teacher1", "Group1", TIMESLOT1, ROOM1)
        val conflictingLesson = Lesson(2, "Subject2", "Teacher2", "Group2", TIMESLOT1, ROOM1)
        val nonConflictingLesson = Lesson(3, "Subject3", "Teacher3", "Group3", TIMESLOT2, ROOM1)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::roomConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1)
    }
    
    @Test
    fun teacherConflict() {
        val conflictingTeacher = "Teacher1"
        val firstLesson = Lesson(1, "Subject1", conflictingTeacher, "Group1", TIMESLOT1, ROOM1)
        val conflictingLesson = Lesson(2, "Subject2", conflictingTeacher, "Group2", TIMESLOT1, ROOM2)
        val nonConflictingLesson = Lesson(3, "Subject3", "Teacher2", "Group3", TIMESLOT2, ROOM1)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1)
    }

    @Test
    fun studentGroupConflict() {
        val conflictingGroup = "Group1"
        val firstLesson = Lesson(1, "Subject1", "Teacher1", conflictingGroup, TIMESLOT1, ROOM1)
        val conflictingLesson = Lesson(2, "Subject2", "Teacher2", conflictingGroup, TIMESLOT1, ROOM2)
        val nonConflictingLesson = Lesson(3, "Subject3", "Teacher3", "Group3", TIMESLOT2, ROOM1)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentGroupConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1)
    }

    @Test
    fun teacherRoomStability() {
        val teacher = "Teacher1"
        val lessonInFirstRoom = Lesson(1, "Subject1", teacher, "Group1", TIMESLOT1, ROOM1)
        val lessonInSameRoom = Lesson(2, "Subject2", teacher, "Group2", TIMESLOT1, ROOM1)
        val lessonInDifferentRoom = Lesson(3, "Subject3", teacher, "Group3", TIMESLOT1, ROOM2)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherRoomStability)
                .given(lessonInFirstRoom, lessonInDifferentRoom, lessonInSameRoom)
                .penalizesBy(2)
    }

    @Test
    fun teacherTimeEfficiency() {
        val teacher = "Teacher1"
        val singleLessonOnMonday = Lesson(1, "Subject1", teacher, "Group1", TIMESLOT1, ROOM1)
        val firstTuesdayLesson = Lesson(2, "Subject2", teacher, "Group2", TIMESLOT2, ROOM1)
        val secondTuesdayLesson = Lesson(3, "Subject3", teacher, "Group3", TIMESLOT3, ROOM1)
        val thirdTuesdayLessonWithGap = Lesson(4, "Subject4", teacher, "Group4", TIMESLOT4, ROOM1)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherTimeEfficiency)
                .given(singleLessonOnMonday, firstTuesdayLesson, secondTuesdayLesson, thirdTuesdayLessonWithGap)
                .rewardsWith(1) // Second tuesday lesson immediately follows the first.
    }

    @Test
    fun studentGroupSubjectVariety() {
        val studentGroup = "Group1"
        val repeatedSubject = "Subject1"
        val mondayLesson = Lesson(1, repeatedSubject, "Teacher1", studentGroup, TIMESLOT1, ROOM1)
        val firstTuesdayLesson = Lesson(2, repeatedSubject, "Teacher2", studentGroup, TIMESLOT2, ROOM1)
        val secondTuesdayLesson = Lesson(3, repeatedSubject, "Teacher3", studentGroup, TIMESLOT3, ROOM1)
        val thirdTuesdayLessonWithDifferentSubject = Lesson(4, "Subject2", "Teacher4", studentGroup, TIMESLOT4, ROOM1)
        val lessonInAnotherGroup = Lesson(5, repeatedSubject, "Teacher5", "Group2", TIMESLOT1, ROOM1)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentGroupSubjectVariety)
                .given(mondayLesson, firstTuesdayLesson, secondTuesdayLesson, thirdTuesdayLessonWithDifferentSubject,
                        lessonInAnotherGroup)
                .penalizesBy(1) // Second tuesday lesson immediately follows the first.
    }
    
}
