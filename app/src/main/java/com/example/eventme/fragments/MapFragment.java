package com.example.eventme.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventme.R;
import com.example.eventme.databinding.FragmentMapBinding;
import com.example.eventme.models.Event;
import com.example.eventme.viewmodels.MapFragmentViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";
    private static final Integer DEFAULT_ZOOM_LEVEL = 15;

    private FragmentMapBinding binding;

    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private MapFragmentViewModel mViewModel;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Binding view model
        mViewModel = new ViewModelProvider(requireActivity()).get(MapFragmentViewModel.class);

        // Permission request dialogue callback
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
            Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

            if (fineLocationGranted != null || coarseLocationGranted != null) {
                // location access granted.
                enableMyLocation();
                loadMapData();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setMapToolbarEnabled(false);
        enableMyLocation();

        if (isLocationPermissionGranted())
            loadMapData();
    }


    private void loadMapData() {
        try {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, DEFAULT_ZOOM_LEVEL));

                mViewModel.getEventsData().observe(getViewLifecycleOwner(), events -> {
                    for (Event event : events) {
                        double eventLon = event.getGeoLocation().get("lng");
                        double eventLat = event.getGeoLocation().get("lat");

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(eventLat, eventLon))
                                .title(event.getName()));
                    }

                });
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }

    }

    private void enableMyLocation() {
        try {
            // 1. Check if permissions are granted, if so, enable the my location layer
            if (isLocationPermissionGranted()) {
                mMap.setMyLocationEnabled(true);
                return;
            }

            // 2. Otherwise, request location permissions from the user.
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public List<Event> filterWithinDistance(int kilometers, double currentLon, double currentLat, List<Event> events) {
        ArrayList<Event> eventsInDistance = new ArrayList<>();
        for (Event event : events) {
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

            if (distance < kilometers) {
                eventsInDistance.add(event);
            }
        }
        return eventsInDistance;
    }
}
