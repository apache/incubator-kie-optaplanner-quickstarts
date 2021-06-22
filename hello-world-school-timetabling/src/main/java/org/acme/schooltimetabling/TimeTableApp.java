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

package org.acme.schooltimetabling;

import static java.util.stream.Collectors.groupingBy;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.solver.TimeTableConstraintProvider;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeTableApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTableApp.class);

    public static void main(String[] args) {
        SolverFactory<TimeTable> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(TimeTable.class)
                .withEntityClasses(Lesson.class)
                .withConstraintProviderClass(TimeTableConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(10)));

        // Load the problem
        TimeTable problem = generateDemoData();

        // Solve the problem
        Solver<TimeTable> solver = solverFactory.buildSolver();
        TimeTable solution = solver.solve(problem);

        // Visualize the solution
        printTimetable(solution);
    }

    public static TimeTable generateDemoData() {
        List<Timeslot> timeslotList = new ArrayList<>(10);
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        List<Room> roomList = new ArrayList<>(3);
        roomList.add(new Room("Room A"));
        roomList.add(new Room("Room B"));
        roomList.add(new Room("Room C"));

        List<Lesson> lessonList = new ArrayList<>();
        long id = 0;
        lessonList.add(new Lesson(id++, "Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson(id++, "Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson(id++, "Physics", "M. Curie", "9th grade"));
        lessonList.add(new Lesson(id++, "Chemistry", "M. Curie", "9th grade"));
        lessonList.add(new Lesson(id++, "Biology", "C. Darwin", "9th grade"));
        lessonList.add(new Lesson(id++, "History", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(id++, "English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(id++, "English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(id++, "Spanish", "P. Cruz", "9th grade"));
        lessonList.add(new Lesson(id++, "Spanish", "P. Cruz", "9th grade"));

        lessonList.add(new Lesson(id++, "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(id++, "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(id++, "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(id++, "Physics", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(id++, "Chemistry", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(id++, "French", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(id++, "Geography", "C. Darwin", "10th grade"));
        lessonList.add(new Lesson(id++, "History", "I. Jones", "10th grade"));
        lessonList.add(new Lesson(id++, "English", "P. Cruz", "10th grade"));
        lessonList.add(new Lesson(id++, "Spanish", "P. Cruz", "10th grade"));

        return new TimeTable(timeslotList, roomList, lessonList);
    }

    private static void printTimetable(TimeTable timeTable) {
        LOGGER.info("");
        List<Room> roomList = timeTable.getRoomList();
        List<Lesson> lessonList = timeTable.getLessonList();
        Map<Timeslot, Map<Room, List<Lesson>>> lessonMap = lessonList.stream()
                .filter(lesson -> lesson.getTimeslot() != null && lesson.getRoom() != null)
                .collect(groupingBy(Lesson::getTimeslot, groupingBy(Lesson::getRoom)));
        LOGGER.info("|            | " + roomList.stream()
                .map(room -> String.format("%-10s", room.getName())).collect(Collectors.joining(" | ")) + " |");
        LOGGER.info("|" + "------------|".repeat(roomList.size() + 1));
        for (Timeslot timeslot : timeTable.getTimeslotList()) {
            List<List<Lesson>> cellList = roomList.stream()
                    .map(room -> {
                        Map<Room, List<Lesson>> byRoomMap = lessonMap.get(timeslot);
                        if (byRoomMap == null) {
                            return Collections.<Lesson>emptyList();
                        }
                        List<Lesson> cellLessonList = byRoomMap.get(room);
                        if (cellLessonList == null) {
                            return Collections.<Lesson>emptyList();
                        }
                        return cellLessonList;
                    })
                    .collect(Collectors.toList());

            LOGGER.info("| " + String.format("%-10s",
                    timeslot.getDayOfWeek().toString().substring(0, 3) + " " + timeslot.getStartTime()) + " | "
                    + cellList.stream().map(cellLessonList -> String.format("%-10s",
                            cellLessonList.stream().map(Lesson::getSubject).collect(Collectors.joining(", "))))
                            .collect(Collectors.joining(" | "))
                    + " |");
            LOGGER.info("|            | "
                    + cellList.stream().map(cellLessonList -> String.format("%-10s",
                            cellLessonList.stream().map(Lesson::getTeacher).collect(Collectors.joining(", "))))
                            .collect(Collectors.joining(" | "))
                    + " |");
            LOGGER.info("|            | "
                    + cellList.stream().map(cellLessonList -> String.format("%-10s",
                            cellLessonList.stream().map(Lesson::getStudentGroup).collect(Collectors.joining(", "))))
                            .collect(Collectors.joining(" | "))
                    + " |");
            LOGGER.info("|" + "------------|".repeat(roomList.size() + 1));
        }
        List<Lesson> unassignedLessons = lessonList.stream()
                .filter(lesson -> lesson.getTimeslot() == null || lesson.getRoom() == null)
                .collect(Collectors.toList());
        if (!unassignedLessons.isEmpty()) {
            LOGGER.info("");
            LOGGER.info("Unassigned lessons");
            for (Lesson lesson : unassignedLessons) {
                LOGGER.info("  " + lesson.getSubject() + " - " + lesson.getTeacher() + " - " + lesson.getStudentGroup());
            }
        }
    }

}
