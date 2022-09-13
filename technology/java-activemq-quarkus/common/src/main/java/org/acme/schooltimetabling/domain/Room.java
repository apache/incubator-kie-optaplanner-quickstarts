package org.acme.schooltimetabling.domain;

import org.optaplanner.core.api.domain.lookup.PlanningId;

public class Room {

    @PlanningId
    private Long id;

    private String name;

    Room() {
        // Required for JSON deserialization.
    }

    public Room(long id, String name) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
