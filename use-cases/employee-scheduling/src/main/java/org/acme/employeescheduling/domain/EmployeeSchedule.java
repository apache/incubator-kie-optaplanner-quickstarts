package org.acme.employeescheduling.domain;

import java.time.LocalDate;
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

    LocalDate fromDate;
    LocalDate toDate;

    SolverStatus solverStatus;

    public EmployeeSchedule() {}

    public EmployeeSchedule(List<Availability> availabilityList, List<Employee> employeeList, List<Shift> shiftList) {
        this.availabilityList = availabilityList;
        this.employeeList = employeeList;
        this.shiftList = shiftList;
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

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public void computeFromAndToDates() {
        if (shiftList == null || shiftList.isEmpty()) {
            fromDate = null;
            toDate = null;
            return;
        }
        fromDate = shiftList.get(0).getStart().toLocalDate();
        toDate = shiftList.get(0).getEnd().toLocalDate();
        for (Shift shift : shiftList.subList(1, shiftList.size())) {
            LocalDate shiftStart = shift.getStart().toLocalDate();
            LocalDate shiftEnd = shift.getEnd().toLocalDate();

            if (shiftStart.isBefore(fromDate)) {
                fromDate = shiftStart;
            }

            if (shiftEnd.isAfter(toDate)) {
                toDate = shiftEnd;
            }
        }
    }
}
