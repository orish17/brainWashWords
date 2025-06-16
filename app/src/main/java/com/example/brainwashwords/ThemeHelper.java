package com.example.brainwashwords; // הצהרת החבילה שבה נמצאת המחלקה

import android.content.Context; // ייבוא קונטקסט – נדרש לגישה להעדפות
import android.content.SharedPreferences; // ייבוא של מערכת SharedPreferences
import androidx.appcompat.app.AppCompatDelegate; // ייבוא של AppCompatDelegate לניהול מצב תאורה

/**
 * מחלקת עזר לניהול מצב תאורה (כהה/בהיר) באפליקציה.
 * מאפשרת שמירה, טעינה והחלה של מצב התצוגה בהתאם להעדפות המשתמש.
 */
public class ThemeHelper { // תחילת המחלקה

    /**
     * מגדיר את מצב התאורה של האפליקציה (כהה או בהיר) ושומר אותו ב־SharedPreferences.
     *
     * @param context הקשר (Context) של האקטיביטי או האפליקציה
     * @param isNightMode true אם המשתמש רוצה מצב כהה, false אם מצב בהיר
     *
     * המתודה מבצעת שתי פעולות:
     * 1. מחליפה את מצב התאורה באפליקציה בזמן אמת.
     * 2. שומרת את הבחירה של המשתמש בזיכרון כדי לשחזר אותה בעתיד.
     */
    public static void setNightMode(Context context, boolean isNightMode) {
        // שינוי המצב הכללי של מצב תאורה – כהה או בהיר
        AppCompatDelegate.setDefaultNightMode(
                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // גישה ל־SharedPreferences בקובץ בשם settings
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        // שמירת העדפת המשתמש (כהה/בהיר) תחת המפתח "dark_mode"
        sharedPreferences.edit().putBoolean("dark_mode", isNightMode).apply();
    }

    /**
     * טוען את מצב התאורה האחרון שנשמר ומחיל אותו על האפליקציה.
     * אם לא נשמר ערך קודם – ייבחר מצב כהה כברירת מחדל.
     *
     * @param context הקשר של האקטיביטי או האפליקציה
     */
    public static void applySavedTheme(Context context) {
        // גישה להעדפות שמורות של המשתמש
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        // קריאה לערך שמור של מצב כהה, ברירת מחדל = true
        boolean isNightMode = sharedPreferences.getBoolean("dark_mode", true);

        // הפעלת מצב התאורה לפי הבחירה
        AppCompatDelegate.setDefaultNightMode(
                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
