package com.example.brainwashwords;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ProfileActivity is responsible for displaying user profile data,
 * including test performance statistics retrieved from Firebase Realtime Database.
 *
 * It shows:
 * - Greeting with username
 * - Dropdown menu to select test type
 * - Pie chart with success rate (correct/incorrect)
 * - Motivational message based on performance
 */
public class ProfileActivity extends BaseActivity {

    private TextView usernameText, successRateText, motivationText;
    private Spinner testSelector;
    private PieChart pieChart;
    private DatabaseReference userRef;
    private Map<String, TestResult> tests;

    /**
     * Lifecycle method called when the activity is created.
     * Sets up the layout, retrieves user data, and populates the UI.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupDrawer(); // תפריט צד

        // אתחול רכיבי UI
        usernameText = findViewById(R.id.username_text);
        successRateText = findViewById(R.id.success_rate_text);
        motivationText = findViewById(R.id.motivation_text);
        testSelector = findViewById(R.id.test_selector);
        pieChart = findViewById(R.id.pie_chart);

        // שליפת UID מה־SharedPreferences (נשמר לאחר login)
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("uid", null);

        // אם לא נכנס למערכת, חזור למסך קודם
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // קישור לנתיב המשתמש במסד Realtime
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // שליפת נתוני המשתמש
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    // הצגת שם משתמש (אם יש displayName – השתמש בו)
                    String nameToShow = user.getDisplayName() != null ? user.getDisplayName() : user.getName();
                    usernameText.setText("Hi, " + nameToShow);

                    // בדיקה אם יש תוצאות מבחנים
                    if (user.getTests() != null) {
                        tests = user.getTests();
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
     * Initializes the dropdown menu (spinner) that allows the user to pick
     * a test name and then updates the pie chart accordingly.
     */
    private void setupTestSelector() {
        List<String> testNames = new ArrayList<>(tests.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, testNames);
        testSelector.setAdapter(adapter);

        testSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTest = testNames.get(position);
                TestResult result = tests.get(selectedTest);
                if (result != null) {
                    updateChart(selectedTest, result.getSuccessRate());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // לא נבחר כלום – אין פעולה
            }
        });
    }

    /**
     * Updates the pie chart and success message according to the selected test.
     * @param testName The name of the test selected.
     * @param successRate The user's success rate for the test (0-100).
     */
    private void updateChart(String testName, float successRate) {
        // הצגת אחוז הצלחה מספרי
        successRateText.setText("Success Rate: " + (int) successRate + "%");

        // יצירת נתוני הגרף
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(successRate, "Correct"));
        entries.add(new PieEntry(100 - successRate, "Incorrect"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(18f);

        // הגדרות גרף עוגה
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(testName); // שם המבחן באמצע העוגה
        pieChart.setCenterTextSize(20f);
        pieChart.setDrawHoleEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setTextSize(16f);
        legend.setXEntrySpace(24f);

        pieChart.invalidate(); // רענון

        // טקסט מוטיבציוני מותאם להצלחה
        if (successRate >= 80)
            motivationText.setText("\uD83D\uDD25 Awesome!");
        else if (successRate >= 50)
            motivationText.setText("\uD83D\uDCAA Keep practicing!");
        else
            motivationText.setText("\uD83D\uDCDA Don’t give up!");
    }
}
