package com.example.brainwashwords;

import android.os.Bundle; // טעינת נתוני savedInstanceState בעת יצירת האקטיביטי

import androidx.activity.EdgeToEdge; // מאפשר תצוגת מסך מלאה (edge-to-edge)
import androidx.appcompat.app.AppCompatActivity; // בסיס לאקטיביטי עם תמיכה בעיצוב מודרני
import androidx.core.graphics.Insets; // מחלקה לניהול שוליים פנימיים של מסך
import androidx.core.view.ViewCompat; // כלים לניהול תצוגה בצורה מותאמת לגרסאות שונות
import androidx.core.view.WindowInsetsCompat; // ניהול ריווח מול רכיבי מערכת כמו status bar

public class workOut1 extends BaseActivity { // מחלקה שמייצגת את מסך workOut1 ויורשת מ-BaseActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) { // המתודה מופעלת בעת יצירת האקטיביטי
        super.onCreate(savedInstanceState); // קריאה לאתחול הבסיסי של המחלקה האב
        EdgeToEdge.enable(this); // מאפשר תצוגה שמגיעה עד קצוות המסך (ללא מרווחים מיותרים)
        setContentView(R.layout.activity_work_out1); // קובע את עיצוב המסך לפי קובץ ה-XML המתאים
    }
}
