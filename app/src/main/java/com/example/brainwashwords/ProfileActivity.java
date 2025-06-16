package com.example.brainwashwords; // הגדרת המיקום של המחלקה בפרויקט

import android.content.SharedPreferences; // מאפשר גישה להגדרות מקומיות
import android.os.Bundle; // אובייקט שמעביר מידע בין מסכים
import android.view.View; // ניהול תצוגות (כפתורים, טקסטים וכו')
import android.widget.AdapterView; // מאזין לרשימות נגללות
import android.widget.ArrayAdapter; // מתאם לרשימות תפריט
import android.widget.Spinner; // תפריט נפתח
import android.widget.TextView; // תצוגת טקסט
import android.widget.Toast; // הודעת פופ־אפ למשתמש

import androidx.annotation.NonNull; // בדיקה שלא חוזר null

// ספרייה להצגת גרף עוגה
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.firebase.database.*; // עבודה עם Firebase Realtime Database

import java.util.ArrayList; // רשימה ניתנת להרחבה
import java.util.List; // ממשק לרשימות
import java.util.Map; // אוסף key-value

/**
 * ProfileActivity מציג את פרופיל המשתמש ואת סטטיסטיקות הביצועים שלו.
 * כולל:
 * - הצגת שם המשתמש
 * - תפריט בחירת סוג מבחן
 * - גרף עוגה עם אחוז הצלחה
 * - הודעת מוטיבציה מותאמת אישית
 */
public class ProfileActivity extends BaseActivity {

    private TextView usernameText, successRateText, motivationText; // תצוגות טקסט
    private Spinner testSelector; // תפריט בחירת מבחן
    private PieChart pieChart; // גרף עוגה
    private DatabaseReference userRef; // רפרנס למשתמש ב־Firebase
    private Map<String, TestResult> tests; // מפת מבחנים ותוצאות

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // הפעלת ערכת נושא
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // קביעת layout
        setupDrawer(); // הפעלת תפריט צד

        // קישור רכיבי UI מה־XML
        usernameText = findViewById(R.id.username_text);
        successRateText = findViewById(R.id.success_rate_text);
        motivationText = findViewById(R.id.motivation_text);
        testSelector = findViewById(R.id.test_selector);
        pieChart = findViewById(R.id.pie_chart);

        // שליפת מזהה המשתמש מה־SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("uid", null);

        // אם המשתמש לא התחבר – חזרה למסך הקודם
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // קישור למסלול של המשתמש במסד הנתונים
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // שליפת הנתונים של המשתמש
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class); // המרת JSON למחלקה User
                if (user != null) {
                    // הצגת שם משתמש לפי עדיפות: displayName > name
                    String nameToShow = user.getDisplayName() != null ? user.getDisplayName() : user.getName();
                    usernameText.setText("Hi, " + nameToShow); // הצגת שם

                    if (user.getTests() != null) {
                        tests = user.getTests(); // קבלת תוצאות מבחנים
                        setupTestSelector(); // בניית תפריט לבחירת מבחן
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * בונה את התפריט הנפתח (spinner) לבחירת מבחן.
     * לאחר בחירה, מוצגת תוצאה וגרף.
     */
    private void setupTestSelector() {
        List<String> testNames = new ArrayList<>(tests.keySet()); // שמות כל סוגי המבחנים
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, testNames);
        testSelector.setAdapter(adapter); // חיבור המתאם ל־spinner

        // מאזין לבחירה בתפריט
        testSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTest = testNames.get(position); // שם המבחן הנבחר
                TestResult result = tests.get(selectedTest); // שליפת תוצאה מהמפה

                if (result != null) {
                    updateChart(selectedTest, result.getSuccessRate()); // עדכון גרף ותצוגה
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // אם המשתמש לא בחר כלום – לא מתבצע כלום
            }
        });
    }

    /**
     * עדכון הגרף וטקסטים בהתאם לתוצאה שנבחרה.
     * @param testName שם המבחן
     * @param successRate אחוז ההצלחה של המשתמש במבחן זה
     */
    private void updateChart(String testName, float successRate) {
        // הצגת אחוז הצלחה במספרים
        successRateText.setText("Success Rate: " + (int) successRate + "%");

        // בניית נתונים לגרף: תשובות נכונות ושגויות
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(successRate, "Correct"));
        entries.add(new PieEntry(100 - successRate, "Incorrect"));

        // הגדרת הגרף – צבעים, גודל וכו'
        PieDataSet dataSet = new PieDataSet(entries, ""); // אין תווית כללית
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS); // צבעים שונים

        PieData data = new PieData(dataSet);
        data.setValueTextSize(18f); // גודל טקסט באחוזים

        pieChart.setData(data); // הכנסת הנתונים לגרף
        pieChart.setUsePercentValues(true); // הצגה באחוזים
        pieChart.getDescription().setEnabled(false); // ביטול תיאור ברירת מחדל
        pieChart.setCenterText(testName); // הצגת שם המבחן במרכז העוגה
        pieChart.setCenterTextSize(20f); // גודל הטקסט המרכזי
        pieChart.setDrawHoleEnabled(true); // חור באמצע העוגה

        // עיצוב מקרא (legend)
        Legend legend = pieChart.getLegend();
        legend.setTextSize(16f);
        legend.setXEntrySpace(24f);

        pieChart.invalidate(); // רענון הגרף

        // טקסט מוטיבציוני לפי רמת הצלחה
        if (successRate >= 80)
            motivationText.setText("🔥 Awesome!");
        else if (successRate >= 50)
            motivationText.setText("💪 Keep practicing!");
        else
            motivationText.setText("📚 Don’t give up!");
    }
}
