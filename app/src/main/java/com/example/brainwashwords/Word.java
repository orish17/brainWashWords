package com.example.brainwashwords;

public class Word {
    private String word;
    private boolean isKnown;
    private String definition;
    private String id;
    private String currentWorkout;

    public Word(String word, boolean isKnown, String definition, String id, String currentWorkout) {
        this.word = word;
        this.isKnown = isKnown;
        this.definition = definition;
        this.id = id;
        this.currentWorkout = currentWorkout;
    }

    // Getters
    public String getWord() {
        return word;
    }

    public boolean isKnown() {
        return isKnown;
    }

    public String getDefinition() {
        return definition;
    }

    public String getId() {
        return id;
    }

    public String getCurrentWorkout() {
        return currentWorkout;
    }

    // Setters
    public void setKnown(boolean known) {
        isKnown = known;
    }
}
