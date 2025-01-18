package com.example.malaysiasafe;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    // Declare UI elements
    EditText username, password;
    Button loginButton;
    TextView createAcc;

    // Firebase Realtime Database
    FirebaseDatabase database;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge content
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance("https://malaysiasafe-4daeb-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersRef = database.getReference("Users"); // "users" node in Firebase

        // Initialize UI components
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        createAcc = findViewById(R.id.createAcc);

        // Handle "Create Account" button click
        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
                finish();
            }
        });

        // Handle Login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input
                String Username = username.getText().toString().trim();
                String Password = password.getText().toString().trim();

                // Validate input
                if (TextUtils.isEmpty(Username)) {
                    Toast.makeText(Login.this, "Enter username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(Password)) {
                    Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check credentials in Firebase Realtime Database
                usersRef.child(Username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User found
                            String dbPassword = snapshot.child("password").getValue(String.class);
                            if (dbPassword != null && dbPassword.equals(Password)) {

                                // Assuming the username and password validation is successful
                                Intent intent = new Intent(Login.this, DashboardActivity.class);
                                intent.putExtra("username", Username);  // Pass username to DashboardActivity
                                startActivity(intent);
                                finish();
                                // Password matches
                                Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();

                            } else {
                                // Password does not match
                                Toast.makeText(Login.this, "Incorrect password. Try again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // User not found
                            Toast.makeText(Login.this, "Username does not exist.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, "Database error: " + error.getMessage());
                        Toast.makeText(Login.this, "Database error. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}