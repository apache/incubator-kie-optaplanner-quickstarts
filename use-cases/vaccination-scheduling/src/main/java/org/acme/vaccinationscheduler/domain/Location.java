package org.acme.vaccinationscheduler.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class Location {

    public double latitude;
    public double longitude;

    // No-arg constructor required for Jackson
    public Location() {}

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return String.format("[%.4fN, %.4fE]", latitude, longitude);
    }

}
