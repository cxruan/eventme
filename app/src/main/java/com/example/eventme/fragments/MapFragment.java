package com.example.eventme.fragments;

import static com.example.eventme.utils.Utils.distanceBetweenLocations;

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

import com.example.eventme.EventRegistrationActivity;
import com.example.eventme.R;
import com.example.eventme.adapters.EventBoxAdapter;
import com.example.eventme.databinding.FragmentMapBinding;
import com.example.eventme.models.Event;
import com.example.eventme.viewmodels.EventListFragmentViewModel;
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

public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener {

    private static final String TAG = "MapFragment";
    private static final Integer DEFAULT_ZOOM_LEVEL = 15;
    private static final Integer EVENT_LIST_CARD_OFFSET_DP = 85;
    private static final Integer EVENT_LIST_CARD_RADIUS_DP = 20;
    private static final Integer SHOW_EVENTS_IN_KM = 50;

    private FragmentMapBinding binding;

    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private MapFragmentViewModel mViewModel;
    private EventListFragmentViewModel mListViewModel;
    private CardView mEventBoxView;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;

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
        binding.eventListCard.setOnTouchListener(this::onListTouchListener);

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

        // Binding view models
        mViewModel = new ViewModelProvider(requireActivity()).get(MapFragmentViewModel.class);
        mListViewModel = new ViewModelProvider(this).get(EventListFragmentViewModel.class);

        // Permission request dialogue callback
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
            Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
            Log.d(TAG, String.format("onViewCreated: %s %s ", fineLocationGranted, coarseLocationGranted));
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
                if(location == null)
                    return;
                // Clear map and view model observers
                mViewModel.getEventsData().removeObservers(getViewLifecycleOwner());
                mMap.clear();

                LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, DEFAULT_ZOOM_LEVEL));

                mViewModel.loadAllData();
                mViewModel.getEventsData().observe(getViewLifecycleOwner(), events -> {
                    List<Event> orderedList = new ArrayList<>();

                    for (Event event : events.values()) {
                        double eventLon = event.getGeoLocation().get("lng");
                        double eventLat = event.getGeoLocation().get("lat");

                        double distance = distanceBetweenLocations(coordinate.latitude, coordinate.longitude, eventLat, eventLon);
                        if (distance < SHOW_EVENTS_IN_KM) {
                            event.setDistanceFromUserLocation(distance);
                            orderedList.add(event);
                            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(eventLat, eventLon)));
                            marker.setTag(event.getEventId());
                        }
                    }

                    orderedList.sort(new Event.EventDistanceComparator(coordinate.latitude, coordinate.longitude));
                    mListViewModel.setEventsData(orderedList);
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

            // 2. Otherwise, request location permissions from the user.œœ
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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

    // Close event box view
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

    private boolean onListTouchListener(View vw, MotionEvent event) {
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
    }
}
