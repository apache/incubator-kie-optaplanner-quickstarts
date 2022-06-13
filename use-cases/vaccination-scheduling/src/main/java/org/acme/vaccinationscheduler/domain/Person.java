package org.acme.vaccinationscheduler.domain;

import java.time.LocalDate;

import org.acme.vaccinationscheduler.domain.solver.PersonAssignment;
import org.acme.vaccinationscheduler.domain.solver.VaccinationSolution;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class Person {

    private String id;

    private String name;
    private Location homeLocation;
    private LocalDate birthdate;
    // Higher is scheduled earlier. For example: priorityRating = age + (healthcareWorker ? 1_000 : 0)
    private long priorityRating;

    // 1 for 1th dose, 2 for 2nd dose, etc
    private int doseNumber;
    // Typically used to enforce that the 2nd dose is the same vaccine as the first dose
    @JsonIdentityReference(alwaysAsId = true)
    private VaccineType requiredVaccineType = null;
    // Typically used if people can pick a favorite vaccine type
    @JsonIdentityReference(alwaysAsId = true)
    private VaccineType preferredVaccineType = null;
    // Typically used to enforce that the 2nd dose is injected at the same location as the first dose
    @JsonIdentityReference(alwaysAsId = true)
    private VaccinationCenter requiredVaccinationCenter = null;
    // Typically used to stimulate that the 2nd dose is injected at the same location as the first dose
    @JsonIdentityReference(alwaysAsId = true)
    private VaccinationCenter preferredVaccinationCenter = null;

    // Typically used to enforce that the 2nd dose is not injected too soon
    private LocalDate readyDate = null;
    // Typically used to enforce that the 2nd dose is not injected as close as possible to the ideal date
    private LocalDate idealDate = null;
    // Typically used to enforce that the 2nd dose is not injected too late
    private LocalDate dueDate = null;

    // Typically used to avoid changing invited and accepted appointments.
    // For example: pinned = (state == INVITED || state == ACCEPTED)
    private boolean pinned;

    // In this implementation, a planning window is at most 2 weeks,
    // so the 1st and 2nd dose won't be in the same planning window.
    // So one assignment suffices. Change the model to support multiple doses in the same planning window.

    /**
     * Translated to {@link PersonAssignment#getVaccinationSlot()} before solving and back again after solving.
     * See {@link VaccinationSolution#VaccinationSolution(VaccinationSchedule)} and {@link VaccinationSolution#toSchedule()}.
     */
    @JsonIdentityReference(alwaysAsId = true)
    private Appointment appointment;

    // No-arg constructor required for Jackson
    public Person() {}

    public Person(String id, String name, Location homeLocation, LocalDate birthdate, long priorityRating) {
        this(id, name, homeLocation, birthdate, priorityRating, 1, null, null, null, null, null, null, null);
    }

    public Person(String id, String name, Location homeLocation, LocalDate birthdate, long priorityRating,
            int doseNumber, VaccineType requiredVaccineType, VaccineType preferredVaccineType,
            VaccinationCenter requiredVaccinationCenter, VaccinationCenter preferredVaccinationCenter,
            LocalDate readyDate, LocalDate idealDate, LocalDate dueDate) {
        this.id = id;
        this.name = name;
        this.homeLocation = homeLocation;
        this.birthdate = birthdate;
        this.priorityRating = priorityRating;
        this.doseNumber = doseNumber;
        this.requiredVaccineType = requiredVaccineType;
        this.preferredVaccineType = preferredVaccineType;
        this.requiredVaccinationCenter = requiredVaccinationCenter;
        this.preferredVaccinationCenter = preferredVaccinationCenter;
        this.readyDate = readyDate;
        this.idealDate = idealDate;
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return name + "(" + id + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getHomeLocation() {
        return homeLocation;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public long getPriorityRating() {
        return priorityRating;
    }

    public int getDoseNumber() {
        return doseNumber;
    }

    public VaccineType getRequiredVaccineType() {
        return requiredVaccineType;
    }

    public VaccineType getPreferredVaccineType() {
        return preferredVaccineType;
    }

    public VaccinationCenter getRequiredVaccinationCenter() {
        return requiredVaccinationCenter;
    }

    public VaccinationCenter getPreferredVaccinationCenter() {
        return preferredVaccinationCenter;
    }

    public LocalDate getReadyDate() {
        return readyDate;
    }

    public LocalDate getIdealDate() {
        return idealDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

}
