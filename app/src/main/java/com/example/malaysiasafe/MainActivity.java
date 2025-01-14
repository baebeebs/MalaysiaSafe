package com.example.malaysiasafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Map setup
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                // Default location for map
                googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                        new com.google.android.gms.maps.model.LatLng(3.139, 101.6869), 10f));
            });
        }

        // Button for Emergency Service
        Button btnEmergencyService = findViewById(R.id.btn_emergency_service);
        btnEmergencyService.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EmergencyServiceActivity.class);
            startActivity(intent);
        });

        // Button for Community Report
        Button btnCommunityReport = findViewById(R.id.btn_community_report);
        btnCommunityReport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CommunityReportActivity.class);
            startActivity(intent);
        });

        // Button for Safety Information
        Button btnSafetyInfo = findViewById(R.id.btn_safety_info);
        btnSafetyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SafetyInformationActivity.class);
            startActivity(intent);
        });

        // Button for Disaster Prone Area
        Button btnDisasterArea = findViewById(R.id.btn_disaster_area);
        btnDisasterArea.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DisasterProneAreaActivity.class);
            startActivity(intent);
        });
    }
}
