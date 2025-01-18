package com.example.malaysiasafe;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp;

public class CommunityReportActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private EditText locationEditText, nameDisasterInput, timeInput, dateInput, contactNumberInput;

    FirebaseDatabase database;
    DatabaseReference disasterRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_report);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        locationEditText = findViewById(R.id.input_location);
        nameDisasterInput = findViewById(R.id.input_name_disaster);
        timeInput = findViewById(R.id.input_time);
        dateInput = findViewById(R.id.input_date);
        contactNumberInput = findViewById(R.id.input_contact_number);

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance("https://malaysiasafe-4daeb-default-rtdb.asia-southeast1.firebasedatabase.app/");
        disasterRef = database.getReference("DisasterLocation");

        // Load Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error loading map.", Toast.LENGTH_SHORT).show();
        }

        // Set button listener for submit
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        String location = locationEditText.getText().toString().trim();
        String nameDisaster = nameDisasterInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String contactNumber = contactNumberInput.getText().toString().trim();

        // Validate inputs
        if (location.isEmpty() || nameDisaster.isEmpty() || time.isEmpty() || date.isEmpty() || contactNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save report to Firebase Database
        saveReportToDatabase(location, nameDisaster, time, date, contactNumber);
    }

    private void saveReportToDatabase(String location, String nameDisaster, String time, String date, String contactNumber) {
        String reportId = disasterRef.push().getKey();

        // Log reportId for debugging
        Log.d("CommunityReport", "Generated reportId: " + reportId);

        ShowReport report = new ShowReport(location, nameDisaster, time, date, contactNumber);

        if (reportId != null) {
            disasterRef.child(reportId).setValue(report)
                    .addOnCompleteListener(aVoid -> {
                        Log.d("CommunityReport", "Report submitted successfully!");
                        Toast.makeText(this, "Report submitted successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to DisplayReportActivity
                        Intent intent = new Intent(CommunityReportActivity.this, DisplayReportActivity.class);
                        intent.putExtra("location", location);
                        intent.putExtra("nameDisaster", nameDisaster);
                        intent.putExtra("time", time);
                        intent.putExtra("date", date);
                        intent.putExtra("contactNumber", contactNumber);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CommunityReport", "Failed to submit report", e);
                        Toast.makeText(this, "Failed to submit report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Failed to generate report ID.", Toast.LENGTH_SHORT).show();
            Log.e("CommunityReport", "Failed to generate report ID.");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (checkLocationPermission()) {
            enableMyLocation();
        } else {
            requestLocationPermission();
        }

        LatLng kualaLumpur = new LatLng(3.1390, 101.6869);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kualaLumpur, 10));

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            String formattedLatLng = String.format("Lat: %.3f, Lng: %.3f", latLng.latitude, latLng.longitude);
            locationEditText.setText(formattedLatLng);
        });
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void enableMyLocation() {
        if (checkLocationPermission()) {
            if (mMap != null) {
                try {
                    mMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {
                    Log.e("CommunityReport", "Location permission error", e);
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
