package org.acme.employeescheduling.persistence;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.acme.employeescheduling.domain.ScheduleState;

@ApplicationScoped
public class ScheduleStateRepository implements PanacheRepository<ScheduleState> {

}
