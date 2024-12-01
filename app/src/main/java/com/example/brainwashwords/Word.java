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

    // Getters and Setters
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

    public void setKnown(boolean known) {
        this.isKnown = known;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

}
