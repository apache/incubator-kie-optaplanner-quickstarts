package org.acme.schooltimetabling.persistence;

import static org.acme.schooltimetabling.bootstrap.DemoDataGenerator.PROBLEM_ID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.TimeTable;
import org.optaplanner.SolutionRepository;

import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class TimeTableRepository implements SolutionRepository<TimeTable, Long> {

    @Inject
    TimeslotRepository timeslotRepository;
    @Inject
    RoomRepository roomRepository;
    @Inject
    LessonRepository lessonRepository;

    @Transactional
    @Override
    public TimeTable get(Long problemId) {
        if (!PROBLEM_ID.equals(problemId)) {
            throw new IllegalStateException("There is no timeTable with id (" + problemId + ").");
        }
        // Occurs in a single transaction, so each initialized lesson references the same timeslot/room instance
        // that is contained by the timeTable's timeslotList/roomList.
        return new TimeTable(
                timeslotRepository.listAll(Sort.by("dayOfWeek").and("startTime").and("endTime").and("id")),
                roomRepository.listAll(Sort.by("name").and("id")),
                lessonRepository.listAll(Sort.by("subject").and("teacher").and("studentGroup").and("id")));
    }

    @Transactional
    @Override
    public void put(Long problemId, TimeTable timeTable) {
        for (Lesson lesson : timeTable.getLessonList()) {
            // TODO this is awfully naive: optimistic locking causes issues if called by the SolverManager
            Lesson attachedLesson = lessonRepository.findById(lesson.getId());
            attachedLesson.setTimeslot(lesson.getTimeslot());
            attachedLesson.setRoom(lesson.getRoom());
        }
    }
}
