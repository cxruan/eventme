package com.example.eventme.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventme.EventRegistrationActivity;
import com.example.eventme.R;
import com.example.eventme.adapters.EventBoxAdapter;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

// TODO: Filter events given user's current location
public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener {

    private static final String TAG = "MapFragment";
    private static final Integer DEFAULT_ZOOM_LEVEL = 15;
    private static final Integer EVENT_LIST_CARD_OFFSET_DP= 85;
    private static final Integer EVENT_LIST_CARD_RADIUS_DP= 20;

    private FragmentMapBinding binding;

    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private MapFragmentViewModel mViewModel;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private CardView mEventBoxView;
    private EventBoxAdapter mEventBoxAdapter;
    private LinearLayoutManager mManager;
    RecyclerView mRecycler;

    private float pxInDp, dY, startY, initialY = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);

        mEventBoxView = binding.eventBox.getRoot();
        mEventBoxView.setVisibility(View.INVISIBLE);

        pxInDp = getResources().getDisplayMetrics().density;
        // Set up event list card swiping animation
        binding.eventListCard.setOnTouchListener((vw, event) -> {
            CardView view = (CardView) vw;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = view.getY();
                    if (initialY == 0) // record card's initial y
                        initialY = startY;
                    dY = view.getY() - event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getRawY() + dY < 0 || event.getRawY() + dY > initialY)
                        return true;
                    view.animate()
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                    float offsetY = event.getRawY() + dY - startY;
                    if (offsetY > 300) { // Swiping down
                        view.animate()
                                .y(initialY)
                                .setDuration(300)
                                .start();
                        view.setRadius(EVENT_LIST_CARD_RADIUS_DP * pxInDp);
                    } else if (offsetY < -300) { // Swiping up
                        view.animate()
                                .y(0)
                                .setDuration(300)
                                .start();
                        view.setRadius(0);
                    } else {
                        view.animate()
                                .y(startY)
                                .setDuration(300)
                                .start();
                    }
                    break;
                default:
                    return false;
            }
            return true;
        });

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

        // Set up Adapter
        mEventBoxAdapter = new EventBoxAdapter();
        mEventBoxAdapter.setOnItemClickListener((position, v) -> {
            // Pass eventId to Registration activity when clicking event box
            Event event = mEventBoxAdapter.getItemByPos(position);
            Intent intent = new Intent(requireActivity(), EventRegistrationActivity.class);
            intent.putExtra("com.example.eventme.EventRegistration.eventId", event.getEventId());
            startActivity(intent);
        });

        // Set up RecyclerView
        mManager = new LinearLayoutManager(getActivity());
        mRecycler = binding.eventList;
        mRecycler.setLayoutManager(mManager);
        mRecycler.setAdapter(mEventBoxAdapter);

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
        mMap.setPadding(0, 0, 0, Math.round(EVENT_LIST_CARD_OFFSET_DP * pxInDp));
        enableMyLocation();

        if (isLocationPermissionGranted())
            loadMapData();

        // Add listeners
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
    }

    private void loadMapData() {
        try {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                // Clear map and view model observers
                mViewModel.getEventsData().removeObservers(getViewLifecycleOwner());
                mMap.clear();

                LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, DEFAULT_ZOOM_LEVEL));

                mViewModel.getEventsData().observe(getViewLifecycleOwner(), events -> {
                    for (Event event : events.values()) {
                        double eventLon = event.getGeoLocation().get("lng");
                        double eventLat = event.getGeoLocation().get("lat");

                        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(eventLat, eventLon)));
                        marker.setTag(event.getEventId());
                    }

                    List<Event> orderedEvents = new ArrayList<>(events.values());
                    mEventBoxAdapter.setItems(orderedEvents);
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

    @Override
    public boolean onMarkerClick(final Marker marker) {
        String eventId = (String) marker.getTag();
        showEventBox(eventId);

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    private void showEventBox(String eventId) {
        // Bind view holder
        EventBoxAdapter.ViewHolder vh = new EventBoxAdapter.ViewHolder(mEventBoxView);
        vh.bind(mViewModel.getEventById(eventId));

        // Bind onClickListener to go to registration page
        mEventBoxView.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), EventRegistrationActivity.class);
            intent.putExtra("com.example.eventme.EventRegistration.eventId", eventId);
            startActivity(intent);
        });


        mEventBoxView.setVisibility(View.VISIBLE);

        // Get actual height
        mEventBoxView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mEventBoxView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Animate
                ObjectAnimator animation = ObjectAnimator.ofFloat(mEventBoxView, "translationY", mEventBoxView.getMeasuredHeight() + 100, 0f);
                animation.setDuration(300);
                animation.start();

                mMap.setPadding(0, 0, 0, mEventBoxView.getMeasuredHeight() + Math.round(EVENT_LIST_CARD_OFFSET_DP * pxInDp)); // Set Google Map control position
            }
        });
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Animate
        ObjectAnimator animation = ObjectAnimator.ofFloat(mEventBoxView, "translationY", 0f, 200 * pxInDp);
        animation.setDuration(300);
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                mEventBoxView.setVisibility(View.INVISIBLE);
                mMap.setPadding(0, 0, 0, Math.round(EVENT_LIST_CARD_OFFSET_DP * pxInDp)); // Set Google Map control position
            }
        });
        animation.start();
    }
}
