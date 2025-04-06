package com.example.brainwashwords;

public class Word {
    private String word;
    private boolean known;
    private String definition;
    private String id;
    private String groupId;

    public Word(String word, boolean known, String definition, String id, String groupId) {
        this.word = word;
        this.known = known;
        this.definition = definition;
        this.id = id;
        this.groupId = groupId;
    }

    public String getWord() {
        return word;
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }

    public String getDefinition() {
        return definition;
    }

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }
}
