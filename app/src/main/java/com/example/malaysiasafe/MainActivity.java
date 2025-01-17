package com.example.malaysiasafe;

import android.Manifest;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap googleMap;
    private List<DisasterArea> disasterAreas;
    private static final float WARNING_RADIUS = 5000; // Radius in meters (5 km)
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Define disaster locations with disaster types
        disasterAreas = new ArrayList<>();
        disasterAreas.add(new DisasterArea("Kuala Lumpur", 3.139, 101.6869, "Flood"));
        disasterAreas.add(new DisasterArea("Petaling Jaya", 3.1579, 101.7114, "Landslide"));
        disasterAreas.add(new DisasterArea("Pekan", 3.4991, 103.3860, "Flood"));

        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Button for Evacuation Route
        Button btnEvacuationRoute = findViewById(R.id.btn_evacuation_route);
        btnEvacuationRoute.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EvacuationRouteActivity.class);
            startActivity(intent);
        });

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

        // Check for disaster alerts
        checkForDisasterAlerts();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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

    private void checkForDisasterAlerts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                boolean isInDangerZone = isLocationNearDisaster(location);

                // Update UI with both the danger status and the disaster location
                DisasterArea dangerLocation = null;
                if (isInDangerZone) {
                    // You can retrieve the disaster location that triggered the warning
                    dangerLocation = findNearestDisasterLocation(location);
                }
                updateAlertUI(isInDangerZone, dangerLocation); // Pass both parameters
            }
        });

    }

    private boolean isLocationNearDisaster(Location userLocation) {
        for (DisasterArea area : disasterAreas) {
            float[] results = new float[1];
            Location.distanceBetween(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    area.latitude,
                    area.longitude,
                    results
            );
            if (results[0] <= WARNING_RADIUS) {
                updateAlertUI(true, area); // Pass the disaster area
                return true;
            }
        }
        updateAlertUI(false, null); // Pass null if no disaster zone
        return false;
    }

    private void updateAlertUI(boolean isInDangerZone, DisasterArea area) {
        TextView floodAlert = findViewById(R.id.flood_alert);
        if (isInDangerZone && area != null) {
            floodAlert.setText("Warning: " + area.disasterType + " warning has been issued in your current location. Evacuate to nearest evacuation center now.");
            floodAlert.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

            // Add a circle to represent the disaster area
            googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(area.latitude, area.longitude))
                    .radius(WARNING_RADIUS)
                    .strokeColor(getResources().getColor(android.R.color.holo_red_dark))
                    .fillColor(0x44FF0000) // Semi-transparent red
                    .strokeWidth(2f));

            // Zoom out to show the entire radius
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(area.latitude, area.longitude), calculateZoomLevel(WARNING_RADIUS)));
        } else {
            floodAlert.setText("No alerts in your area.");
            floodAlert.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    private DisasterArea findNearestDisasterLocation(Location userLocation) {
        DisasterArea nearestDisaster = null;
        float[] results = new float[1];
        for (DisasterArea area : disasterAreas) {
            Location.distanceBetween(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    area.latitude,
                    area.longitude,
                    results
            );
            if (results[0] <= WARNING_RADIUS) {
                nearestDisaster = area; // This is the closest disaster location
                break; // Stop searching once we find the nearest disaster location
            }
        }
        return nearestDisaster;
    }

    private float calculateZoomLevel(double radius) {
        double scale = radius / 500; // Adjust scale factor based on map size
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
