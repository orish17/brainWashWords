package com.example.brainwashwords;

/**
 * Represents a single word in the app.
 * Each word has a definition, an ID, a group ID, and a "known" flag indicating whether the user knows it.
 */
public class Word {

    // המילה עצמה (באנגלית)
    private String word;

    // האם המשתמש סימן את המילה כ"מוכרת"
    private boolean known;

    // ההגדרה או התרגום של המילה
    private String definition;

    // מזהה ייחודי של המילה (לשימוש פנימי/ב-Firebase)
    private String id;

    // מזהה הקבוצה שאליה שייכת המילה
    private String groupId;

    /**
     * Full constructor for creating a Word object with all fields.
     *
     * @param word       The word itself (e.g., "apple").
     * @param known      Whether the user marked it as known.
     * @param definition The word's meaning or translation.
     * @param id         Unique ID for this word.
     * @param groupId    ID of the group (workout) this word belongs to.
     */
    public Word(String word, boolean known, String definition, String id, String groupId) {
        this.word = word;
        this.known = known;
        this.definition = definition;
        this.id = id;
        this.groupId = groupId;
    }

    /**
     * Gets the word (text).
     *
     * @return The word string.
     */
    public String getWord() {
        return word;
    }

    /**
     * Returns whether the word is marked as known by the user.
     *
     * @return true if known, false otherwise.
     */
    public boolean isKnown() {
        return known;
    }

    /**
     * Sets the known status of the word.
     *
     * @param known Whether the word is known or not.
     */
    public void setKnown(boolean known) {
        this.known = known;
    }

    /**
     * Gets the definition or translation of the word.
     *
     * @return The definition string.
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Gets the unique ID of the word.
     *
     * @return The word's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the ID of the group to which the word belongs.
     *
     * @return The group ID.
     */
    public String getGroupId() {
        return groupId;
    }
}
