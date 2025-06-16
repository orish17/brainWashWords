package com.example.brainwashwords; // מציין שהמחלקה הזו שייכת לחבילת הקוד הראשית של האפליקציה

import android.content.Intent; // מאפשר מעבר בין מסכים (Activities)
import android.content.SharedPreferences; // מאפשר גישה לנתונים שמורים מקומית (לא בשימוש ישיר כאן)
import android.os.Bundle; // מחזיק מידע על מצב האקטיביטי בעת אתחול
import android.view.MenuItem; // מייצג פריט שנבחר מתוך התפריט

import androidx.annotation.NonNull; // מוודא שהפרמטר לא יכול להיות null
import androidx.appcompat.app.ActionBarDrawerToggle; // מוסיף כפתור "המבורגר" לפתיחה/סגירה של התפריט הצדדי
import androidx.appcompat.app.AppCompatActivity; // הבסיס לכל אקטיביטי עם תמיכה ב־Toolbar
import androidx.appcompat.widget.Toolbar; // סרגל הכלים שמופיע בראש המסך
import androidx.core.view.GravityCompat; // מאפשר שליטה על כיווני פתיחה של תפריט צד
import androidx.drawerlayout.widget.DrawerLayout; // תפריט צד (Drawer) שמחליק מהקצה של המסך

import com.google.android.material.navigation.NavigationView; // תצוגת ניווט עם תפריט – חלק מהמגירה

/**
 * BaseActivity – מחלקת בסיס משותפת לכל המסכים באפליקציה שיש להם תפריט צד (Navigation Drawer).
 * כל Activity שיורש ממנה יכול להציג תפריט צד ולנווט בין מסכים.
 */
public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout; // רכיב שמכיל את תפריט הצד

    /**
     * onCreate – מתבצע בעת יצירת האקטיביטי.
     * כאן אנחנו טוענים את ערכת הנושא (theme) שהמשתמש בחר.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // מפעיל את מצב התאורה שנשמר (כהה/בהיר)
        super.onCreate(savedInstanceState); // קריאה לאתחול הבסיסי של האקטיביטי
    }

    /**
     * setupDrawer – פעולה שמאתחלת את תפריט הצד, toolbar, כפתור ההמבורגר, ומאזין לבחירת פריטים.
     * יש לקרוא לה בכל אקטיביטי יורש אחרי setContentView.
     */
    protected void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout); // מקשר את רכיב התפריט הצדדי מה־XML

        Toolbar toolbar = findViewById(R.id.toolbar); // מוצא את toolbar לפי ID
        setSupportActionBar(toolbar); // קובע אותו כ־ActionBar הרשמי של המסך

        NavigationView navigationView = findViewById(R.id.nav_view); // מוצא את תפריט הניווט (NavigationView)
        navigationView.setNavigationItemSelectedListener(this); // קובע ש־BaseActivity תטפל בלחיצות על פריטים בתפריט

        // יוצר אובייקט שמטפל באנימציה של פתיחה/סגירה של המגירה + כפתור המבורגר
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, // טקסט לקריינים – פתיחה
                R.string.navigation_drawer_close // טקסט לקריינים – סגירה
        );

        drawerLayout.addDrawerListener(toggle); // מחבר את toggle לרכיב המגירה
        toggle.syncState(); // מסנכרן את האייקון לפי מצב המגירה (פתוח/סגור)
    }

    /**
     * onNavigationItemSelected – מתבצע כאשר המשתמש בוחר פריט מתוך התפריט הצדדי.
     * מבצע ניווט בהתאם לפריט שנבחר.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId(); // מזהה את הפריט שנבחר לפי ה־ID

        drawerLayout.closeDrawer(GravityCompat.START); // סוגר את המגירה לאחר הבחירה

        // התנאים הבאים מנווטים למסכים המתאימים לפי הפריט
        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class)); // מעבר למסך "הפרופיל שלי"
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, about.class)); // מעבר למסך אודות
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class)); // מעבר למסך הגדרות
        } else if (id == R.id.nav_home) {
            startActivity(new Intent(this, home.class)); // מעבר למסך הבית
        }

        return true; // מציין שטיפלנו באירוע
    }

    /**
     * onBackPressed – פעולה שמתרחשת כאשר המשתמש לוחץ על כפתור "חזור".
     * אם התפריט הצדדי פתוח – נסגור אותו במקום לצאת מהמסך.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) { // אם המגירה פתוחה
            drawerLayout.closeDrawer(GravityCompat.START); // נסגור אותה
        } else {
            super.onBackPressed(); // אחרת, נחזור אחורה כרגיל
        }
    }
}
