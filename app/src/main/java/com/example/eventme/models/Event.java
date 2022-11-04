package com.example.eventme.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Event {
    private String eventId;
    private String name;
    private String description;
    private Map<String, Boolean> types = new HashMap<>();
    private Double cost;
    private String date;
    private String time;
    private String location;
    private String sponsor;
    private Boolean parkingAvailable;
    private String photoURI;
    private Map<String, Double> geoLocation = new HashMap<>();
    private Map<String, Boolean> registeredUsers = new HashMap<>();

    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(Event.class)
    }

    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Boolean> getTypes() {
        return types;
    }

    public Double getCost() {
        return cost;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getSponsor() {
        return sponsor;
    }

    public Boolean getParkingAvailable() {
        return parkingAvailable;
    }

    public String getPhotoURI() {
        return photoURI;
    }

    public Map<String, Double> getGeoLocation() {
        return geoLocation;
    }

    public Map<String, Boolean> getRegisteredUsers() {
        return registeredUsers;
    }
}
