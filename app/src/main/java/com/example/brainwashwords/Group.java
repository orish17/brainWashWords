package com.example.brainwashwords;

/**
 * Represents a group of words (a workout list).
 * Each group can have an ID and a name.
 * This class is used for storing and retrieving group data, especially from Firebase.
 */
public class Group {

    // מזהה ייחודי של הקבוצה (יכול לשמש כמפתח ב-Firebase)
    private String id;

    // שם הקבוצה – יוצג למשתמש
    private String name;

    /**
     * Empty constructor required for Firebase automatic deserialization.
     */
    public Group() {
        // קונסטרקטור ריק – Firebase חייב אותו כדי ליצור מופעים של המחלקה
    }

    /**
     * Full constructor for setting both ID and name.
     *
     * @param id   The unique identifier of the group.
     * @param name The display name of the group.
     */
    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Constructor for creating a group with only a name.
     * Useful when the ID is generated later (e.g., by Firebase).
     *
     * @param name The display name of the group.
     */
    public Group(String name) {
        this.name = name;
    }

    /**
     * Returns the ID of the group.
     *
     * @return The group's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the group.
     *
     * @param id The ID to set for the group.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the group.
     *
     * @return The group's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the group.
     *
     * @param name The name to set for the group.
     */
    public void setName(String name) {
        this.name = name;
    }
}
