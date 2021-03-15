/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
