package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.Map;

/**
 * Activity responsible for handling user login.
 * Supports email/password login (custom DB),
 * Google Sign-In (via FirebaseAuth),
 * and dev bypass mode for quick testing.
 */
public class login extends AppCompatActivity {

    ImageButton imageButton;     // כפתור התחברות רגילה
    TextView devBypass;          // טקסט קטן ללחיצה רבתית למצב מפתח
    Button button5;              // כפתור מעבר למסך הרשמה
    EditText Email, Password, Name; // שדות הזנה
    private DatabaseReference usersRef; // רפרנס למסד נתונים

    private static final int RC_SIGN_IN = 9001; // קוד לזיהוי חזרת תוצאה מה-Google Sign-In
    private GoogleSignInClient mGoogleSignInClient; // לקוח התחברות של גוגל
    private FirebaseAuth mAuth; // Firebase Auth – לצורך התחברות עם Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // קישור רכיבי UI מה-XML
        Email = findViewById(R.id.editTextTextPersonName2);
        Password = findViewById(R.id.editTextTextPassword);
        Name = findViewById(R.id.editTextTextPersonName);
        imageButton = findViewById(R.id.imageButton);
        button5 = findViewById(R.id.button5);
        devBypass = findViewById(R.id.devBypass);

        // חיבור למסד הנתונים (Realtime Database)
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        // אתחול FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // הגדרת התחברות עם Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // מזהה OAuth
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // לחיצה על כפתור התחברות עם גוגל
        findViewById(R.id.button3).setOnClickListener(v -> signInWithGoogle());

        // מצב מפתחים: לאחר 5 לחיצות – דילוג ישיר למסך הבית
        final int[] tapCount = {0};
        devBypass.setOnClickListener(v -> {
            tapCount[0]++;
            if (tapCount[0] >= 5) {
                Toast.makeText(this, "Dev login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, home.class));
                finish();
            }
        });

        // מעבר למסך הרשמה
        button5.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, signup.class);
            startActivity(intent);
        });

        // התחברות ידנית עם אימייל וסיסמה
        imageButton.setOnClickListener(v -> attemptLogin());
    }

    /**
     * Attempts to log in using manual email/password authentication from Realtime Database.
     * Stores the UID and username locally upon success.
     */
    private void attemptLogin() {
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                boolean userFound = false;

                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    Object value = userSnapshot.getValue();
                    if (value instanceof Map) {
                        User user = userSnapshot.getValue(User.class);

                        // בדיקת התאמה של אימייל וסיסמה
                        if (user != null &&
                                email.equals(user.getEmail()) &&
                                password.equals(user.getPassword())) {

                            userFound = true;

                            // שמירת פרטי המשתמש בזיכרון המקומי
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("uid", userSnapshot.getKey())
                                    .putString("username", user.getName() != null ? user.getName() : "")
                                    .apply();

                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(login.this, home.class));
                            finish();
                            break;
                        }
                    }
                }

                if (!userFound) {
                    Toast.makeText(this, "User not found. Please sign up first.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Starts Google Sign-In flow.
     */
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handles the result from Google Sign-In activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
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

    /**
     * Authenticates with Firebase using the Google ID token.
     * On success, saves the user's UID and name to SharedPreferences and moves to home screen.
     *
     * @param idToken The Google ID token.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null || idToken.length() == 0) {
            Toast.makeText(this, "ID token is null", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("uid", user.getUid())
                                    .putString("username", user.getDisplayName() != null ? user.getDisplayName() : "")
                                    .apply();

                            startActivity(new Intent(login.this, home.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Google sign in failed: user is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Firebase Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
