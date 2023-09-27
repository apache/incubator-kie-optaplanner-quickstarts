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
package org.acme.common.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.acme.common.domain.Lesson;
import org.acme.common.domain.TimeTable;

@ApplicationScoped
public class TimeTableRepository {

    private final RoomRepository roomRepository;
    private final TimeslotRepository timeslotRepository;
    private final LessonRepository lessonRepository;

    @Inject
    public TimeTableRepository(RoomRepository roomRepository, TimeslotRepository timeslotRepository, LessonRepository lessonRepository) {
        this.roomRepository = roomRepository;
        this.timeslotRepository = timeslotRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional
    public void persist(TimeTable timeTable) {
        timeslotRepository.persist(timeTable.getTimeslotList());
        roomRepository.persist(timeTable.getRoomList());
        lessonRepository.persist(timeTable.getLessonList());
    }

    @Transactional
    public void save(Long problemId, TimeTable timeTable) {
        for (Lesson lesson : timeTable.getLessonList()) {
            Lesson attachedLesson = lessonRepository.findById(lesson.getId());
            attachedLesson.setTimeslot(lesson.getTimeslot());
            attachedLesson.setRoom(lesson.getRoom());
        }
    }

    @Transactional
    public TimeTable load(Long problemId) {
        return new TimeTable(
                timeslotRepository.listAllByProblemId(problemId),
                roomRepository.listAllByProblemId(problemId),
                lessonRepository.listAllByProblemId(problemId));
    }
}
