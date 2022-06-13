package org.acme.maintenancescheduling.persistence;

import javax.enterprise.context.ApplicationScoped;

import org.acme.maintenancescheduling.domain.Crew;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.acme.maintenancescheduling.domain.WorkCalendar;

@ApplicationScoped
public class WorkCalendarRepository implements PanacheRepository<WorkCalendar> {

}
