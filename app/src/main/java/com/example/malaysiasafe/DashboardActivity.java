package com.example.malaysiasafe;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap googleMap;
    private List<DisasterArea> disasterAreas;
    private static final float WARNING_RADIUS = 5000; // Radius in meters (5 km)
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Firebase Database reference
    private DatabaseReference disasterRef;
    TextView welcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Firebase database reference
        disasterRef = FirebaseDatabase.getInstance("https://malaysiasafe-4daeb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("DisasterLocation");

        // Initialize the disasterAreas list
        disasterAreas = new ArrayList<>();

        welcomeMessage = findViewById(R.id.welcomeMessage);
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            welcomeMessage.setText("Welcome, " + username);}

        loadDisasterLocationsFromFirebase();

        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Button for Evacuation Route
        Button btnEvacuationRoute = findViewById(R.id.btn_evacuation_route);
        btnEvacuationRoute.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, EvacuationRouteActivity.class);
            startActivity(intent);
        });

        // Button for Emergency Service
        Button btnEmergencyService = findViewById(R.id.btn_emergency_service);
        btnEmergencyService.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, EmergencyServiceActivity.class);
            startActivity(intent);
        });

        // Button for Community Report
        Button btnCommunityReport = findViewById(R.id.btn_community_report);
        btnCommunityReport.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, CommunityReportActivity.class);
            startActivity(intent);
        });

        // Button for Safety Information
        Button btnSafetyInfo = findViewById(R.id.btn_safety_info);
        btnSafetyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SafetyInformationActivity.class);
            startActivity(intent);
        });

        // Button for Disaster Prone Area
        Button btnDisasterArea = findViewById(R.id.btn_disaster_area);
        btnDisasterArea.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, DisasterProneAreaActivity.class);
            startActivity(intent);
        });

        // Check for disaster alerts
        checkForDisasterAlerts();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        googleMap.setMyLocationEnabled(true);
        moveToCurrentLocation();
    }

    private void moveToCurrentLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && googleMap != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f));
            }
        });
    }

    private void loadDisasterLocationsFromFirebase() {
        disasterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                disasterAreas.clear(); // Clear the list before adding new data

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("nameDisaster").getValue(String.class);
                    String locationStr = snapshot.child("location").getValue(String.class); // Get the location as a single string

                    if (name != null && locationStr != null) {
                        // Extract latitude and longitude from the string
                        String[] parts = locationStr.split(","); // Split the string by comma
                        if (parts.length == 2) {
                            try {
                                // Extract latitude and longitude from the parts
                                String latStr = parts[0].replace("Lat: ", "").trim(); // Remove "Lat : " and trim whitespace
                                String lngStr = parts[1].replace(" Lng: ", "").trim(); // Remove "Lng: " and trim whitespace

                                // Convert to double
                                double latitude = Double.parseDouble(latStr);
                                double longitude = Double.parseDouble(lngStr);

                                // Add to the disasterAreas list
                                disasterAreas.add(new DisasterArea(name, latitude, longitude, name));
                            } catch (NumberFormatException e) {
                                e.printStackTrace(); // Handle invalid latitude/longitude format
                            }
                        }
                    }
                }

                // Once data is loaded, check for alerts
                checkForDisasterAlerts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database read failure
            }
        });
    }


    private void checkForDisasterAlerts() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Pass the location object to find the nearest disaster location
                DisasterArea nearestDisaster = findNearestDisasterLocation(location);
                boolean isInDangerZone = nearestDisaster != null;

                // Update the UI with the result
                updateAlertUI(isInDangerZone, nearestDisaster);
            }
        });
    }


    private boolean isLocationNearDisaster(Location userLocation) {
        for (DisasterArea area : disasterAreas) {
            float[] results = new float[1];
            Location.distanceBetween(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    area.getLatitude(),
                    area.getLongitude(),
                    results
            );
            if (results[0] <= WARNING_RADIUS) {
                return true;
            }
        }
        return false;
    }

    private void updateAlertUI(boolean isInDangerZone, DisasterArea area) {
        TextView floodAlert = findViewById(R.id.flood_alert);
        if (isInDangerZone && area != null) {
            floodAlert.setText("Warning: " + area.getDisasterType() + " warning has been issued in your current location. Evacuate to nearest evacuation center now.");
            floodAlert.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

            googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(area.getLatitude(), area.getLongitude()))
                    .radius(WARNING_RADIUS)
                    .strokeColor(getResources().getColor(android.R.color.holo_red_dark))
                    .fillColor(0x44FF0000)
                    .strokeWidth(2f));

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(area.getLatitude(), area.getLongitude()), calculateZoomLevel(WARNING_RADIUS)));
        } else {
            floodAlert.setText("No alerts in your area.");
            floodAlert.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    private DisasterArea findNearestDisasterLocation(android.location.Location userLocation) {
        DisasterArea nearestDisaster = null;
        float minDistance = Float.MAX_VALUE;

        for (DisasterArea area : disasterAreas) {
            float[] results = new float[1];
            Location.distanceBetween(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    area.getLatitude(),
                    area.getLongitude(),
                    results
            );
            if (results[0] <= WARNING_RADIUS && results[0] < minDistance) {
                minDistance = results[0];
                nearestDisaster = area;
            }
        }
        return nearestDisaster;
    }


    private float calculateZoomLevel(double radius) {
        double scale = radius / 500;
        return (float) (16 - Math.log(scale) / Math.log(2));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            moveToCurrentLocation();
            checkForDisasterAlerts();
        }
    }
}
