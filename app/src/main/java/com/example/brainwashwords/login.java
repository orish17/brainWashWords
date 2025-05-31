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

public class login extends AppCompatActivity {
    ImageButton imageButton;
    TextView devBypass;
    Button button5;
    EditText Email, Password, Name;
    private DatabaseReference usersRef;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Email = findViewById(R.id.editTextTextPersonName2);
        Password = findViewById(R.id.editTextTextPassword);
        Name = findViewById(R.id.editTextTextPersonName);
        imageButton = findViewById(R.id.imageButton);
        button5 = findViewById(R.id.button5);
        devBypass = findViewById(R.id.devBypass);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        mAuth = FirebaseAuth.getInstance();

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.button3).setOnClickListener(v -> signInWithGoogle());

        final int[] tapCount = {0};
        devBypass.setOnClickListener(v -> {
            tapCount[0]++;
            if (tapCount[0] >= 5) {
                Toast.makeText(this, "Dev login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, home.class));
                finish();
            }
        });

        button5.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, signup.class);
            startActivity(intent);
        });

        imageButton.setOnClickListener(v -> attemptLogin());
    }

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
                        if (user != null &&
                                email.equals(user.getEmail()) &&
                                password.equals(user.getPassword())) {

                            userFound = true;

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

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

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
