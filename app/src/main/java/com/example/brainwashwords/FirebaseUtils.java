// 🔹 FirebaseUtils.java – גרסה ללא שימוש ב־FirebaseAuth
package com.example.brainwashwords; // מציין שהקובץ שייך לחבילת הקוד הראשית של האפליקציה

import android.content.Context; // דרוש כדי לגשת למשאבים של האפליקציה (כמו SharedPreferences)
import android.content.SharedPreferences; // מאפשר שמירת נתונים פשוטים בזיכרון המקומי של האפליקציה
import android.util.Log; // מאפשר לכתוב הודעות ליומן (Logcat) לצורכי דיבוג

import com.google.firebase.database.DatabaseReference; // מציין מיקום ב־Realtime Database
import com.google.firebase.database.FirebaseDatabase; // מאפשר גישה לבסיס הנתונים בזמן אמת

import java.util.HashMap; // מבנה נתונים למיפוי key-value
import java.util.Map; // ממשק של HashMap

/**
 * מחלקת עזר לשמירת תוצאות מבחנים למסד הנתונים Firebase Realtime Database.
 * גרסה זו משתמשת ב־SharedPreferences כדי לזהות את המשתמש – ללא FirebaseAuth.
 */
public class FirebaseUtils {

    /**
     * שומר את תוצאת המבחן (אחוזי הצלחה) עבור משתמש ספציפי ולסוג מבחן מסוים.
     *
     * @param context     הקשר (Context) של האקטיביטי שממנו נקראה הפונקציה – משמש לשליפת SharedPreferences.
     * @param testType    סוג המבחן – לדוגמה "MultipleChoice", "AudioTest".
     * @param successRate האחוז שהמשתמש הצליח במבחן (float, לדוגמה 85.0).
     */
    public static void saveTestResult(Context context, String testType, float successRate) {

        // גישה ל־SharedPreferences ע״י שם הקובץ "UserPrefs"
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // שליפת מזהה המשתמש מה־SharedPreferences (המפתח הוא "uid")
        String userId = prefs.getString("uid", null);

        // אם לא מצאנו uid – אי אפשר לשמור כלום, נרשום שגיאה ונפסיק
        if (userId == null) {
            Log.e("SAVE_TEST", "User ID not found in SharedPreferences");
            return;
        }

        // יצירת רפרנס למיקום בבסיס הנתונים:
        // users/{userId}/tests/{testType}
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")     // טבלת המשתמשים
                .child(userId)             // משתמש נוכחי
                .child("tests")            // סעיף מבחנים
                .child(testType);          // סוג המבחן הספציפי

        // יצירת map עם הנתונים שרוצים לשמור – במקרה הזה, רק successRate
        Map<String, Object> data = new HashMap<>();
        data.put("successRate", successRate); // מפתח: "successRate", ערך: אחוז הצלחה

        // שמירה בפועל למסד הנתונים – פעולה זו תדרוס ערכים קיימים
        ref.setValue(data);
    }
}
