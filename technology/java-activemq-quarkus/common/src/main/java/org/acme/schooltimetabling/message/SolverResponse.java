package org.acme.schooltimetabling.message;

import org.acme.schooltimetabling.domain.TimeTable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SolverResponse {

    private Long problemId;
    private TimeTable timeTable;
    private ResponseStatus responseStatus;
    private ErrorInfo errorInfo;

    SolverResponse() {
        // Required for JSON deserialization.
    }

    public SolverResponse(Long problemId, TimeTable timeTable) {
        this.problemId = problemId;
        this.timeTable = timeTable;
        this.responseStatus = ResponseStatus.SUCCESS;
    }

    public SolverResponse(Long problemId, ErrorInfo errorInfo) {
        this.problemId = problemId;
        this.errorInfo = errorInfo;
        this.responseStatus = ResponseStatus.FAILURE;
    }

    public Long getProblemId() {
        return problemId;
    }

    public TimeTable getTimeTable() {
        return timeTable;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return ResponseStatus.SUCCESS == responseStatus;
    }

    public enum ResponseStatus {
        SUCCESS,
        FAILURE
    }

    public static class ErrorInfo {
        private String exceptionClassName;
        private String exceptionMessage;

        ErrorInfo() {
            // Required for JSON deserialization.
        }

        public ErrorInfo(String exceptionClassName, String exceptionMessage) {
            this.exceptionClassName = exceptionClassName;
            this.exceptionMessage = exceptionMessage;
        }

        public String getExceptionClassName() {
            return exceptionClassName;
        }

        public String getExceptionMessage() {
            return exceptionMessage;
        }
    }
}
