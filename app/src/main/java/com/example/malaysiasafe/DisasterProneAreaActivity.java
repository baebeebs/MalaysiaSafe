package com.example.malaysiasafe;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CircleOptions;

import java.util.Arrays;
import java.util.List;

public class DisasterProneAreaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disaster_prone_area);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Disaster-prone areas in Malaysia
        List<DisasterArea> disasterAreas = Arrays.asList(
                new DisasterArea("Kuala Lumpur", 3.139, 101.6869, "Flood"),
                new DisasterArea("Penang", 5.4164, 100.3327, "Landslide"),
                new DisasterArea("Johor Bahru", 1.4927, 103.7414, "Flood"),
                new DisasterArea("Pekan", 3.4991, 103.3860, "Flood"),
                new DisasterArea("Kota Kinabalu", 5.9804, 116.0735, "Earthquake")
        );

        // Add markers and highlight areas
        for (DisasterArea area : disasterAreas) {
            LatLng location = new LatLng(area.latitude, area.longitude);
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(area.name)
                    .snippet("Disaster Type: " + area.disasterType));

            // Add a circle to highlight the area
            mMap.addCircle(new CircleOptions()
                    .center(location)
                    .radius(10000) // Radius in meters
                    .strokeColor(getColorForDisaster(area.disasterType))
                    .fillColor(getColorForDisaster(area.disasterType, true))
                    .strokeWidth(2f));
        }

        // Focus camera on Malaysia
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(4.2105, 101.9758), 6));
    }

    // Get stroke and fill color for disaster type
    private int getColorForDisaster(String disasterType) {
        switch (disasterType) {
            case "Flood":
                return Color.BLUE;
            case "Landslide":
                return Color.RED;
            case "Earthquake":
                return Color.YELLOW;
            default:
                return Color.GRAY;
        }
    }

    private int getColorForDisaster(String disasterType, boolean isFill) {
        int color = getColorForDisaster(disasterType);
        return isFill ? Color.argb(50, Color.red(color), Color.green(color), Color.blue(color)) : color;
    }


}
