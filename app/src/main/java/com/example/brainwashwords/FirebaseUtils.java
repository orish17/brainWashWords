// 🔹 FirebaseUtils.java – גרסה ללא FirebaseAuth

package com.example.brainwashwords;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for saving user test results to Firebase Realtime Database.
 * This version uses SharedPreferences to retrieve the user ID instead of FirebaseAuth.
 */
public class FirebaseUtils {

    /**
     * Saves the result of a test (success rate) for a specific user and test type into Firebase.
     *
     * @param context     The context used to access SharedPreferences.
     * @param testType    The type of test (e.g., "MultipleChoice", "AudioTest").
     * @param successRate The user's success rate in the test (as a float percentage, e.g., 85.0).
     */
    public static void saveTestResult(Context context, String testType, float successRate) {
        // ניגש ל-SharedPreferences כדי לקבל את מזהה המשתמש ששמור באפליקציה
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("uid", null); // שליפת המזהה (uid)

        // בדיקה אם המשתמש לא מחובר – במקרה כזה לא שומרים כלום
        if (userId == null) {
            Log.e("SAVE_TEST", "User ID not found in SharedPreferences");
            return;
        }

        // יצירת רפרנס לנתיב בבסיס הנתונים של Firebase:
        // users/{userId}/tests/{testType}
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("tests")
                .child(testType);

        // בניית מפת נתונים לשמירה: {"successRate": value}
        Map<String, Object> data = new HashMap<>();
        data.put("successRate", successRate);

        // שמירת הנתונים ב-Firebase (הנתונים נדרסים אם קיימים)
        ref.setValue(data);
    }
}
