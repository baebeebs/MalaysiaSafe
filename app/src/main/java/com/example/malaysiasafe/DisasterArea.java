package com.example.malaysiasafe;

import com.google.android.gms.maps.model.LatLng;

public class DisasterArea {
    String name;
    double latitude;
    double longitude;
    String disasterType;

    public DisasterArea(String name, double latitude, double longitude, String disasterType) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.disasterType = disasterType;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public String getDisasterType() {
        return disasterType;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;

    }
}
