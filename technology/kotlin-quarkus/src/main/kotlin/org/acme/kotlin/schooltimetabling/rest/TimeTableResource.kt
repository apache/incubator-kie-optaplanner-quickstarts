package org.acme.kotlin.schooltimetabling.rest

import io.quarkus.panache.common.Sort
import org.acme.kotlin.schooltimetabling.domain.Lesson
import org.acme.kotlin.schooltimetabling.domain.TimeTable
import org.acme.kotlin.schooltimetabling.persistence.LessonRepository
import org.acme.kotlin.schooltimetabling.persistence.RoomRepository
import org.acme.kotlin.schooltimetabling.persistence.TimeslotRepository
import org.optaplanner.core.api.score.ScoreManager
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.api.solver.SolverManager
import org.optaplanner.core.api.solver.SolverStatus
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path


@Path("timeTable")
class TimeTableResource {

    val SINGLETON_TIME_TABLE_ID = 1L

    @Inject
    lateinit var timeslotRepository: TimeslotRepository
    @Inject
    lateinit var roomRepository: RoomRepository
    @Inject
    lateinit var lessonRepository: LessonRepository

    @Inject
    lateinit var solverManager: SolverManager<TimeTable, Long>
    @Inject
    lateinit var scoreManager: ScoreManager<TimeTable, HardSoftScore>

    // To try, open http://localhost:8080/timeTable
    @GET
    fun getTimeTable(): TimeTable {
        // Get the solver status before loading the solution
        // to avoid the race condition that the solver terminates between them
        val solverStatus = getSolverStatus()
        val solution: TimeTable = findById(SINGLETON_TIME_TABLE_ID)
        scoreManager.updateScore(solution) // Sets the score
        solution.solverStatus = solverStatus
        return solution
    }

    @POST
    @Path("solve")
    fun solve() {
        solverManager.solveAndListen(SINGLETON_TIME_TABLE_ID,
                this::findById,
                this::save)
    }

    fun getSolverStatus(): SolverStatus {
        return solverManager.getSolverStatus(SINGLETON_TIME_TABLE_ID)
    }

    @POST
    @Path("stopSolving")
    fun stopSolving() {
        solverManager.terminateEarly(SINGLETON_TIME_TABLE_ID)
    }

    @Transactional
    protected fun findById(id: Long): TimeTable {
        check(SINGLETON_TIME_TABLE_ID == id) { "There is no timeTable with id ($id)." }
        // Occurs in a single transaction, so each initialized lesson references the same timeslot/room instance
        // that is contained by the timeTable's timeslotList/roomList.
        return TimeTable(
                timeslotRepository.listAll(Sort.by("dayOfWeek").and("startTime").and("endTime").and("id")),
                roomRepository.listAll(Sort.by("name").and("id")),
                lessonRepository.listAll(Sort.by("subject").and("teacher").and("studentGroup").and("id")))
    }

    @Transactional
    protected fun save(timeTable: TimeTable) {
        for (lesson in timeTable.lessonList) {
            // TODO this is awfully naive: optimistic locking causes issues if called by the SolverManager
            val attachedLesson: Lesson = lessonRepository.findById(lesson.id!!)!!
            attachedLesson.timeslot = lesson.timeslot
            attachedLesson.room = lesson.room
        }
    }
    
}
