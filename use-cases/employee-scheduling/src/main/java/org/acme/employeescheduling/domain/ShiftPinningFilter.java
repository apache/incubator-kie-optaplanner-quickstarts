package org.acme.employeescheduling.domain;

import org.optaplanner.core.api.domain.entity.PinningFilter;

public class ShiftPinningFilter implements PinningFilter<EmployeeSchedule, Shift> {

    @Override
    public boolean accept(EmployeeSchedule employeeSchedule, Shift shift) {
        ScheduleState scheduleState = employeeSchedule.getScheduleState();
        return !scheduleState.isDraft(shift);
    }
}
