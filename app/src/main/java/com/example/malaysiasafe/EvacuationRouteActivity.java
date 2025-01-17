package com.example.malaysiasafe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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
                if (dataSnapshot.exists()) {
                    for (DataSnapshot centerSnapshot : dataSnapshot.getChildren()) {
                        // Retrieve latitude and longitude as strings and parse to double
                        String latStr = centerSnapshot.child("Latitude").getValue(String.class);
                        String lngStr = centerSnapshot.child("Longitude").getValue(String.class);

                        if (latStr != null && lngStr != null) {
                            try {
                                double lat = Double.parseDouble(latStr);
                                double lng = Double.parseDouble(lngStr);
                                evacuationCenters.add(new LatLng(lat, lng));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Toast.makeText(EvacuationRouteActivity.this, "Invalid coordinates format", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    if (!evacuationCenters.isEmpty()) {
                        Toast.makeText(EvacuationRouteActivity.this, "Evacuation centers loaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EvacuationRouteActivity.this, "No evacuation centers found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EvacuationRouteActivity.this, "No evacuation centers found", Toast.LENGTH_SHORT).show();
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
                } else {
                    Toast.makeText(EvacuationRouteActivity.this, "No evacuation centers available", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            plotEvacuationRoute();
        }
    }
}
