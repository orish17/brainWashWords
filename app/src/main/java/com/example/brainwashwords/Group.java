package com.example.brainwashwords;

public class Group {
    private String id;
    private String name;

    // קונסטרקטור ריק - חובה בשביל Firebase
    public Group() {
    }

    // קונסטרקטור מלא
    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // קונסטרקטור עם שם בלבד
    public Group(String name) {
        this.name = name;
    }

    // גטרים וסטרים
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
