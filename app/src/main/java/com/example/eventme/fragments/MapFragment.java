package com.example.eventme.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventme.R;
import com.example.eventme.databinding.FragmentMapBinding;
import com.example.eventme.models.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private FragmentMapBinding binding;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ArrayList<Event> allEvents = new ArrayList<>();




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);

        // Initialize map fragment
        SupportMapFragment mapFragment=(SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return binding.getRoot();

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        getAllEvents(googleMap);
        for(Event event : allEvents) {

            System.out.println(event.getGeoLocation());

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("events");
        mAuth = FirebaseAuth.getInstance();


    }


    public void getAllEvents(GoogleMap googleMap){
        mDatabase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        GenericTypeIndicator<ArrayList<Event>> t = new GenericTypeIndicator<ArrayList<Event>>() {};
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            Event event = ds.getValue(Event.class);
                            allEvents.add(event);
                        }
                        System.out.println("yeahyeahyea");
                        for(Event event : allEvents){
                            double eventLon = event.getGeoLocation().get("lng");
                            double eventLat = event.getGeoLocation().get("lat");
                            googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(eventLat, eventLon))
                                    .title(event.getName()));
                            System.out.println(event.getName());
                            System.out.println(eventLon);
                            System.out.println(eventLat);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }



    public void isWithinDistance(int kilometers, double currentLon, double currentLat){
        System.out.println("yeahyeahyeah");
        ArrayList<Event> eventsInDistance = new ArrayList<>();
        for(Event event : allEvents){
            double eventLon = event.getGeoLocation().get("lng");
            double eventLat = event.getGeoLocation().get("lat");

            double dLat = Math.toRadians(currentLat - eventLat);
            double dLon = Math.toRadians(currentLon - eventLon);

            currentLat = Math.toRadians(currentLat);
            eventLat = Math.toRadians(eventLat);

            double a = Math.pow(Math.sin(dLat / 2), 2) +
                    Math.pow(Math.sin(dLon / 2), 2) *
                            Math.cos(eventLat) *
                            Math.cos(currentLat);
            double rad = 6371;
            double c = 2 * Math.asin(Math.sqrt(a));
            double distance = rad * c;
            System.out.println(event.getName());
            System.out.println(distance);
            if (distance < kilometers){
                eventsInDistance.add(event);
            }
        }
        allEvents = eventsInDistance;
    }
}
