package com.example.brainwashwords;

public class Word {
    private String word;
    private boolean known;
    private String id;

    public Word(String word, boolean known, String id) {
        this.word = word;
        this.known = known;
        this.id = id;
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

    public String getId() {
        return id;
    }
}
