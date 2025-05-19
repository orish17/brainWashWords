// 🔹 FirebaseUtils.java – גרסה ללא FirebaseAuth

package com.example.brainwashwords;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class FirebaseUtils {
    public static void saveTestResult(Context context, String testType, float successRate) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("uid", null);

        if (userId == null) {
            Log.e("SAVE_TEST", "User ID not found in SharedPreferences");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("tests")
                .child(testType);

        Map<String, Object> data = new HashMap<>();
        data.put("successRate", successRate);
        ref.setValue(data);
    }
}




