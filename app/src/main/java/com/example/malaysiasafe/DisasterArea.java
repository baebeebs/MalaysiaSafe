package com.example.malaysiasafe;

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
}
