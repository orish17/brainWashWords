package com.example.brainwashwords; // מציין שהמחלקה שייכת לחבילת הקוד הראשית של האפליקציה

/**
 * Group – מייצגת קבוצה של מילים (Workout).
 * כל קבוצה כוללת מזהה ייחודי ושם תצוגה.
 * המחלקה משמשת לשליפה ושמירה של קבוצות, בעיקר מול Firebase.
 */
public class Group {

    private String id;   // מזהה ייחודי של הקבוצה – יכול לשמש כמפתח במסד נתונים
    private String name; // שם הקבוצה – מוצג למשתמש באפליקציה

    /**
     * קונסטרקטור ריק – דרוש ל־Firebase כדי ליצור מופעים של האובייקט באופן אוטומטי.
     * בלי זה, Firebase לא יצליח לשלוף את הנתונים למסך.
     */
    public Group() {}

    /**
     * קונסטרקטור מלא – מאפשר ליצור קבוצה עם מזהה ושם.
     * שימושי כאשר יוצרים את האובייקט באופן ידני.
     *
     * @param id   מזהה הקבוצה (למשל מזהה Firebase)
     * @param name שם הקבוצה להצגה
     */
    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * קונסטרקטור עם שם בלבד – שימושי כאשר מזהה נוצר מאוחר יותר (לדוגמה ע"י Firebase).
     *
     * @param name שם הקבוצה
     */
    public Group(String name) {
        this.name = name;
    }

    /**
     * מחזיר את מזהה הקבוצה.
     *
     * @return מחרוזת המזהה
     */
    public String getId() {
        return id;
    }

    /**
     * מגדיר את מזהה הקבוצה.
     *
     * @param id מזהה חדש שיש להקצות
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * מחזיר את שם הקבוצה.
     *
     * @return מחרוזת שם הקבוצה
     */
    public String getName() {
        return name;
    }

    /**
     * מגדיר את שם הקבוצה.
     *
     * @param name שם חדש שיש להקצות
     */
    public void setName(String name) {
        this.name = name;
    }
}
