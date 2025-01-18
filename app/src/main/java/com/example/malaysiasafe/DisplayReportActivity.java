package com.example.malaysiasafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisplayReportActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference disasterRef;
    String disasterKey; // Key of the node to update or delete

    TextView locationText, nameDisasterText, timeText, dateText, contactNumberText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_report);

        // Retrieve data passed from CommunityReportActivity
        Intent intent = getIntent();
        String location = intent.getStringExtra("location");
        String nameDisaster = intent.getStringExtra("nameDisaster");
        String time = intent.getStringExtra("time");
        String date = intent.getStringExtra("date");
        String contactNumber = intent.getStringExtra("contactNumber");

        // Log the received data
        Log.d("DisplayReportActivity", "Location: " + location);
        Log.d("DisplayReportActivity", "Name of Disaster: " + nameDisaster);
        Log.d("DisplayReportActivity", "Time: " + time);
        Log.d("DisplayReportActivity", "Date: " + date);
        Log.d("DisplayReportActivity", "Contact Number: " + contactNumber);

        // Initialize TextViews
        locationText = findViewById(R.id.location_value);
        nameDisasterText = findViewById(R.id.name_disaster_value);
        timeText = findViewById(R.id.time_value);
        dateText = findViewById(R.id.date_value);
        contactNumberText = findViewById(R.id.contact_number_value);

        // Display Intent data
        locationText.setText(location != null ? location : "Not Available");
        nameDisasterText.setText(nameDisaster != null ? nameDisaster : "Not Available");
        timeText.setText(time != null ? time : "Not Available");
        dateText.setText(date != null ? date : "Not Available");
        contactNumberText.setText(contactNumber != null ? contactNumber : "Not Available");

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance("https://malaysiasafe-4daeb-default-rtdb.asia-southeast1.firebasedatabase.app/");
        disasterRef = database.getReference("DisasterLocation");

        // Get the disaster key from the intent
        disasterKey = intent.getStringExtra("disasterKey");

        // Load data from Firebase if disasterKey is provided
        if (disasterKey != null && !disasterKey.isEmpty()) {
            loadData();
        } else {
            Log.d("DisplayReportActivity", "DisasterKey is null or empty, skipping Firebase load.");
        }

        // Button to navigate back to CommunityReportActivity
        Button goToCommunityReportButton = findViewById(R.id.add_button);
        goToCommunityReportButton.setOnClickListener(v -> {
            Intent communityIntent = new Intent(DisplayReportActivity.this, CommunityReportActivity.class);
            startActivity(communityIntent);
        });

        // Edit button
        Button editButton = findViewById(R.id.edit_button);
        editButton.setOnClickListener(v -> {
            if (disasterKey == null || disasterKey.isEmpty()) {
                Toast.makeText(this, "Cannot edit: DisasterKey is null or invalid", Toast.LENGTH_SHORT).show();
            } else {
                Intent editIntent = new Intent(DisplayReportActivity.this, EditReportActivity.class);
                editIntent.putExtra("disasterKey", disasterKey);
                startActivity(editIntent);
            }
        });

        // Delete button
        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void loadData() {
        if (disasterKey != null && !disasterKey.isEmpty()) {
            Log.d("DisplayReportActivity", "Loading data for key: " + disasterKey);
            disasterRef.child(disasterKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        locationText.setText(snapshot.child("location").getValue(String.class));
                        nameDisasterText.setText(snapshot.child("nameDisaster").getValue(String.class));
                        timeText.setText(snapshot.child("time").getValue(String.class));
                        dateText.setText(snapshot.child("date").getValue(String.class));
                        contactNumberText.setText(snapshot.child("contactNumber").getValue(String.class));
                    } else {
                        Log.d("DisplayReportActivity", "No data exists for the provided key: " + disasterKey);
                        Toast.makeText(DisplayReportActivity.this, "No data found for the given key.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("DisplayReportActivity", "Failed to load data: " + error.getMessage());
                    Toast.makeText(DisplayReportActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void confirmDelete() {
        if (disasterKey == null || disasterKey.isEmpty()) {
            Toast.makeText(this, "Cannot delete: DisasterKey is null or invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Report")
                .setMessage("Are you sure you want to delete this report?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Log.d("DisplayReportActivity", "Deleting data for key: " + disasterKey);
                    disasterRef.child(disasterKey).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(DisplayReportActivity.this, "Report Deleted", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity
                        } else {
                            Exception exception = task.getException();
                            Log.e("DisplayReportActivity", "Failed to delete data: " + (exception != null ? exception.getMessage() : "Unknown error"));
                            Toast.makeText(DisplayReportActivity.this, "Failed to delete report", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}

