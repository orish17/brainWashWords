package com.example.brainwashwords; // הצהרת החבילה – מסדרת את המחלקה תחת שם האפליקציה

import java.util.Map; // ייבוא של Map – מאפשר לשמור רשימת מבחנים ותוצאותיהם עבור המשתמש

/**
 * מחלקה המייצגת משתמש באפליקציה.
 * כוללת פרטי זיהוי, תצוגה, וסיכומי מבחנים.
 */
public class User { // פתיחת המחלקה

    /** שם המשתמש (שם פרטי או מלא) */
    private String name; // שדה לשמירת השם המקורי של המשתמש

    /** כתובת האימייל של המשתמש */
    private String email; // שדה לשמירת כתובת האימייל

    /** סיסמת המשתמש (מאוחסנת זמנית – עדיף להצפין בעתיד) */
    private String password; // שדה לסיסמה (כרגע לא מוצפנת – לא מאובטח)

    /** שם תצוגה – ניתן לשינוי בנפרד מהשם המקורי */
    private String displayName; // שם תצוגה של המשתמש – נפרד מהשם המקורי, לדוגמה "אורי המלך"

    /** מפת מבחנים עם שם המבחן (מזהה) ותוצאה עבור כל אחד */
    private Map<String, TestResult> tests; // מפת תוצאות מבחנים, לדוגמה: {"MultipleChoice": TestResult, ...}

    /** קונסטרקטור ריק – דרוש עבור Firebase */
    public User() {} // קונסטרקטור ברירת מחדל – Firebase משתמש בו בזמן קריאת נתונים

    /**
     * קונסטרקטור עם פרמטרים בסיסיים.
     * @param name שם המשתמש
     * @param email כתובת אימייל
     * @param password סיסמה
     */
    public User(String name, String email, String password) {
        this.name = name; // שמירת השם
        this.email = email; // שמירת האימייל
        this.password = password; // שמירת הסיסמה
        this.displayName = name; // כברירת מחדל – שם התצוגה יהיה זהה לשם המקורי
    }

    /** @return השם המקורי של המשתמש */
    public String getName() { return name; } // מתודה להחזרת שם המשתמש

    /** @param name השם המקורי של המשתמש */
    public void setName(String name) { this.name = name; } // מתודה לעדכון שם המשתמש

    /** @return כתובת האימייל */
    public String getEmail() { return email; } // מחזיר את כתובת האימייל

    /** @param email כתובת האימייל */
    public void setEmail(String email) { this.email = email; } // מגדיר כתובת אימייל חדשה

    /** @return הסיסמה (לא מומלץ לשמור כך בייצור) */
    public String getPassword() { return password; } // מחזיר את הסיסמה (לא מאובטח)

    /** @param password הסיסמה של המשתמש */
    public void setPassword(String password) { this.password = password; } // מגדיר סיסמה חדשה

    /** @return שם התצוגה */
    public String getDisplayName() { return displayName; } // מחזיר את שם התצוגה

    /** @param displayName שם חדש לתצוגה */
    public void setDisplayName(String displayName) { this.displayName = displayName; } // מגדיר שם תצוגה חדש

    /** @return מפת מבחנים לפי סוג מבחן */
    public Map<String, TestResult> getTests() { return tests; } // מחזיר את מפת תוצאות המבחנים

    /** @param tests תוצאות מבחנים של המשתמש */
    public void setTests(Map<String, TestResult> tests) { this.tests = tests; } // מגדיר את מפת תוצאות המבחנים
}
