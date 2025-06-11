package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays a list of word groups (workouts) from Firebase Firestore.
 * Allows the user to choose a group for sorting or practicing words.
 * Includes loading indicator and empty state handling.
 */
public class group_selection extends BaseActivity {

    private RecyclerView recyclerView;       // תצוגת הרשימה של הקבוצות
    private GroupAdapter adapter;            // המתאם שמציג את הקבוצות ברשימה
    private List<Group> groupList;           // רשימת קבוצות שמגיעות מ-Firebase
    private FirebaseFirestore db;            // חיבור למסד הנתונים של Firestore
    private ProgressBar progressBar;         // טוען אנימציה בעת שליפת הנתונים

    /**
     * Called when the activity is created. Initializes UI and loads data.
     *
     * @param savedInstanceState State of the activity from previous instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🌗 החלת מצב תאורה לפי ההעדפה השמורה
        ThemeHelper.applySavedTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);

        // קישור רכיבי UI מתוך ה-XML
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        // אתחול רשימת הקבוצות
        groupList = new ArrayList<>();

        // אתחול תפריט צד (Navigation Drawer)
        setupDrawer();

        // הכנת ה־RecyclerView לתצוגה
        setupRecyclerView();

        // חיבור למסד הנתונים Firestore
        db = FirebaseFirestore.getInstance();

        // שליפת הקבוצות מה-DB
        loadGroups();
    }

    /**
     * Sets up the RecyclerView with a linear layout and adapter.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // רשימה אנכית
        adapter = new GroupAdapter(groupList);                        // יצירת מתאם עם הנתונים
        recyclerView.setAdapter(adapter);                             // חיבור לרכיב RecyclerView
        recyclerView.setHasFixedSize(true);                           // אופטימיזציה למספר פריטים קבוע
    }

    /**
     * Loads word groups from Firestore into the list and updates the UI.
     */
    private void loadGroups() {
        showLoading(true); // הצגת טעינה

        db.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        showLoading(false); // הסתרת טעינה

                        if (task.isSuccessful()) {
                            groupList.clear(); // ניקוי הרשימה הקודמת
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Group group = document.toObject(Group.class); // יצירת אובייקט מקבוצת הנתונים
                                groupList.add(group); // הוספה לרשימה
                            }

                            adapter.notifyDataSetChanged(); // עדכון התצוגה

                            if (groupList.isEmpty()) {
                                showEmptyState(true); // הצגת "אין נתונים"
                            } else {
                                showEmptyState(false); // הסתרת מצב ריק
                            }

                        } else {
                            handleError(task.getException()); // טיפול בשגיאה
                        }
                    }
                });
    }

    /**
     * Shows or hides the loading indicator and content list.
     *
     * @param show true to show loading, false to hide.
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE); // הצגת/הסתרת progressBar
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE); // הצגת/הסתרת רשימה
        }
    }

    /**
     * Shows a view indicating that no groups are available.
     *
     * @param show true to show empty state, false to show list.
     */
    private void showEmptyState(boolean show) {
        View emptyView = findViewById(R.id.emptyView); // איתור תצוגת ריקנות (אם קיימת)
        if (emptyView != null) {
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE); // הסתרה/הצגה של הרשימה
    }

    /**
     * Displays an error message in case of a failure to load data.
     *
     * @param e The exception that occurred.
     */
    private void handleError(Exception e) {
        Toast.makeText(this,
                "Error loading groups: " + e.getMessage(),
                Toast.LENGTH_SHORT).show(); // הודעת שגיאה למשתמש
    }
}
