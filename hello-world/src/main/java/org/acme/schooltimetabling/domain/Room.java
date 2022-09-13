package org.acme.schooltimetabling.domain;

public class Room {

    private String name;

    public Room(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

}
