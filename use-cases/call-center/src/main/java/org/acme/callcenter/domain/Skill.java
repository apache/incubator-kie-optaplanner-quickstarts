package org.acme.callcenter.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Skill {

    ENGLISH("EN"),
    GERMAN("DE"),
    SPANISH("ES"),
    CAR_INSURANCE("Car insurance"),
    LIFE_INSURANCE("Life insurance"),
    PROPERTY_INSURANCE("Property insurance");

    private String name;

    Skill(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
