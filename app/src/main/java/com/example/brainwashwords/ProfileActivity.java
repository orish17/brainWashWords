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
import androidx.appcompat.app.AppCompatDelegate;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private TextView usernameText, successRateText, motivationText;
    private Spinner testSelector;
    private PieChart pieChart;
    private DatabaseReference userRef;
    private Map<String, TestResult> tests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupDrawer();

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("uid", null);



        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    // ✅ מציג את displayName אם קיים, אחרת נופל חזרה ל-name
                    String nameToShow = user.getDisplayName() != null ? user.getDisplayName() : user.getName();
                    usernameText.setText("Hi, " + nameToShow);

                    if (user.getTests() != null) {
                        tests = user.getTests();
                        setupTestSelector();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateChart(String testName, float successRate) {
        successRateText.setText("Success Rate: " + (int) successRate + "%");

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(successRate, "Correct"));
        entries.add(new PieEntry(100 - successRate, "Incorrect"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(18f);

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(testName);
        pieChart.setCenterTextSize(20f);
        pieChart.setDrawHoleEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setTextSize(16f);
        legend.setXEntrySpace(24f);

        pieChart.invalidate();

        if (successRate >= 80)
            motivationText.setText("\uD83D\uDD25 Awesome!");
        else if (successRate >= 50)
            motivationText.setText("\uD83D\uDCAA Keep practicing!");
        else
            motivationText.setText("\uD83D\uDCDA Don’t give up!");
    }
}