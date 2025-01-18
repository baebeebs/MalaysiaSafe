package com.example.malaysiasafe;

public class ShowReport {
    private String location;
    private String nameDisaster;
    private String time;
    private String date;
    private String contactNumber;

    public ShowReport() {
        // Default constructor required for Firebase
    }

    public ShowReport(String location, String nameDisaster, String time, String date, String contactNumber) {
        this.location = location;
        this.nameDisaster = nameDisaster;
        this.time = time;
        this.date = date;
        this.contactNumber = contactNumber;
    }

    // Getters and setters
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNameDisaster() {
        return nameDisaster;
    }

    public void setNameDisaster(String nameDisaster) {
        this.nameDisaster = nameDisaster;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
