/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.acme.schooltimetabling.persistence;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.TimeTable;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class TimeTableRepository implements PanacheRepository<TimeTable> {

    @Inject
    TimeslotRepository timeslotRepository;
    @Inject
    RoomRepository roomRepository;
    @Inject
    LessonRepository lessonRepository;

    @Transactional
    public TimeTable load(Long id) {
        // Occurs in a single transaction, so each initialized lesson references the same timeslot/room instance
        // that is contained by the timeTable's timeslotList/roomList.
        TimeTable timeTable = findById(id);
        timeTable.setTimeslotList(timeslotRepository.listAll(Sort.by("dayOfWeek").and("startTime").and("endTime").and("id")));
        timeTable.setRoomList(roomRepository.listAll(Sort.by("name").and("id")));
        timeTable.setLessonList(lessonRepository.listAll(Sort.by("subject").and("teacher").and("studentGroup").and("id")));
        return timeTable;
    }

    @Transactional
    public void save(TimeTable timeTable) {
        for (Lesson lesson : timeTable.getLessonList()) {
            Lesson attachedLesson = lessonRepository.findById(lesson.getId());
            attachedLesson.setTimeslot(lesson.getTimeslot());
            attachedLesson.setRoom(lesson.getRoom());
        }
        TimeTable attachedTimeTable = findById(timeTable.getId());
        attachedTimeTable.setSolverStatus(timeTable.getSolverStatus());
        attachedTimeTable.setScore(timeTable.getScore());
    }
}
