package org.acme.vaccinationscheduler.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class Appointment {

    @JsonIdentityReference(alwaysAsId = true)
    private VaccinationCenter vaccinationCenter;
    private String boothId;
    private LocalDateTime dateTime;
    @JsonIdentityReference(alwaysAsId = true)
    private VaccineType vaccineType;

    // No-arg constructor required for Jackson
    public Appointment() {}

    public Appointment(VaccinationCenter vaccinationCenter, String boothId, LocalDateTime dateTime, VaccineType vaccineType) {
        this.vaccinationCenter = vaccinationCenter;
        this.boothId = boothId;
        this.dateTime = dateTime;
        this.vaccineType = vaccineType;
    }

    @Override
    public String toString() {
        return vaccinationCenter + "-" + boothId + "@" + dateTime;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public VaccinationCenter getVaccinationCenter() {
        return vaccinationCenter;
    }

    public String getBoothId() {
        return boothId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public VaccineType getVaccineType() {
        return vaccineType;
    }

}
