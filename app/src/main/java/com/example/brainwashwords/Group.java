package com.example.brainwashwords;

public class Group {
    private String id;
    private String name;

    public Group() {
        // Required for Firebase
    }

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // אם אתה משתמש רק בשם - אתה יכול להשתמש בזה גם:
    public Group(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
