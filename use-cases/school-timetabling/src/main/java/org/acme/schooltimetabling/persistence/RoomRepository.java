package org.acme.schooltimetabling.persistence;

import javax.enterprise.context.ApplicationScoped;

import org.acme.schooltimetabling.domain.Room;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class RoomRepository implements PanacheRepository<Room> {

}
