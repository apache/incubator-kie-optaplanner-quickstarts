package org.acme.schooltimetabling.rest;

import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.persistence.TimeslotRepository;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;

@ResourceProperties(path = "timeslots")
public interface TimeslotResource extends PanacheRepositoryResource<TimeslotRepository, Timeslot, Long> {

}
