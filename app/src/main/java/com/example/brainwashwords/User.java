package com.example.brainwashwords;

import java.util.Map;

/**
 * מחלקה המייצגת משתמש באפליקציה.
 * כוללת פרטי זיהוי, תצוגה, וסיכומי מבחנים.
 */
public class User {

    /** שם המשתמש (שם פרטי או מלא) */
    private String name;

    /** כתובת האימייל של המשתמש */
    private String email;

    /** סיסמת המשתמש (מאוחסנת זמנית – עדיף להצפין בעתיד) */
    private String password;

    /** שם תצוגה – ניתן לשינוי בנפרד מהשם המקורי */
    private String displayName;

    /** מפת מבחנים עם שם המבחן (מזהה) ותוצאה עבור כל אחד */
    private Map<String, TestResult> tests;

    /** קונסטרקטור ריק – דרוש עבור Firebase */
    public User() {}

    /**
     * קונסטרקטור עם פרמטרים בסיסיים.
     * @param name שם המשתמש
     * @param email כתובת אימייל
     * @param password סיסמה
     */
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.displayName = name; // ברירת מחדל: תצוגה לפי השם המקורי
    }

    /** @return השם המקורי של המשתמש */
    public String getName() { return name; }

    /** @param name השם המקורי של המשתמש */
    public void setName(String name) { this.name = name; }

    /** @return כתובת האימייל */
    public String getEmail() { return email; }

    /** @param email כתובת האימייל */
    public void setEmail(String email) { this.email = email; }

    /** @return הסיסמה (לא מומלץ לשמור כך בייצור) */
    public String getPassword() { return password; }

    /** @param password הסיסמה של המשתמש */
    public void setPassword(String password) { this.password = password; }

    /** @return שם התצוגה */
    public String getDisplayName() { return displayName; }

    /** @param displayName שם חדש לתצוגה */
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    /** @return מפת מבחנים לפי סוג מבחן */
    public Map<String, TestResult> getTests() { return tests; }

    /** @param tests תוצאות מבחנים של המשתמש */
    public void setTests(Map<String, TestResult> tests) { this.tests = tests; }
}
