package com.example.brainwashwords; // הצהרת החבילה שבה נמצאת המחלקה

/**
 * מחלקה המייצגת מילה בודדת באפליקציה.
 * כל מילה כוללת טקסט, תרגום/הגדרה, סטטוס אם היא מוכרת, מזהה ייחודי, וקבוצת שייכות.
 */
public class Word {

    private String word; // המילה עצמה באנגלית, לדוגמה: "apple"
    private boolean known; // האם המשתמש סימן את המילה כ"מוכרת"
    private String definition; // תרגום או הסבר של המילה, לדוגמה: "תפוח"
    private String id; // מזהה ייחודי של המילה במסד הנתונים (למשל ב-Firebase)
    private String groupId; // מזהה של קבוצת המילים (workout) שאליה שייכת המילה

    /**
     * בנאי מלא – מאפשר ליצור מופע של Word עם כל השדות.
     *
     * @param word       טקסט המילה עצמה (למשל "banana")
     * @param known      האם המשתמש סימן אותה כמוכרת
     * @param definition תרגום/הסבר של המילה
     * @param id         מזהה ייחודי של המילה
     * @param groupId    מזהה הקבוצה (workout) שהמילה שייכת אליה
     */
    public Word(String word, boolean known, String definition, String id, String groupId) {
        this.word = word; // שומר את טקסט המילה
        this.known = known; // שומר אם היא מוכרת
        this.definition = definition; // שומר את ההגדרה
        this.id = id; // שומר את המזהה
        this.groupId = groupId; // שומר את מזהה הקבוצה
    }

    /** מחזיר את טקסט המילה */
    public String getWord() {
        return word;
    }

    /** מחזיר true אם המילה סומנה כ"מוכרת" על ידי המשתמש */
    public boolean isKnown() {
        return known;
    }

    /** קובע אם המילה מוכרת או לא לפי פרמטר */
    public void setKnown(boolean known) {
        this.known = known;
    }

    /** מחזיר את ההגדרה או התרגום של המילה */
    public String getDefinition() {
        return definition;
    }

    /** מחזיר את המזהה הייחודי של המילה */
    public String getId() {
        return id;
    }

    /** מחזיר את מזהה קבוצת המילים שהמילה שייכת אליה */
    public String getGroupId() {
        return groupId;
    }
}
