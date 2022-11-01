package com.example.eventme.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String birthday;
    private String profilePicture;
    private Map<String, Boolean>  registeredEvents = new HashMap<>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String firstName, String lastName, String email) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }


    public String getLastName() {
        return lastName;
    }


    public String getEmail() {
        return email;
    }


    public String getBirthday() {
        return birthday;
    }


    public String getProfilePicture() {
        return profilePicture;
    }

    public Map<String, Boolean> getRegisteredEvents() {
        return registeredEvents;
    }

}