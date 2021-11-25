package org.acme.employeescheduling.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class ScheduleState {

    @Id
    Long tenantId;

    Integer publishNotice; // In number of days

    Integer draftLength; // In number of days

    LocalDate firstDraftDate;

    LocalDate lastHistoricDate;

    @JsonIgnore
    public boolean isHistoric(LocalDateTime dateTime) {
        return dateTime.isBefore(getFirstPublishedDate().atTime(LocalTime.MIDNIGHT));
    }

    @JsonIgnore
    public boolean isDraft(LocalDateTime dateTime) {
        return !dateTime.isBefore(getFirstDraftDate().atTime(LocalTime.MIDNIGHT));
    }

    @JsonIgnore
    public boolean isPublished(LocalDateTime dateTime) {
        return !isHistoric(dateTime) && !isDraft(dateTime);
    }

    @JsonIgnore
    public boolean isHistoric(Shift shift) {
        return isHistoric(shift.getStart());
    }

    @JsonIgnore
    public boolean isDraft(Shift shift) {
        return isDraft(shift.getStart());
    }

    @JsonIgnore
    public boolean isPublished(Shift shift) {
        return isPublished(shift.getStart());
    }

    @JsonIgnore
    public LocalDate getFirstPublishedDate() {
        return lastHistoricDate.plusDays(1);
    }

    @JsonIgnore
    public LocalDate getFirstUnplannedDate() {
        return firstDraftDate.plusDays(draftLength);
    }

    @JsonIgnore
    public LocalDate getPublishDeadline() {
        return firstDraftDate.minusDays(publishNotice);
    }

    // Simple getters/setters

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getPublishNotice() {
        return publishNotice;
    }

    public void setPublishNotice(Integer publishNotice) {
        this.publishNotice = publishNotice;
    }

    public Integer getDraftLength() {
        return draftLength;
    }

    public void setDraftLength(Integer draftLength) {
        this.draftLength = draftLength;
    }

    public LocalDate getFirstDraftDate() {
        return firstDraftDate;
    }

    public void setFirstDraftDate(LocalDate firstDraftDate) {
        this.firstDraftDate = firstDraftDate;
    }

    public LocalDate getLastHistoricDate() {
        return lastHistoricDate;
    }

    public void setLastHistoricDate(LocalDate lastHistoricDate) {
        this.lastHistoricDate = lastHistoricDate;
    }
}
