package org.acme.demoapp.generator;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.common.domain.Lesson;
import org.acme.common.domain.Room;
import org.acme.common.domain.TimeTable;
import org.acme.common.domain.Timeslot;

@ApplicationScoped
public final class DemoDataGenerator {

    private static final String[] ROOM_NAMES = new String[] {
            "Room A", "Room B", "Room C", "Room D", "Room E", "Room F", "Room G", "Room H", "Room I", "Room J"
    };

    private static final String[] SUBJECTS = new String[] {
            "Math", "Physics", "Chemistry", "Biology", "History", "English", "Art", "Drama", "ICT", "Physical education",
            "French", "Spanish", "Geography", "Geology"
    };

    private static final String[] STUDENT_GROUPS = new String[] {
            "9th grade", "10th grade", "11th grade", "12th grade"
    };

    private Map<String, String> subjectToTeacherMap;

    public DemoDataGenerator() {
        this.subjectToTeacherMap = createSubjectsToTeachersMapping();
    }

    private Map<String, String> createSubjectsToTeachersMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("ICT", "A. Turing");
        mapping.put("Math", "A. Turing");
        mapping.put("Chemistry", "M. Curie");
        mapping.put("French", "M. Curie");
        mapping.put("Physics", "M. Curie");
        mapping.put("Biology", "C. Darwin");
        mapping.put("Geography", "C. Darwin");
        mapping.put("Geology", "C. Darwin");
        mapping.put("Drama", "I. Jones");
        mapping.put("English", "I. Jones");
        mapping.put("History", "I. Jones");
        mapping.put("Art", "S. Dali");
        mapping.put("Physical education", "C. Lewis");
        mapping.put("Spanish", "P. Cruz");
        return mapping;
    }

    public TimeTable generateUnsolvedTimeTable(long problemId, int lessons, int rooms) {
        final int timeslotCount = (int) (lessons * 1.5 / rooms);
        List<Timeslot> timeslotList = generateTimeslots(problemId, timeslotCount);
        List<Lesson> lessonList = generateLessons(problemId, lessons);
        List<Room> roomList = generateRooms(problemId, rooms);
        return new TimeTable(timeslotList, roomList, lessonList);
    }

    List<Timeslot> generateTimeslots(long problemId, int count) {
        int sanitizedCount = count > 30 ? 30 : count; // Maximum of 30 timeslots per week.
        LocalTime startTime = LocalTime.of(8, 0);
        DayOfWeek startDay = DayOfWeek.MONDAY;
        DayOfWeek currentDay = startDay;
        LocalTime currentStartTime = startTime;
        List<Timeslot> timeslots = new ArrayList<>(sanitizedCount);
        for (int i = 0; i < sanitizedCount; i++) {
            Timeslot timeslot = new Timeslot(problemId, currentDay, currentStartTime, currentStartTime.plusHours(1));
            timeslots.add(timeslot);

            if (currentStartTime.getHour() == 10) { // Lunch break.
                currentStartTime = currentStartTime.plusHours(3);
            } else if (currentStartTime.getHour() == 15) { // The next day.
                currentStartTime = startTime;
                currentDay = currentDay.plus(1);
            } else {
                currentStartTime = currentStartTime.plusHours(1);
            }
        }

        return timeslots;
    }

    List<Room> generateRooms(long problemId, int count) {
        int sanitizedCount = count > ROOM_NAMES.length ? ROOM_NAMES.length : count;
        List<Room> rooms = new ArrayList<>(sanitizedCount);
        for (int i = 0; i < sanitizedCount; i++) {
            Room room = new Room(ROOM_NAMES[i]);
            room.setProblemId(problemId);
            rooms.add(room);
        }
        return rooms;
    }

    List<Lesson> generateLessons(long problemId, int count) {
        int sanitizedCount = count > 200 ? 200 : count;
        List<Lesson> lessons = new ArrayList<>(sanitizedCount);
        for (int i = 0; i < sanitizedCount; i++) {
            int subjectIndex = i % SUBJECTS.length;
            int studentGroupIndex = i % STUDENT_GROUPS.length;
            String subject = SUBJECTS[subjectIndex];
            String studentGroup = STUDENT_GROUPS[studentGroupIndex];
            Lesson lesson = new Lesson(problemId, subject, subjectToTeacherMap.get(subject), studentGroup);
            lessons.add(lesson);
        }
        return lessons;
    }
}
