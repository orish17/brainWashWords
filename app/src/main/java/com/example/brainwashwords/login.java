package com.example.brainwashwords; // החבילה שבה נמצאת המחלקה

import android.content.Intent; // מאפשר פתיחת אקטיביטיז חדשות
import android.content.SharedPreferences; // לשמירת נתונים מקומיים (כמו מזהה המשתמש)
import android.os.Bundle; // מכיל מידע על מצב קודם של activity
import android.util.Log; // להדפסת הודעות דיבוג
import android.widget.Button; // כפתור סטנדרטי
import android.widget.EditText; // שדה להזנת טקסט
import android.widget.ImageButton; // כפתור עם אייקון
import android.widget.TextView; // תיבת טקסט להצגה
import android.widget.Toast; // להודעות קצרות למשתמש

import androidx.annotation.Nullable; // מאפשר קבלת ערכים null
import androidx.appcompat.app.AppCompatActivity; // מחלקת בסיס לכל Activity עם תמיכה בעיצוב מודרני

import com.google.android.gms.auth.api.signin.*; // ספריות של Google Sign-In
import com.google.android.gms.common.api.ApiException; // שגיאה של Google Sign-In
import com.google.android.gms.tasks.Task; // מייצג פעולה אסינכרונית
import com.google.firebase.auth.*; // ספרייה של Firebase Authentication
import com.google.firebase.database.*; // ספרייה של Firebase Realtime Database

import java.util.Map; // ממשק למיפוי מפתחות וערכים

public class login extends AppCompatActivity { // מחלקת activity ראשית להתחברות

    ImageButton imageButton; // כפתור התחברות רגילה
    TextView devBypass; // טקסט למעבר מוסתר למצב מפתח
    Button button5; // כפתור למעבר למסך הרשמה
    EditText Email, Password, Name; // שדות קלט למייל, סיסמה ושם
    private DatabaseReference usersRef; // הפניה ל־Realtime Database

    private static final int RC_SIGN_IN = 9001; // קוד קבוע לזיהוי תוצאה מ־Google Sign-In
    private GoogleSignInClient mGoogleSignInClient; // לקוח ניהול התחברות גוגל
    private FirebaseAuth mAuth; // ניהול התחברות של Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) { // מופעל בעת יצירת המסך
        super.onCreate(savedInstanceState); // קריאה למחלקת העל
        setContentView(R.layout.activity_login); // קישור לקובץ XML של המסך

        Email = findViewById(R.id.editTextTextPersonName2); // קישור לשדה המייל
        Password = findViewById(R.id.editTextTextPassword); // קישור לשדה הסיסמה
        Name = findViewById(R.id.editTextTextPersonName); // קישור לשדה השם
        imageButton = findViewById(R.id.imageButton); // קישור לכפתור ההתחברות
        button5 = findViewById(R.id.button5); // קישור לכפתור הרשמה
        devBypass = findViewById(R.id.devBypass); // קישור לטקסט של מצב dev

        FirebaseDatabase database = FirebaseDatabase.getInstance(); // יצירת מופע של בסיס הנתונים
        usersRef = database.getReference("users"); // קישור לצומת "users"

        mAuth = FirebaseAuth.getInstance(); // אתחול FirebaseAuth

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) // יצירת הגדרות התחברות בסיסיות
                .requestIdToken("494986206209-75mh992ljeopuj60sdq4f5ioik4kj7cb.apps.googleusercontent.com") // בקשת טוקן התחברות מהשרת של גוגל
                .requestEmail() // בקשת גישה לאימייל של המשתמש
                .build(); // סיום הבנייה של האובייקט

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso); // יצירת לקוח התחברות של גוגל

        findViewById(R.id.button3).setOnClickListener(v -> signInWithGoogle()); // לחיצה על כפתור התחברות עם גוגל

        final int[] tapCount = {0}; // מונה לחיצות למצב dev
        devBypass.setOnClickListener(v -> { // לחיצה על הטקסט של מצב dev
            tapCount[0]++; // העלאת המונה
            if (tapCount[0] >= 5) { // אם נלחץ 5 פעמים
                Toast.makeText(this, "Dev login successful", Toast.LENGTH_SHORT).show(); // הודעת הצלחה
                startActivity(new Intent(this, home.class)); // מעבר למסך הבית
                finish(); // סיום המסך הנוכחי
            }
        });

        button5.setOnClickListener(v -> { // לחיצה על כפתור הרשמה
            Intent intent = new Intent(login.this, signup.class); // מעבר למסך הרשמה
            startActivity(intent);
        });

        imageButton.setOnClickListener(v -> attemptLogin()); // התחברות ידנית דרך מייל/סיסמה
    }

    private void attemptLogin() { // התחברות ידנית דרך המערכת שלנו
        String email = Email.getText().toString().trim(); // קבלת מייל
        String password = Password.getText().toString().trim(); // קבלת סיסמה

        if (email.isEmpty() || password.isEmpty()) { // בדיקת שדות ריקים
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.get().addOnCompleteListener(task -> { // שליפת נתוני המשתמשים
            if (task.isSuccessful() && task.getResult().exists()) { // אם הצליח ויש תוצאה
                boolean userFound = false; // דגל האם המשתמש נמצא

                for (DataSnapshot userSnapshot : task.getResult().getChildren()) { // מעבר על כל המשתמשים
                    Object value = userSnapshot.getValue(); // קבלת הערך
                    if (value instanceof Map) { // אם זה מפה
                        User user = userSnapshot.getValue(User.class); // המרת הנתונים למחלקת User

                        if (user != null &&
                                email.equals(user.getEmail()) &&
                                password.equals(user.getPassword())) { // אם המייל והסיסמה תואמים

                            userFound = true;

                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE); // פתיחת SharedPreferences
                            prefs.edit()
                                    .putString("uid", userSnapshot.getKey()) // שמירת UID
                                    .putString("username", user.getName() != null ? user.getName() : "") // שמירת שם משתמש
                                    .apply();

                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(login.this, home.class)); // מעבר למסך הבית
                            finish();
                            break;
                        }
                    }
                }

                if (!userFound) {
                    Toast.makeText(this, "User not found. Please sign up first.", Toast.LENGTH_LONG).show(); // הודעה אם המשתמש לא קיים
                }
            } else {
                Toast.makeText(this, "Database error. Please try again.", Toast.LENGTH_SHORT).show(); // שגיאה בגישה ל־DB
            }
        });
    }

    private void signInWithGoogle() { // התחברות עם גוגל
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> { // ניתוק התחברות קודמת
            Intent signInIntent = mGoogleSignInClient.getSignInIntent(); // פתיחת חלון התחברות
            startActivityForResult(signInIntent, RC_SIGN_IN); // התחלת תהליך ההתחברות
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // תוצאה מהתחברות חיצונית
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) { // אם זו תוצאה מהתחברות גוגל
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data); // ניסיון לקבל חשבון
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class); // קבלת חשבון מגוגל
                if (account != null && account.getIdToken() != null) { // אם החשבון תקין ויש טוקן
                    firebaseAuthWithGoogle(account.getIdToken()); // התחברות עם Firebase לפי הטוקן
                } else {
                    Toast.makeText(this, "Google Sign-In failed: no token", Toast.LENGTH_SHORT).show();
                    Log.e("Login", "account or token is null: " + account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In error", Toast.LENGTH_SHORT).show();
                Log.e("Login", "Google sign-in error", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) { // התחברות ל־Firebase עם טוקן של גוגל
        if (idToken == null || idToken.length() == 0) {
            Toast.makeText(this, "ID token is null", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null); // יצירת אישור התחברות
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser(); // קבלת משתמש מחובר
                        if (user != null) {
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE); // שמירה מקומית
                            prefs.edit()
                                    .putString("uid", user.getUid()) // שמירת מזהה
                                    .putString("username", user.getDisplayName() != null ? user.getDisplayName() : "") // שמירת שם
                                    .apply();

                            startActivity(new Intent(login.this, home.class)); // מעבר למסך הבית
                            finish();
                        } else {
                            Toast.makeText(this, "Google sign in failed: user is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Firebase Authentication failed.", Toast.LENGTH_SHORT).show(); // כישלון בהתחברות
                    }
                });
    }
}
