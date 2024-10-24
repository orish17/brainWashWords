package com.example.brainwashwords;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class group_selection extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load groups from Firestore
        loadGroups();
    }

    private void loadGroups() {
        db.collection("groups")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Group> groupList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String groupId = document.getId();

                            // בדיקה אם הנתונים תקינים לפני הוספתם לרשימה
                            if (name != null && groupId != null) {
                                groupList.add(new Group(name, groupId));
                            }
                        }

                        // Setup RecyclerView adapter
                        groupAdapter = new GroupAdapter(groupList);
                        recyclerView.setAdapter(groupAdapter);
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to load groups.", e);
                });
    }
}
