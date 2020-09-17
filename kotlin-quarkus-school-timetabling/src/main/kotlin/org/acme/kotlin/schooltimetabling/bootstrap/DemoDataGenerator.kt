package org.acme.kotlin.schooltimetabling.bootstrap

import io.quarkus.runtime.StartupEvent
import org.acme.kotlin.schooltimetabling.domain.Lesson
import org.acme.kotlin.schooltimetabling.domain.Room
import org.acme.kotlin.schooltimetabling.domain.Timeslot
import org.acme.kotlin.schooltimetabling.persistence.LessonRepository
import org.acme.kotlin.schooltimetabling.persistence.RoomRepository
import org.acme.kotlin.schooltimetabling.persistence.TimeslotRepository
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.ArrayList
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.transaction.Transactional


@ApplicationScoped
class DemoDataGenerator {

    @Inject
    lateinit var timeslotRepository: TimeslotRepository
    @Inject
    lateinit var roomRepository: RoomRepository
    @Inject
    lateinit var lessonRepository: LessonRepository

    @ConfigProperty(name = "timeTable.demoData", defaultValue = "SMALL")
    var demoData: DemoData? = null

    @Transactional
    fun generateDemoData(@Observes startupEvent: StartupEvent) {
        if (demoData == DemoData.NONE) {
            return
        }

        val timeslotList: MutableList<Timeslot> = ArrayList(10)
        timeslotList.add(Timeslot(DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
        timeslotList.add(Timeslot(DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)))
        timeslotList.add(Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))
        timeslotList.add(Timeslot(DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)))
        timeslotList.add(Timeslot(DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))

        timeslotList.add(Timeslot(DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
        timeslotList.add(Timeslot(DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)))
        timeslotList.add(Timeslot(DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))
        timeslotList.add(Timeslot(DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)))
        timeslotList.add(Timeslot(DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))
        if (demoData == DemoData.LARGE) {
            timeslotList.add(Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
            timeslotList.add(Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)))
            timeslotList.add(Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))
            timeslotList.add(Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)))
            timeslotList.add(Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))
            timeslotList.add(Timeslot(DayOfWeek.THURSDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
            timeslotList.add(Timeslot(DayOfWeek.THURSDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)))
            timeslotList.add(Timeslot(DayOfWeek.THURSDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))
            timeslotList.add(Timeslot(DayOfWeek.THURSDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)))
            timeslotList.add(Timeslot(DayOfWeek.THURSDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))
            timeslotList.add(Timeslot(DayOfWeek.FRIDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)))
            timeslotList.add(Timeslot(DayOfWeek.FRIDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)))
            timeslotList.add(Timeslot(DayOfWeek.FRIDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)))
            timeslotList.add(Timeslot(DayOfWeek.FRIDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)))
            timeslotList.add(Timeslot(DayOfWeek.FRIDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)))
        }
        timeslotRepository.persist(timeslotList)


        val roomList: MutableList<Room> = ArrayList(3)
        roomList.add(Room("Room A"))
        roomList.add(Room("Room B"))
        roomList.add(Room("Room C"))
        if (demoData == DemoData.LARGE) {
            roomList.add(Room("Room D"))
            roomList.add(Room("Room E"))
            roomList.add(Room("Room F"))
        }
        roomRepository.persist(roomList)


        val lessonList: MutableList<Lesson> = ArrayList()
        lessonList.add(Lesson("Math", "A. Turing", "9th grade"))
        lessonList.add(Lesson("Math", "A. Turing", "9th grade"))
        lessonList.add(Lesson("Physics", "M. Curie", "9th grade"))
        lessonList.add(Lesson("Chemistry", "M. Curie", "9th grade"))
        lessonList.add(Lesson("Biology", "C. Darwin", "9th grade"))
        lessonList.add(Lesson("History", "I. Jones", "9th grade"))
        lessonList.add(Lesson("English", "I. Jones", "9th grade"))
        lessonList.add(Lesson("English", "I. Jones", "9th grade"))
        lessonList.add(Lesson("Spanish", "P. Cruz", "9th grade"))
        lessonList.add(Lesson("Spanish", "P. Cruz", "9th grade"))
        if (demoData == DemoData.LARGE) {
            lessonList.add(Lesson("Math", "A. Turing", "9th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "9th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "9th grade"))
            lessonList.add(Lesson("ICT", "A. Turing", "9th grade"))
            lessonList.add(Lesson("Physics", "M. Curie", "9th grade"))
            lessonList.add(Lesson("Geography", "C. Darwin", "9th grade"))
            lessonList.add(Lesson("Geology", "C. Darwin", "9th grade"))
            lessonList.add(Lesson("History", "I. Jones", "9th grade"))
            lessonList.add(Lesson("English", "I. Jones", "9th grade"))
            lessonList.add(Lesson("Drama", "I. Jones", "9th grade"))
            lessonList.add(Lesson("Art", "S. Dali", "9th grade"))
            lessonList.add(Lesson("Art", "S. Dali", "9th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "9th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "9th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "9th grade"))
        }

        lessonList.add(Lesson("Math", "A. Turing", "10th grade"))
        lessonList.add(Lesson("Math", "A. Turing", "10th grade"))
        lessonList.add(Lesson("Math", "A. Turing", "10th grade"))
        lessonList.add(Lesson("Physics", "M. Curie", "10th grade"))
        lessonList.add(Lesson("Chemistry", "M. Curie", "10th grade"))
        lessonList.add(Lesson("French", "M. Curie", "10th grade"))
        lessonList.add(Lesson("Geography", "C. Darwin", "10th grade"))
        lessonList.add(Lesson("History", "I. Jones", "10th grade"))
        lessonList.add(Lesson("English", "P. Cruz", "10th grade"))
        lessonList.add(Lesson("Spanish", "P. Cruz", "10th grade"))
        if (demoData == DemoData.LARGE) {
            lessonList.add(Lesson("Math", "A. Turing", "10th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "10th grade"))
            lessonList.add(Lesson("ICT", "A. Turing", "10th grade"))
            lessonList.add(Lesson("Physics", "M. Curie", "10th grade"))
            lessonList.add(Lesson("Biology", "C. Darwin", "10th grade"))
            lessonList.add(Lesson("Geology", "C. Darwin", "10th grade"))
            lessonList.add(Lesson("History", "I. Jones", "10th grade"))
            lessonList.add(Lesson("English", "P. Cruz", "10th grade"))
            lessonList.add(Lesson("English", "P. Cruz", "10th grade"))
            lessonList.add(Lesson("Drama", "I. Jones", "10th grade"))
            lessonList.add(Lesson("Art", "S. Dali", "10th grade"))
            lessonList.add(Lesson("Art", "S. Dali", "10th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "10th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "10th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "10th grade"))

            lessonList.add(Lesson("Math", "A. Turing", "11th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "11th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "11th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "11th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "11th grade"))
            lessonList.add(Lesson("ICT", "A. Turing", "11th grade"))
            lessonList.add(Lesson("Physics", "M. Curie", "11th grade"))
            lessonList.add(Lesson("Chemistry", "M. Curie", "11th grade"))
            lessonList.add(Lesson("French", "M. Curie", "11th grade"))
            lessonList.add(Lesson("Physics", "M. Curie", "11th grade"))
            lessonList.add(Lesson("Geography", "C. Darwin", "11th grade"))
            lessonList.add(Lesson("Biology", "C. Darwin", "11th grade"))
            lessonList.add(Lesson("Geology", "C. Darwin", "11th grade"))
            lessonList.add(Lesson("History", "I. Jones", "11th grade"))
            lessonList.add(Lesson("History", "I. Jones", "11th grade"))
            lessonList.add(Lesson("English", "P. Cruz", "11th grade"))
            lessonList.add(Lesson("English", "P. Cruz", "11th grade"))
            lessonList.add(Lesson("English", "P. Cruz", "11th grade"))
            lessonList.add(Lesson("Spanish", "P. Cruz", "11th grade"))
            lessonList.add(Lesson("Drama", "P. Cruz", "11th grade"))
            lessonList.add(Lesson("Art", "S. Dali", "11th grade"))
            lessonList.add(Lesson("Art", "S. Dali", "11th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "11th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "11th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "11th grade"))

            lessonList.add(Lesson("Math", "A. Turing", "12th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "12th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "12th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "12th grade"))
            lessonList.add(Lesson("Math", "A. Turing", "12th grade"))
            lessonList.add(Lesson("ICT", "A. Turing", "12th grade"))
            lessonList.add(Lesson("Physics", "M. Curie", "12th grade"))
            lessonList.add(Lesson("Chemistry", "M. Curie", "12th grade"))
            lessonList.add(Lesson("French", "M. Curie", "12th grade"))
            lessonList.add(Lesson("Physics", "M. Curie", "12th grade"))
            lessonList.add(Lesson("Geography", "C. Darwin", "12th grade"))
            lessonList.add(Lesson("Biology", "C. Darwin", "12th grade"))
            lessonList.add(Lesson("Geology", "C. Darwin", "12th grade"))
            lessonList.add(Lesson("History", "I. Jones", "12th grade"))
            lessonList.add(Lesson("History", "I. Jones", "12th grade"))
            lessonList.add(Lesson("English", "P. Cruz", "12th grade"))
            lessonList.add(Lesson("English", "P. Cruz", "12th grade"))
            lessonList.add(Lesson("English", "P. Cruz", "12th grade"))
            lessonList.add(Lesson("Spanish", "P. Cruz", "12th grade"))
            lessonList.add(Lesson("Drama", "P. Cruz", "12th grade"))
            lessonList.add(Lesson("Art", "S. Dali", "12th grade"))
            lessonList.add(Lesson("Art", "S. Dali", "12th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "12th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "12th grade"))
            lessonList.add(Lesson("Physical education", "C. Lewis", "12th grade"))
        }


        val lesson: Lesson = lessonList[0]
        lesson.timeslot = timeslotList[0]
        lesson.room = roomList[0]

        lessonRepository.persist(lessonList)
    }
    
    enum class DemoData {
        NONE, SMALL, LARGE
    }
}
