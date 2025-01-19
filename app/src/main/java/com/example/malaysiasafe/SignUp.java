package com.example.malaysiasafe;

import static android.content.ContentValues.TAG;

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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

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

public class SignUp extends AppCompatActivity {

    // Declare UI elements
    EditText username, password, email;
    Button signUpButton;
    TextView loginNow;

    // Firebase Realtime Database
    FirebaseDatabase database;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge content
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance("https://malaysiasafe-4daeb-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersRef = database.getReference("Users"); // "users" node in Firebase

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        signUpButton = findViewById(R.id.signUpButton);
        loginNow = findViewById(R.id.loginNow);

        // Handle "Already have an account? Login" button click
        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Handle sign-up button click
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input
                String Username = username.getText().toString().trim();
                String Password = password.getText().toString().trim();
                String Email = email.getText().toString().trim();

                // Validate input
                if (TextUtils.isEmpty(Username)) {
                    Toast.makeText(SignUp.this, "Enter username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(Password)) {
                    Toast.makeText(SignUp.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(Email)) {
                    Toast.makeText(SignUp.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a User object
                User newUser = new User(Username, Email, Password);

                // Save user data to Realtime Database
                usersRef.child(Username).setValue(newUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Data saved successfully
                                    Toast.makeText(SignUp.this, "User registered successfully!", Toast.LENGTH_SHORT).show();

                                    // Redirect to Login screen
                                    Intent intent = new Intent(SignUp.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Failed to save data
                                    Log.w(TAG, "User registration failed", task.getException());
                                    Toast.makeText(SignUp.this, "Failed to register user. Try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
          // Initialize SensorManager and Proximity Sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Initialize SensorEventListener
        proximitySensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    if (event.values[0] < proximitySensor.getMaximumRange()) {
                        // Phone is close to the user (face/body detected)
                        disableInputs(); // Disable input fields
                    } else {
                        // Phone is away from the user
                        enableInputs(); // Enable input fields
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // No implementation needed
            }
        };

        // Register the proximity sensor listener
        sensorManager.registerListener(proximitySensorListener, proximitySensor, SensorManager.SENSOR_DELAY_UI);
    }

    // Disable input fields and sign-up button
    private void disableInputs() {
        username.setEnabled(false);
        password.setEnabled(false);
        email.setEnabled(false);
        signUpButton.setEnabled(false);
        Toast.makeText(SignUp.this, "Please hold the phone steady to continue", Toast.LENGTH_SHORT).show();
    }

    // Enable input fields and sign-up button
    private void enableInputs() {
        username.setEnabled(true);
        password.setEnabled(true);
        email.setEnabled(true);
        signUpButton.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the proximity sensor listener
        if (sensorManager != null) {
            sensorManager.unregisterListener(proximitySensorListener);
        }
    }
    
}

