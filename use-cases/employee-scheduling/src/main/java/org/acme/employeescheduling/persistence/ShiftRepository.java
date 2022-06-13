package org.acme.employeescheduling.persistence;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.acme.employeescheduling.domain.Shift;

@ApplicationScoped
public class ShiftRepository implements PanacheRepository<Shift> {

}
