package com.example.brainwashwords;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * מחלקת עזר לניהול מצב תאורה (מצב כהה/בהיר) באפליקציה.
 * מאפשרת שמירה, טעינה והחלה של מצב התצוגה בהתאם להעדפות המשתמש.
 */
public class ThemeHelper {

    /**
     * מגדיר את מצב התאורה של האפליקציה (כהה או בהיר) ושומר אותו ב־SharedPreferences.
     *
     * @param context הקשר (Context) של האקטיביטי או האפליקציה
     * @param isNightMode true אם המשתמש רוצה מצב כהה, false אם מצב בהיר
     *
     * <p>המתודה מבצעת שתי פעולות:
     * 1. מחליפה את מצב התאורה באפליקציה בזמן אמת.
     * 2. שומרת את הבחירה של המשתמש בזיכרון כדי לשחזר אותה בפעם הבאה שהוא פותח את האפליקציה.</p>
     */
    public static void setNightMode(Context context, boolean isNightMode) {
        AppCompatDelegate.setDefaultNightMode(
                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("dark_mode", isNightMode).apply();
    }

    /**
     * טוען את מצב התאורה האחרון שנשמר ומחיל אותו על האפליקציה.
     * אם לא נשמר ערך קודם – מצב כהה מופעל כברירת מחדל.
     *
     * @param context הקשר של האקטיביטי או האפליקציה
     *
     * <p>המתודה הזאת צריכה להיקרא בתחילת onCreate של כל אקטיביטי שבו חשוב להחיל את ערכת העיצוב לפני שמוגדר setContentView.</p>
     */
    public static void applySavedTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("dark_mode", true); // כברירת מחדל מצב כהה
        AppCompatDelegate.setDefaultNightMode(
                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
