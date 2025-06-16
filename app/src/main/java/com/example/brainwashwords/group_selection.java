package com.example.brainwashwords; // מיקום המחלקה בתוך חבילת הקוד הראשית

import android.content.Intent; // מאפשר פתיחה של מסכים אחרים
import android.content.SharedPreferences; // (לא בשימוש כאן) שמירה וטעינה של נתונים פשוטים
import android.os.Bundle; // מחזיק מידע על מצב האקטיביטי בעת יצירתו
import android.util.Log; // הדפסת הודעות ליומן (Logcat)
import android.view.View; // בסיס לכל רכיב UI
import android.widget.ProgressBar; // רכיב שמציג אנימציית טעינה
import android.widget.Toast; // הצגת הודעות קצרות למשתמש

import androidx.annotation.NonNull; // אנוטציה שמסייעת למנוע NullPointerException
import androidx.recyclerview.widget.LinearLayoutManager; // פריסת הרשימה בצורה אנכית
import androidx.recyclerview.widget.RecyclerView; // תצוגת רשימה שניתן לגלול

import com.google.android.gms.tasks.OnCompleteListener; // מאזין לסיום משימה אסינכרונית
import com.google.android.gms.tasks.Task; // אובייקט של משימה אסינכרונית
import com.google.firebase.firestore.FirebaseFirestore; // חיבור למסד הנתונים בענן Firestore
import com.google.firebase.firestore.QueryDocumentSnapshot; // תוצאה של מסמך יחיד מהשאילתה
import com.google.firebase.firestore.QuerySnapshot; // תוצאה כוללת של שאילתה במסד הנתונים

import java.util.ArrayList; // מבנה נתונים של רשימה דינמית
import java.util.List; // ממשק של רשימה

/**
 * group_selection – מסך המציג את כל קבוצות המילים הקיימות ב־Firestore.
 * המשתמש בוחר כאן קבוצה לתרגול או למיון.
 * התצוגה כוללת טעינה ו־Empty State אם אין קבוצות.
 */
public class group_selection extends BaseActivity { // המחלקה יורשת מ־BaseActivity כדי להפעיל תפריט צד

    private RecyclerView recyclerView;       // רכיב תצוגת רשימה לקבוצות
    private GroupAdapter adapter;            // המתאם שמציג את האובייקטים בקובץ XML
    private List<Group> groupList;           // הרשימה שמכילה את כל קבוצות המילים
    private FirebaseFirestore db;            // חיבור למסד הנתונים Firestore
    private ProgressBar progressBar;         // אנימציית טעינה בעת שליפת נתונים

    /**
     * מופעל בעת יצירת המסך.
     * מאתחל את ה־UI, מפעיל את תפריט הצד, ומטעין את קבוצות המילים מ־Firestore.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // טוען מצב תאורה (בהיר/כהה) לפי ההגדרה השמורה
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection); // קובע את ה־layout של המסך

        // קישור בין רכיבי XML לבין משתני ג'אווה
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        groupList = new ArrayList<>(); // יצירת רשימה ריקה שתתמלא מהשרת

        setupDrawer(); // תפריט צד

        setupRecyclerView(); // אתחול התצוגה של הרשימה

        db = FirebaseFirestore.getInstance(); // התחברות למסד הנתונים בענן

        loadGroups(); // טעינת קבוצות מהמאגרים
    }

    /**
     * אתחול ה־RecyclerView עם פריסה אנכית ומתאם נתונים.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // קובע פריסה אנכית
        adapter = new GroupAdapter(groupList); // יוצר את המתאם עם הנתונים
        recyclerView.setAdapter(adapter); // מחבר את המתאם לרשימה בפועל
        recyclerView.setHasFixedSize(true); // משפר ביצועים אם הגובה לא משתנה לפי התוכן
    }

    /**
     * טוען את הקבוצות ממסד הנתונים Firebase ומעדכן את המסך בהתאם.
     */
    private void loadGroups() {
        showLoading(true); // מציג טעינה

        db.collection("groups") // ניגש לאוסף "groups" במסד הנתונים
                .get() // שולף את כל המסמכים מתוך האוסף
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        showLoading(false); // מסיים טעינה

                        if (task.isSuccessful()) {
                            groupList.clear(); // מנקה את הרשימה הקודמת

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Group group = document.toObject(Group.class); // ממיר כל מסמך לאובייקט מסוג Group
                                groupList.add(group); // מוסיף לרשימה הכללית
                            }

                            adapter.notifyDataSetChanged(); // מודיע ל־RecyclerView שצריך לרנדר מחדש

                            if (groupList.isEmpty()) {
                                showEmptyState(true); // אם הרשימה ריקה – מציג הודעת "אין קבוצות"
                            } else {
                                showEmptyState(false); // אם יש קבוצות – מציג אותן
                            }

                        } else {
                            handleError(task.getException()); // אם התרחשה שגיאה – נטפל בה
                        }
                    }
                });
    }

    /**
     * מציג או מסתיר את ה־ProgressBar ואת ה־RecyclerView.
     *
     * @param show true = מציג טעינה, false = מציג את התוכן
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE); // מציג או מסתיר את אנימציית הטעינה
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE); // מסתיר או מציג את הרשימה עצמה
        }
    }

    /**
     * מציג הודעה או תצוגה חלופית כאשר אין קבוצות להצגה.
     *
     * @param show true = מציג תצוגת ריקנות, false = מציג את הרשימה
     */
    private void showEmptyState(boolean show) {
        View emptyView = findViewById(R.id.emptyView); // מוצא את תצוגת ה־Empty אם קיימת ב־XML
        if (emptyView != null) {
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE); // קובע אם תוצג או לא
        }
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE); // מסתיר את הרשימה במצב ריק
    }

    /**
     * מציג הודעת שגיאה במידה ולא הצלחנו לטעון קבוצות.
     *
     * @param e השגיאה שהתרחשה
     */
    private void handleError(Exception e) {
        Toast.makeText(this,
                "Error loading groups: " + e.getMessage(),
                Toast.LENGTH_SHORT).show(); // מציג טוסט עם תוכן השגיאה
    }
}
