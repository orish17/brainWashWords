package com.example.brainwashwords;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private PieChart pieChart;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupDrawer();

        pieChart = findViewById(R.id.pieChart);

        // שליפת UID מה־SharedPreferences
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
                if (user != null && user.getTests() != null) {
                    loadChart(user.getTests());
                } else {
                    Toast.makeText(ProfileActivity.this, "No test data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChart(Map<String, TestResult> tests) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, TestResult> entry : tests.entrySet()) {
            entries.add(new PieEntry(entry.getValue().getSuccessRate(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Success Rates");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Test Success");
        pieChart.setDrawHoleEnabled(true);
        pieChart.invalidate(); // רענון הגרף
    }
}
