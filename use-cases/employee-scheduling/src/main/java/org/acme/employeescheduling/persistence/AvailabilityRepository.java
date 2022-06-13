package org.acme.employeescheduling.persistence;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.acme.employeescheduling.domain.Availability;

@ApplicationScoped
public class AvailabilityRepository implements PanacheRepository<Availability> {

}
