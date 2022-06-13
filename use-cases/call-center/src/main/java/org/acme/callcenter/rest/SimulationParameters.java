package org.acme.callcenter.rest;

public class SimulationParameters {
    private int frequency;
    private int duration;

    public SimulationParameters() {
        // Required by Jackson.
    }

    public SimulationParameters(int frequency, int duration) {
        this.frequency = frequency;
        this.duration = duration;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getDuration() {
        return duration;
    }
}
