package com.example.brainwashwords; // הגדרת שם החבילה שבה נמצאת המחלקה

/**
 * A model class for storing the result of a test/quiz. // מחלקת מודל לאחסון תוצאות מבחן
 * This class is used to save and retrieve test results from Firebase. // משמשת לשמירה ושליפה של תוצאה מ־Firebase
 */
public class TestResult { // תחילת הגדרת המחלקה

    /**
     * The success rate of the test, as a percentage (0-100). // משתנה המייצג אחוז הצלחה במבחן (0–100)
     */
    private int successRate; // אחוז הצלחה (כמספר שלם)

    /**
     * Default constructor required for Firebase deserialization. // קונסטרקטור ריק – Firebase צריך אותו
     * Firebase uses this constructor when converting data snapshots into objects. // בעת המרה מאובייקט JSON למחלקה
     */
    public TestResult() { // קונסטרקטור ברירת מחדל
    }

    /**
     * Constructor that initializes the object with a given success rate. // קונסטרקטור עם פרמטר אתחול
     *
     * @param successRate the percentage score achieved by the user in a test. // אחוז הצלחה שהמשתמש השיג
     */
    public TestResult(int successRate) { // קונסטרקטור שמקבל את אחוז ההצלחה
        this.successRate = successRate; // אתחול המשתנה עם הערך שהתקבל
    }

    /**
     * Returns the success rate of the test. // מחזיר את ערך אחוז ההצלחה
     *
     * @return an integer from 0 to 100 representing the user's performance. // מספר שלם בין 0 ל־100
     */
    public int getSuccessRate() { // פונקציה שמחזירה את אחוז ההצלחה
        return successRate; // מחזירה את הערך של successRate
    }

    /**
     * Sets the success rate of the test. // מאפשרת להגדיר/לעדכן את אחוז ההצלחה
     *
     * @param successRate the new percentage score to store. // הערך החדש שיישמר
     */
    public void setSuccessRate(int successRate) { // פונקציה שמעדכנת את אחוז ההצלחה
        this.successRate = successRate; // מגדירה את המשתנה עם הערך החדש
    }
}
