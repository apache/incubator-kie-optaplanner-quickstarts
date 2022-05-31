package org.acme.demoapp.rest;

public class Dataset {
    private long problemId;
    private int lessons;
    private int rooms;
    private boolean solved;

    private String score = null;

    public Dataset() {
    }

    public Dataset(long problemId, int lessons, int rooms, boolean solved) {
        this.problemId = problemId;
        this.lessons = lessons;
        this.rooms = rooms;
        this.solved = solved;
    }

    public long getProblemId() {
        return problemId;
    }

    public void setProblemId(long problemId) {
        this.problemId = problemId;
    }

    public int getLessons() {
        return lessons;
    }

    public void setLessons(int lessons) {
        this.lessons = lessons;
    }

    public int getRooms() {
        return rooms;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
