package com.example.brainwashwords;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class home extends AppCompatActivity {

    private AppCompatButton button, about, testYourself;
    private TextView usernameDisplay;
    private DatabaseReference usersRef;
    private FirebaseFirestore firestore;
    private String username;
    private boolean isSorted = false;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        firestore = FirebaseFirestore.getInstance();

        initializeViews();
        setupAlertDialog();

        username = getIntent().getStringExtra("USERNAME");
        if (username != null) {
            usernameDisplay.setText("Welcome, " + username);
            loadUsernameFromFirebase();
        }

        setupClickListeners();
    }

    private void initializeViews() {
        usernameDisplay = findViewById(R.id.usernameTextView3);
        button = findViewById(R.id.button);
        about = findViewById(R.id.button3);
        testYourself = findViewById(R.id.button1);
    }

    private void setupAlertDialog() {
        builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Wait a minute");
        builder.setMessage("You have to sort the words first");
        builder.setNegativeButton("OK, got it!", (dialog, which) -> dialog.cancel());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfSorted();
    }

    private void checkIfSorted() {
        firestore.collection("groups").document("workout1").collection("words")
                .get()
                .addOnSuccessListener(result -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : result) {
                        Boolean known = doc.getBoolean("known");
                        if (Boolean.TRUE.equals(known)) {
                            count++;
                        }
                    }
                    isSorted = count >= 4;
                    updateTestButtonState(isSorted);
                });
    }

    private void updateTestButtonState(boolean enabled) {
        testYourself.setEnabled(enabled);
        testYourself.setTextColor(enabled ? Color.WHITE : Color.GRAY);
        if (enabled) {
            testYourself.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .withEndAction(() ->
                            testYourself.animate().scaleX(1f).scaleY(1f).setDuration(150));
        }
    }

    private void setupClickListeners() {
        button.setOnClickListener(v -> startActivity(new Intent(home.this, group_selection.class)));
        about.setOnClickListener(v -> startActivity(new Intent(home.this, about.class)));
        testYourself.setOnClickListener(v -> {
            if (!isSorted) {
                builder.show();
            } else {
                startActivity(new Intent(home.this, TestMenuActivity.class));
            }
        });
    }

    private void loadUsernameFromFirebase() {
        usersRef.orderByChild("name").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    usernameDisplay.setText("Welcome, " + user.getName());
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(home.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
