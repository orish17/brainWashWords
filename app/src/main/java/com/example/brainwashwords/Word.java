package com.example.brainwashwords;

public class Word {
    private String id;
    private String word;
    private boolean known;
    private String currentWorkout;

    // Constructor without parameters (needed for Firebase)
    public Word() {}

    public Word(String word, boolean known, String id) {
        this.word = word;
        this.known = known;
        this.id = id;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }

    public String getCurrentWorkout() {
        return currentWorkout;
    }

    public void setCurrentWorkout(String currentWorkout) {
        this.currentWorkout = currentWorkout;
    }
}