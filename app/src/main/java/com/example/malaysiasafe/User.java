package com.example.malaysiasafe;


public class User {
    public String username;
    public String email;
    public String password;

    // Default constructor for Firebase
    public User() {
    }

    // Parameterized constructor for creating User objects
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}