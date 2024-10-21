package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signup extends AppCompatActivity {
    ImageButton imageButton, home;
    EditText Name, Email, Password, RePassword;

    private FirebaseDatabase orishDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        orishDatabase= FirebaseDatabase.getInstance();
        DatabaseReference word = orishDatabase.getReference("name:");
        word .setValue(Name.getText()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(signup.this, "Data added successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(signup.this, "Failed to add data", Toast.LENGTH_SHORT).show();
                }
            }
        });
        home=findViewById(R.id.imageButton2);
        imageButton=findViewById(R.id.imageButton);
        Name=findViewById(R.id.editTextTextPersonName);
        Email=findViewById(R.id.editTextTextPersonName2);
        Password=findViewById(R.id.editTextTextPassword);
        RePassword=findViewById(R.id.editTextTextPassword2);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(signup.this, home.class);
                startActivity(intent);
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(signup.this, home.class);
                startActivity(intent);
            }
        });
    }
}