package org.acme.employeescheduling.domain;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverStatus;

@PlanningSolution
public class EmployeeSchedule {
    @ProblemFactCollectionProperty
    List<Availability> availabilityList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id="employeeRange")
    List<Employee> employeeList;

    @PlanningEntityCollectionProperty
    List<Shift> shiftList;

    @PlanningScore
    HardSoftScore score;

    ScheduleState scheduleState;

    SolverStatus solverStatus;

    // No-arg constructor required for OptaPlanner
    public EmployeeSchedule() {}

    public EmployeeSchedule(ScheduleState scheduleState, List<Availability> availabilityList, List<Employee> employeeList, List<Shift> shiftList) {
        this.scheduleState = scheduleState;
        this.availabilityList = availabilityList;
        this.employeeList = employeeList;
        this.shiftList = shiftList;
    }

    public ScheduleState getScheduleState() {
        return scheduleState;
    }

    public void setScheduleState(ScheduleState scheduleState) {
        this.scheduleState = scheduleState;
    }

    public List<Availability> getAvailabilityList() {
        return availabilityList;
    }

    public void setAvailabilityList(List<Availability> availabilityList) {
        this.availabilityList = availabilityList;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public List<Shift> getShiftList() {
        return shiftList;
    }

    public void setShiftList(List<Shift> shiftList) {
        this.shiftList = shiftList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
