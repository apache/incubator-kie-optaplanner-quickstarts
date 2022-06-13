package org.acme.schooltimetabling.persistence;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;

import org.acme.schooltimetabling.domain.TimeTable;

@ApplicationScoped
public class TimeTableRepository {

    private TimeTable timeTable;

    public TimeTable get() {
        return timeTable;
    }

    public void update(TimeTable timeTable) {
        this.timeTable = Objects.requireNonNull(timeTable);
    }
}
