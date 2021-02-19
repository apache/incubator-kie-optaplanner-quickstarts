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

package org.acme.schooltimetabling.domain;

import java.util.List;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.acme.schooltimetabling.persistence.HardSoftScoreConverter;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class TimeTable {

    @Id
    @JsonIgnore
    private Long id;

    @Transient
    private List<Timeslot> timeslotList;

    @Transient
    private List<Room> roomList;

    @Transient
    private List<Lesson> lessonList;

    // TODO: switch to optaplanner-persistence-jpa once Quarkus upgrades to GraalVM 21.0.0
    //  https://github.com/oracle/graal/issues/460
    @Convert(converter = HardSoftScoreConverter.class)
    private HardSoftScore score;

    // Ignored by OptaPlanner, used by the UI to display solve or stop solving button
    private SolverStatus solverStatus;

    // No-arg constructor required for OptaPlanner
    public TimeTable() {
    }

    public TimeTable(Long id, List<Timeslot> timeslotList, List<Room> roomList, List<Lesson> lessonList) {
        this.id = id;
        this.timeslotList = timeslotList;
        this.roomList = roomList;
        this.lessonList = lessonList;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Timeslot> getTimeslotList() {
        return timeslotList;
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public List<Lesson> getLessonList() {
        return lessonList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setTimeslotList(List<Timeslot> timeslotList) {
        this.timeslotList = timeslotList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    public void setLessonList(List<Lesson> lessonList) {
        this.lessonList = lessonList;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
