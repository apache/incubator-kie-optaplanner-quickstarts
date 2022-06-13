package org.acme.schooltimetabling.rest;

import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.persistence.RoomRepository;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;

@ResourceProperties(path = "rooms")
public interface RoomResource extends PanacheRepositoryResource<RoomRepository, Room, Long> {

}
