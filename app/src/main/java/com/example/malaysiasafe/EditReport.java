package com.example.malaysiasafe;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditReportActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference disasterRef;

    private String disasterKey;

    private EditText locationInput, nameDisasterInput, timeInput, dateInput, contactNumberInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_report);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance("https://malaysiasafe-4daeb-default-rtdb.asia-southeast1.firebasedatabase.app/");
        disasterRef = database.getReference("DisasterLocation");

        // Get the disaster key
        disasterKey = getIntent().getStringExtra("disasterKey");

        // Initialize input fields
        locationInput = findViewById(R.id.location_input);
        nameDisasterInput = findViewById(R.id.name_disaster_input);
        timeInput = findViewById(R.id.time_input);
        dateInput = findViewById(R.id.date_input);
        contactNumberInput = findViewById(R.id.contact_number_input);

        // Save button
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> updateData());
    }

    private void updateData() {
        if (disasterKey != null) {
            disasterRef.child(disasterKey).child("location").setValue(locationInput.getText().toString());
            disasterRef.child(disasterKey).child("nameDisaster").setValue(nameDisasterInput.getText().toString());
            disasterRef.child(disasterKey).child("time").setValue(timeInput.getText().toString());
            disasterRef.child(disasterKey).child("date").setValue(dateInput.getText().toString());
            disasterRef.child(disasterKey).child("contactNumber").setValue(contactNumberInput.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditReportActivity.this, "Report Updated", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity
                        } else {
                            Toast.makeText(EditReportActivity.this, "Failed to update report", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
