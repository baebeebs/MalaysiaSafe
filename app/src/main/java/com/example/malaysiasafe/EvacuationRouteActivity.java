package com.example.malaysiasafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;

public class EvacuationRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LatLng currentLocation;
    private LatLng nearestCenter;
    private final List<LatLng> evacuationCenters = new ArrayList<>(); // Evacuation centers fetched from Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evacuation_route);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize buttons
        Button btnCurrentLocation = findViewById(R.id.btn_current_location);
        Button btnStartRoute = findViewById(R.id.btn_start_route);

        Button btnAddCenter = findViewById(R.id.btn_add_new_centre);
        btnAddCenter.setOnClickListener(v -> {
            if (currentLocation != null) {
                addEvacuationCenterToFirebase(currentLocation);
            } else {
                Toast.makeText(EvacuationRouteActivity.this, "Current location is unavailable", Toast.LENGTH_SHORT).show();
            }
        });


        // Set listeners for buttons
        btnCurrentLocation.setOnClickListener(v -> plotEvacuationRoute());
        btnStartRoute.setOnClickListener(v -> {
            if (currentLocation != null && nearestCenter != null) {
                fetchRoute(currentLocation, nearestCenter);
            } else {
                Toast.makeText(EvacuationRouteActivity.this, "Please find your location first", Toast.LENGTH_SHORT).show();
            }
        });
        // Fetch evacuation centers from Firebase
        fetchEvacuationCentersFromFirebase();
    }
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        googleMap.setMyLocationEnabled(true);
    }
    private void fetchEvacuationCentersFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://malaysiasafe-4daeb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("EvacuationCentre");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                evacuationCenters.clear();  // Clear previous list
                if (dataSnapshot.exists()) {
                    for (DataSnapshot centerSnapshot : dataSnapshot.getChildren()) {
                        String latStr = centerSnapshot.child("Latitude").getValue(String.class);
                        String lngStr = centerSnapshot.child("Longitude").getValue(String.class);

                        if (latStr != null && lngStr != null) {
                            try {
                                double lat = Double.parseDouble(latStr);
                                double lng = Double.parseDouble(lngStr);
                                LatLng centerLatLng = new LatLng(lat, lng);
                                evacuationCenters.add(centerLatLng);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    addEvacuationCenterMarkers();  // Add updated markers to map
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EvacuationRouteActivity.this, "Failed to load evacuation centers", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void plotEvacuationRoute() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && googleMap != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                nearestCenter = findNearestEvacuationCenter(currentLocation);

                if (nearestCenter != null) {
                    // Add markers for current location and evacuation center
                    googleMap.clear();  // Clear previous markers and polylines
                    googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Your Location"));
                    googleMap.addMarker(new MarkerOptions().position(nearestCenter).title("Nearest Evacuation Center"));

                    // Move camera to current location
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12f));

                    // Re-add markers for evacuation centers after clearing previous markers
                    addEvacuationCenterMarkers();
                } else {
                    Toast.makeText(EvacuationRouteActivity.this, "No evacuation centers available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("PotentialBehaviorOverride")
    private void addEvacuationCenterMarkers() {
        for (LatLng center : evacuationCenters) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(center)
                    .title("Evacuation Center"));

            if (marker != null) {
                marker.setTag(center);  // Tag the marker with its LatLng for easy identification

                googleMap.setOnMarkerClickListener(clickedMarker -> {
                    LatLng clickedCenter = (LatLng) clickedMarker.getTag();
                    if (clickedCenter != null) {
                        // Show confirmation dialog for deletion
                        new AlertDialog.Builder(EvacuationRouteActivity.this)
                                .setTitle("Delete Evacuation Center")
                                .setMessage("Are you sure you want to delete this evacuation center?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    deleteEvacuationCenterFromFirebase(clickedCenter);  // Delete from Firebase
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                    return false;
                });
            }
        }
    }




    private LatLng findNearestEvacuationCenter(LatLng currentLocation) {
        LatLng nearest = null;
        float shortestDistance = Float.MAX_VALUE;
        float[] results = new float[1];

        for (LatLng center : evacuationCenters) {
            Location.distanceBetween(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    center.latitude,
                    center.longitude,
                    results
            );

            if (results[0] < shortestDistance) {
                shortestDistance = results[0];
                nearest = center;
            }
        }
        return nearest;
    }

    private void fetchRoute(LatLng origin, LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&key=AIzaSyDDDT9hf_3F7hXwHGEsd8FQOyG-dGFzXEU"; // Replace with your actual API key

        OkHttpClient client = new OkHttpClient();

        new Thread(() -> {
            try {
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray routes = json.getJSONArray("routes");

                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                        String encodedPolyline = overviewPolyline.getString("points");

                        List<LatLng> points = PolyUtil.decode(encodedPolyline);
                        runOnUiThread(() -> googleMap.addPolyline(new PolylineOptions()
                                .addAll(points)
                                .width(10)
                                .color(Color.BLUE))); // Customize polyline
                    }
                } else {
                    throw new Exception("HTTP Request failed: " + response.code());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to fetch route", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void addEvacuationCenterToFirebase(LatLng centerLocation) {
        String centerName = ((EditText) findViewById(R.id.edit_center_name)).getText().toString().trim();

        if (centerName.isEmpty()) {
            Toast.makeText(EvacuationRouteActivity.this, "Please enter a center name", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://malaysiasafe-4daeb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("EvacuationCentre");

        // Create a unique key for the new evacuation center
        String key = databaseReference.push().getKey();

        if (key != null) {
            // Create a map of the new center data
            databaseReference.child(key).child("Name").setValue(centerName);
            databaseReference.child(key).child("Latitude").setValue(String.valueOf(centerLocation.latitude));
            databaseReference.child(key).child("Longitude").setValue(String.valueOf(centerLocation.longitude));

            // Update the map by adding the new marker
            googleMap.addMarker(new MarkerOptions()
                    .position(centerLocation)
                    .title(centerName));  // Display the center name in the marker title

            Toast.makeText(EvacuationRouteActivity.this, "Evacuation Center added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(EvacuationRouteActivity.this, "Failed to add evacuation center", Toast.LENGTH_SHORT).show();
        }
    }


    private void deleteEvacuationCenterFromFirebase(LatLng centerLocation) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://malaysiasafe-4daeb-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("EvacuationCentre");

        // Fetch all evacuation centers and look for the one matching the centerLocation
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot centerSnapshot : dataSnapshot.getChildren()) {
                    String latStr = centerSnapshot.child("Latitude").getValue(String.class);
                    String lngStr = centerSnapshot.child("Longitude").getValue(String.class);

                    if (latStr != null && lngStr != null) {
                        double lat = Double.parseDouble(latStr);
                        double lng = Double.parseDouble(lngStr);

                        LatLng centerLatLng = new LatLng(lat, lng);
                        if (centerLatLng.equals(centerLocation)) {
                            String key = centerSnapshot.getKey();
                            if (key != null) {
                                // Remove center from Firebase
                                databaseReference.child(key).removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EvacuationRouteActivity.this, "Evacuation Center deleted", Toast.LENGTH_SHORT).show();
                                                googleMap.clear();  // Clear all markers
                                                fetchEvacuationCentersFromFirebase();  // Re-fetch updated list of centers
                                            } else {
                                                Toast.makeText(EvacuationRouteActivity.this, "Failed to delete evacuation center", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EvacuationRouteActivity.this, "Failed to load evacuation centers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            plotEvacuationRoute();
        }
    }

}