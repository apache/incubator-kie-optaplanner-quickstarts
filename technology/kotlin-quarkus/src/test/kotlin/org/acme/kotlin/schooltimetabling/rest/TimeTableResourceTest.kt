package org.acme.kotlin.schooltimetabling.rest

import io.quarkus.test.junit.QuarkusTest
import org.acme.kotlin.schooltimetabling.domain.TimeTable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.optaplanner.core.api.solver.SolverStatus
import javax.inject.Inject


@QuarkusTest
class TimeTableResourceTest {

    @Inject
    lateinit var timeTableResource: TimeTableResource

    @Test
    @Timeout(600_000)
    @Throws(InterruptedException::class)
    fun solveDemoDataUntilFeasible() {
        timeTableResource.solve()
        var timeTable: TimeTable = timeTableResource.getTimeTable()
        do { // Use do-while to give the solver some time and avoid retrieving an early infeasible solution.
            // Quick polling (not a Test Thread Sleep anti-pattern)
            // Test is still fast on fast machines and doesn't randomly fail on slow machines.
            Thread.sleep(20L)
            timeTable = timeTableResource.getTimeTable()
        } while (timeTable.solverStatus != SolverStatus.NOT_SOLVING || !timeTable.score!!.isFeasible)
        Assertions.assertFalse(timeTable.lessonList.isEmpty())
        for (lesson in timeTable.lessonList) {
            Assertions.assertNotNull(lesson.timeslot)
            Assertions.assertNotNull(lesson.room)
        }
        Assertions.assertTrue(timeTable.score!!.isFeasible)
    }
    
}
