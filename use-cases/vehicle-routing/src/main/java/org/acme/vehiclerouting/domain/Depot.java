package org.acme.vehiclerouting.domain;

public class Depot {

    private final long id;
    private final Location location;

    public Depot(long id, Location location) {
        this.id = id;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }
}
