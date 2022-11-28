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
    private String profilePictureURI;
    private Map<String, Boolean> registeredEvents = new HashMap<>();
    private Map<String, Boolean> savedEvents = new HashMap<>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String firstName, String lastName, String email, String birthday) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthday = birthday;
    }

    public String getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }


    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getProfilePictureURI() {
        return profilePictureURI;
    }

    public Map<String, Boolean> getRegisteredEvents() {
        return registeredEvents;
    }

    public Map<String, Boolean> getSavedEvents() {
        return savedEvents;
    }

}