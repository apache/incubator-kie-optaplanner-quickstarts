package org.acme.demoapp.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.acme.common.domain.Lesson;
import org.acme.common.domain.Room;
import org.acme.common.domain.TimeTable;
import org.acme.common.domain.Timeslot;
import org.junit.jupiter.api.Test;

class DemoDataGeneratorTest {

    @Test
    void generateRooms() {
        DemoDataGenerator demoDataGenerator = new DemoDataGenerator();
        List<Room> rooms = demoDataGenerator.generateRooms(1L,4);
        assertThat(rooms).hasSize(4);
        assertThat(rooms.get(0).getName()).isEqualTo("Room A");
        assertThat(rooms.get(3).getName()).isEqualTo("Room D");
    }

    @Test
    void generateLessons() {
        DemoDataGenerator demoDataGenerator = new DemoDataGenerator();
        List<Lesson> lessons = demoDataGenerator.generateLessons(1L,10);
        assertThat(lessons).hasSize(10);

        Lesson firstLesson = lessons.get(0);
        assertThat(firstLesson.getSubject()).isEqualTo("Math");
        assertThat(firstLesson.getTeacher()).isEqualTo("A. Turing");
        assertThat(firstLesson.getStudentGroup()).isEqualTo("9th grade");

        Lesson lastLesson = lessons.get(lessons.size() - 1);
        assertThat(lastLesson.getSubject()).isEqualTo("Physical education");
        assertThat(lastLesson.getTeacher()).isEqualTo("C. Lewis");
        assertThat(lastLesson.getStudentGroup()).isEqualTo("10th grade");
    }

    @Test
    void generateTimeslots() {
        DemoDataGenerator demoDataGenerator = new DemoDataGenerator();
        List<Timeslot> timeslots = demoDataGenerator.generateTimeslots(1L, 7);
        assertThat(timeslots).hasSize(7);

        Timeslot mondayMorning = timeslots.get(0);
        assertThat(mondayMorning.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(mondayMorning.getStartTime()).isEqualTo(LocalTime.of(8, 0));

        Timeslot mondayAfterLunch = timeslots.get(3);
        assertThat(mondayAfterLunch.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(mondayAfterLunch.getStartTime()).isEqualTo(LocalTime.of(13, 0));

        Timeslot mondaySecondAfterLunch = timeslots.get(4);
        assertThat(mondaySecondAfterLunch.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(mondaySecondAfterLunch.getStartTime()).isEqualTo(LocalTime.of(14, 0));

        Timeslot tuesdayMorning = timeslots.get(timeslots.size() - 1);
        assertThat(tuesdayMorning.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(tuesdayMorning.getStartTime()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    void generateUnsolvedTimeTable() {
        DemoDataGenerator demoDataGenerator = new DemoDataGenerator();
        final int lessons = 100;
        final int rooms = 5;
        TimeTable timeTable = demoDataGenerator.generateUnsolvedTimeTable(1L, lessons, rooms);
        assertThat(timeTable.getScore()).isNull();

        assertThat(timeTable.getLessonList()).hasSize(lessons);
        assertThat(timeTable.getRoomList()).hasSize(rooms);
        assertThat(timeTable.getTimeslotList().size() * rooms).isGreaterThan(lessons);
    }
}
