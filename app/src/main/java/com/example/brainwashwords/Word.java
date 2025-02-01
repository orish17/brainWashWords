package com.example.brainwashwords;

public class Word {
    private String word;
    private String definition;
    private boolean known;
    private String id;
    private String groupId;

    // Constructor
    public Word(String word, String definition, boolean known, String id, String groupId) {
        this.word = word;
        this.definition = definition;
        this.known = known;
        this.id = id;
        this.groupId = groupId;
    }

    // Default constructor for Firebase
    public Word() {}

    // Getters and setters
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
