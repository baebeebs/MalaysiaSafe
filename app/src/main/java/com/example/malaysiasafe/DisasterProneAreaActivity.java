package com.example.malaysiasafe;

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

        // Disaster-prone areas data
        List<Object[]> disasterAreas = Arrays.asList(
                new Object[] {"Kuala Lumpur", 3.139, 101.6869, "Flood"},
                new Object[] {"Penang", 5.4164, 100.3327, "Landslide"},
                new Object[] {"Johor Bahru", 1.4927, 103.7414, "Flood"},
                new Object[] {"Pekan", 3.4991, 103.3860, "Flood"},
                new Object[] {"Kota Kinabalu", 5.9804, 116.0735, "Earthquake"},
                new Object[] {"Sri Muda, Shah Alam", 3.0726, 101.5485, "Flood"},
                new Object[] {"Taman Seri Pantai, Kota Bharu", 6.1004, 102.2899, "Flood"},
                new Object[] {"Kuala Krai", 5.5369, 102.2324, "Flood"},
                new Object[] {"Pantai Cahaya Bulan, Kota Bharu", 6.1132, 102.2721, "Coastal Erosion, Flood"},
                new Object[] {"Sungai Besi, Kuala Lumpur", 3.0705, 101.6872, "Flood"},
                new Object[] {"Gua Musang", 4.8929, 102.1392, "Landslide, Flood"},
                new Object[] {"Taman Melawati, Kuala Lumpur", 3.2252, 101.7746, "Landslide"},
                new Object[] {"Mersing", 2.2806, 103.8477, "Flood, Storm Surge"},
                new Object[] {"Batu Pahat", 1.8595, 102.9383, "Flood"},
                new Object[] {"Taman Rinting, Johor Bahru", 1.5141, 103.7747, "Flood"},
                new Object[] {"Kuching", 1.5531, 110.359, "Flood, Landslide"},
                new Object[] {"Kota Tinggi", 1.8203, 103.9074, "Flood"},
                new Object[] {"Sabah (Tuaran)", 6.0143, 116.0606, "Flood, Earthquake"},
                new Object[] {"Labuan", 5.2843, 115.2077, "Flood, Earthquake"},
                new Object[] {"Taman Bukit Indah, Johor Bahru", 1.4965, 103.6603, "Flood"},
                new Object[] {"Kuantan", 3.814, 103.323, "Flood, Landslide"},
                new Object[] {"Temerloh", 3.4567, 102.3411, "Flood"},
                new Object[] {"Bentong", 3.2325, 101.8871, "Landslide, Flood"},
                new Object[] {"Raub", 3.3585, 101.8192, "Flood, Landslide"},
                new Object[] {"Maran", 3.4583, 102.3114, "Flood"},
                new Object[] {"Lipis", 4.1685, 102.1156, "Flood"},
                new Object[] {"Jelebu", 3.0814, 102.0303, "Flood"},
                new Object[] {"Sungai Lembing", 3.1123, 102.1061, "Flood, Landslide"},
                new Object[] {"Benta", 3.2475, 102.3819, "Flood"},
                new Object[] {"Kuala Terengganu", 5.3320, 103.1395, "Flood, Storm Surge"},
                new Object[] {"Kemaman", 4.2441, 103.4173, "Flood, Storm Surge"},
                new Object[] {"Marang", 5.2524, 103.1565, "Flood, Coastal Erosion"},
                new Object[] {"Dungun", 4.7615, 103.2131, "Flood, Landslide"},
                new Object[] {"Besut", 5.8095, 102.8581, "Flood"},
                new Object[] {"Tumpat", 6.1771, 102.2769, "Flood, Coastal Erosion"},
                new Object[] {"Chukai, Kemaman", 4.2397, 103.4182, "Flood"},
                new Object[] {"Paka", 4.5062, 103.4154, "Flood, Coastal Erosion"},
                new Object[] {"Kijal, Kemaman", 4.2850, 103.4230, "Flood"},
                new Object[] {"Bachok", 5.7437, 102.2532, "Flood, Coastal Erosion"},
                new Object[] {"Tanah Merah", 5.8165, 102.1317, "Flood"},
                new Object[] {"Gua Musang", 4.8929, 102.1392, "Landslide, Flood"},
                new Object[] {"Jerteh", 5.6724, 102.5275, "Flood"},
                new Object[] {"Pulau Perhentian", 5.9302, 102.7023, "Storm Surge, Coastal Erosion"},
                new Object[] {"Kuala Besut", 5.8222, 102.5728, "Flood, Coastal Erosion"},
                new Object[] {"Batu Rakit", 5.2803, 103.0979, "Flood, Coastal Erosion"},
                new Object[] {"Setiu", 5.6727, 102.9435, "Flood, Coastal Erosion"}


        );

        // Add markers and highlight areas
        for (Object[] area : disasterAreas) {
            String areaName = (String) area[0];
            double latitude = (double) area[1];
            double longitude = (double) area[2];
            String disasterType = (String) area[3];

            LatLng location = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(areaName)
                    .snippet("Disaster Type: " + disasterType));

            // Add a circle to highlight the area
            mMap.addCircle(new CircleOptions()
                    .center(location)
                    .radius(1000) // Radius in meters
                    .strokeColor(getColorForDisaster(disasterType))
                    .fillColor(getColorForDisaster(disasterType, true))
                    .strokeWidth(2f));
        }

        // Focus camera on Malaysia
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(4.2105, 101.9758), 8));
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
