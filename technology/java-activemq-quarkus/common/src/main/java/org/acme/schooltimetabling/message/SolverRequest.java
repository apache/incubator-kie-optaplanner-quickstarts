package org.acme.schooltimetabling.message;

import org.acme.schooltimetabling.domain.TimeTable;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SolverRequest {
    private Long problemId;
    private TimeTable timeTable;

    SolverRequest() {
        // Required for JSON deserialization.
    }

    public SolverRequest(Long problemId, TimeTable timeTable) {
        this.problemId = problemId;
        this.timeTable = timeTable;
    }

    public Long getProblemId() {
        return problemId;
    }

    public TimeTable getTimeTable() {
        return timeTable;
    }
}
