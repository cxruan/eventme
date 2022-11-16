package com.example.eventme.models;

import static com.example.eventme.utils.Utils.distanceBetweenLocations;

import android.util.Log;

import com.example.eventme.utils.Utils;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private Double distanceFromUserLocation;

    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(Event.class)
    }

    // Used for generating Event tests
    public Event(String eventId) {
        this.eventId = eventId;
    }

    // Firebase Database populated fields
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

    // self-populated fields
    public Double getDistanceFromUserLocation() {
        return distanceFromUserLocation;
    }

    public void setDistanceFromUserLocation(Double distanceFromUserLocation) {
        this.distanceFromUserLocation = distanceFromUserLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(eventId, event.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    public static boolean checkTimeConflict(Event e1, Event e2) {
        if (!e1.getDate().equals(e2.getDate()))
            return false;

        Integer[] time1 = Utils.parseTime(e1.getTime());
        Integer[] time2 = Utils.parseTime(e2.getTime());
        Log.d("Event", String.format("checkTimeConflict: %d:%d-%d:%d %d:%d-%d:%d", time1[0], time1[1], time1[2], time1[3], time2[0], time2[1], time2[2], time2[3]));

        Integer startHour1, startMin1, endHour1, endMin1;
        Integer startHour2, startMin2, endHour2, endMin2;

        if (time1[0] < time2[0]) {
            startHour1 = time1[0];
            startMin1 = time1[1];
            endHour1 = time1[2];
            endMin1 = time1[3];
            startHour2 = time2[0];
            startMin2 = time2[1];
            endHour2 = time2[2];
            endMin2 = time2[3];
        } else {
            startHour1 = time2[0];
            startMin1 = time2[1];
            endHour1 = time2[2];
            endMin1 = time2[3];
            startHour2 = time1[0];
            startMin2 = time1[1];
            endHour2 = time1[2];
            endMin2 = time1[3];
        }

        if (endHour1 > startHour2)
            return true;
        if (endHour1 == startHour2 && endMin1 >= startMin2)
            return true;


        return false;
    }

    public static class EventNameComparator implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            return e1.getName().compareTo(e2.getName());
        }
    }

    public static class EventCostComparator implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            return e1.getCost().compareTo(e2.getCost());
        }
    }

    public static class EventDateComparator implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            return e1.getDate().compareTo(e2.getDate());
        }
    }

    public static class EventDistanceComparator implements Comparator<Event> {
        double originalLat;
        double originalLng;

        public EventDistanceComparator(double lat, double lng) {
            super();
            originalLat = lat;
            originalLng = lng;
        }

        @Override
        public int compare(Event e1, Event e2) {
            double d1 = distanceBetweenLocations(originalLat, originalLng, e1.getGeoLocation().get("lat"), e1.getGeoLocation().get("lng"));
            double d2 = distanceBetweenLocations(originalLat, originalLng, e2.getGeoLocation().get("lat"), e2.getGeoLocation().get("lng"));

            if (d1 < d2)
                return -1;
            if (d1 > d2)
                return 1;
            return 0;
        }
    }
}
