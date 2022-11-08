package com.example.eventme.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventme.EventRegistrationActivity;
import com.example.eventme.R;
import com.example.eventme.adapters.EventBoxAdapter;
import com.example.eventme.databinding.FragmentEventListBinding;
import com.example.eventme.models.Event;
import com.example.eventme.viewmodels.EventListFragmentViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;


public class EventListFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "EventListFragment";

    private FragmentEventListBinding binding;

    private EventBoxAdapter mEventBoxAdapter;
    private LinearLayoutManager mManager;
    private RecyclerView mRecycler;
    private EventListFragmentViewModel mViewModel;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Permission request dialogue callback
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
            Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

            if (fineLocationGranted != null || coarseLocationGranted != null) {
                // location access granted.
            }
        });

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

        // Set up sort spinner
        ArrayAdapter<CharSequence> adapterSort = ArrayAdapter.createFromResource(getContext(), R.array.sortBy_array, android.R.layout.simple_spinner_item);
        adapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sortBySpinner.setAdapter(adapterSort);
        binding.sortBySpinner.setOnItemSelectedListener(this);

        // Set up view model, listening for events from parent fragment
        mViewModel = new ViewModelProvider(requireParentFragment()).get(EventListFragmentViewModel.class);
        mViewModel.getEventsData().observe(getViewLifecycleOwner(), events -> {
            binding.eventNum.setText(String.valueOf(events.size()));
            if (requireParentFragment() instanceof MapFragment) {
                binding.sortBySpinner.setSelection(3); // Defaults to Location if in map page
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
                try {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null)
                            events.sort(new Event.EventDistanceComparator(location.getLatitude(), location.getLongitude()));
                    });
                } catch (SecurityException e) {
                    Log.e("Exception: %s", e.getMessage(), e);
                }
            } else {
                binding.sortBySpinner.setSelection(0);
                events.sort(new Event.EventCostComparator());
            }
            mEventBoxAdapter.setItems(events);
            if (events.size() == 0)
                binding.emptyResultText.setVisibility(View.VISIBLE);
            else
                binding.emptyResultText.setVisibility(View.GONE);


        });
    }

    @Override
    public void onStart() {
        super.onStart();
        enableMyLocation();
    }

    private void enableMyLocation() {
        try {
            // 1. Check if permissions are granted
            if (isLocationPermissionGranted()) {
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

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String value = (String) parent.getItemAtPosition(position);
        List<Event> list = new ArrayList(mEventBoxAdapter.getAllItems());

        if (value.equals("Alphabetical")) {
            list.sort(new Event.EventNameComparator());
            mEventBoxAdapter.setItems(list);
        } else if (value.equals("Cost")) {
            list.sort(new Event.EventCostComparator());
            mEventBoxAdapter.setItems(list);
        } else if (value.equals("Date")) {
            list.sort(new Event.EventDateComparator());
            mEventBoxAdapter.setItems(list);
        } else if (value.equals("Location")) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            try {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location == null)
                        return;
                    list.sort(new Event.EventDistanceComparator(location.getLatitude(), location.getLongitude()));
                    mEventBoxAdapter.setItems(list);
                });
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage(), e);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
