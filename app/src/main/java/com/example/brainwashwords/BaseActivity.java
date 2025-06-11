package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

/**
 * BaseActivity serves as a parent class for all Activities that use a Navigation Drawer.
 * It handles drawer initialization and navigation item selection.
 */
public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // משתנה שמחזיק את תפריט הצד
    protected DrawerLayout drawerLayout;

    /**
     * Called when the activity is starting.
     * Applies the saved theme before the activity view is created.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🌗 טוען את מצב התאורה (בהיר/כהה) לפני טעינת המסך
        ThemeHelper.applySavedTheme(this);

        // קריאה ל־super כדי לאתחל את מחזור החיים של האקטיביטי
        super.onCreate(savedInstanceState);
    }

    /**
     * Sets up the Navigation Drawer and connects it to the toolbar.
     * This should be called in child activities after setContentView().
     */
    protected void setupDrawer() {
        // איתור רכיב ה־DrawerLayout מתוך ה־XML
        drawerLayout = findViewById(R.id.drawer_layout);

        // איתור והגדרת ה־Toolbar כ־ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // איתור NavigationView וחיבור מאזין לבחירת פריטים
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // יצירת כפתור פתיחה/סגירה של המגירה (ההמבורגר)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        // חיבור הכפתור ל־DrawerLayout
        drawerLayout.addDrawerListener(toggle);

        // מסנכרן את מצב הכפתור עם מצב המגירה
        toggle.syncState();
    }

    /**
     * Handles selection of items in the navigation drawer.
     *
     * @param item The selected menu item.
     * @return true if handled successfully.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // סגירת המגירה מיד לאחר בחירת פריט
        drawerLayout.closeDrawer(GravityCompat.START);

        // ניווט למסכים השונים לפי הפריט שנבחר
        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, about.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_home) {
            startActivity(new Intent(this, home.class));
        }

        return true;
    }

    /**
     * Overrides the default back button behavior.
     * If the drawer is open, closes it instead of exiting the activity.
     */
    @Override
    public void onBackPressed() {
        // אם המגירה פתוחה, נסגור אותה במקום לסגור את המסך
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // אחרת, ביצוע ברירת מחדל (יציאה/חזרה אחורה)
            super.onBackPressed();
        }
    }
}
