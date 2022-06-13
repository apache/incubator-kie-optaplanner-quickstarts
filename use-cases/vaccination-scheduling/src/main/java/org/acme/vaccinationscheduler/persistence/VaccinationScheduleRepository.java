package org.acme.vaccinationscheduler.persistence;

import javax.enterprise.context.ApplicationScoped;

import org.acme.vaccinationscheduler.domain.VaccinationSchedule;

@ApplicationScoped
public class VaccinationScheduleRepository {

    private VaccinationSchedule vaccinationSchedule;

    public VaccinationSchedule find() {
        return vaccinationSchedule;
    }

    public void save(VaccinationSchedule vaccinationSchedule) {
        this.vaccinationSchedule = vaccinationSchedule;
    }

}
