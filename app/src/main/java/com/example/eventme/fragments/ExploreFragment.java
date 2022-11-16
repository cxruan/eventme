package com.example.eventme.fragments;

import static androidx.appcompat.content.res.AppCompatResources.getColorStateList;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventme.BuildConfig;
import com.example.eventme.R;
import com.example.eventme.databinding.FragmentExploreBinding;
import com.example.eventme.models.*;
import com.example.eventme.utils.Utils;
import com.example.eventme.viewmodels.EventListFragmentViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class ExploreFragment extends Fragment {
    private static final String TAG = "ExploreFragment";

    private FragmentExploreBinding binding;

    private EventListFragmentViewModel mListViewModel;
    private FirebaseDatabase mDatabase;

    private String[] searchKeys = {"nameLowercase", "locationLowercase", "sponsorLowercase"};
    private String[] eventTypes = {"Music", "Arts", "Food & Drinks", "Outdoors"};
    boolean[] selectedTypes = new boolean[eventTypes.length];
    private MaterialDatePicker<Pair<Long, Long>> mRangeDatePicker;
    private String mStartDate;
    private String mEndDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up Firebase Database
        mDatabase = FirebaseDatabase.getInstance();
        if (BuildConfig.DEBUG) {
            mDatabase.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_DATABASE_PORT);
        }

        // Set up range calender builder
        MaterialDatePicker.Builder<Pair<Long, Long>> materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker();
        materialDateBuilder.setTitleText("SELECT A DATE");
        mRangeDatePicker = materialDateBuilder.build();

        // Binding view models
        mListViewModel = new ViewModelProvider(this).get(EventListFragmentViewModel.class);

        // Binding click listeners
        binding.searchBtn.setOnClickListener(this::onClickSearch);
        binding.searchBar.setOnEditorActionListener(this::onEditorAction);
        binding.typeFilter.setOnClickListener(this::onClickTypeFilter);
        binding.dateFilter.setOnClickListener(this::onClickDateFilter);
        mRangeDatePicker.addOnPositiveButtonClickListener(this::onSelectRangeDate);
    }

    private void onSelectRangeDate(Pair<Long, Long> longLongPair) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        mStartDate = sdf.format(new Date(longLongPair.first));
        mEndDate = sdf.format(new Date(longLongPair.second));

        binding.dateFilter.setText(mStartDate + " - " + mEndDate);
        binding.dateFilter.setChipBackgroundColor(getColorStateList(getContext(), R.color.primary));
        binding.dateFilter.setChipIconTint(getColorStateList(getContext(), R.color.white));
        binding.dateFilter.setCloseIconTint(getColorStateList(getContext(), R.color.white));
        binding.dateFilter.setTextColor(getColorStateList(getContext(), R.color.white));
        binding.dateFilter.setCloseIconResource(R.drawable.ic_baseline_close_24);
        binding.dateFilter.setOnCloseIconClickListener(this::clearRangeFilter);
        loadSearchResults();
    }

    private void clearRangeFilter(View view) {
        mStartDate = null;
        mEndDate = null;
        binding.dateFilter.setText("Anytime");
        binding.dateFilter.setChipBackgroundColor(getColorStateList(getContext(), R.color.light_grey));
        binding.dateFilter.setChipIconTint(getColorStateList(getContext(), R.color.black));
        binding.dateFilter.setCloseIconTint(getColorStateList(getContext(), R.color.black));
        binding.dateFilter.setTextColor(getColorStateList(getContext(), R.color.black));
        binding.dateFilter.setCloseIconResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        loadSearchResults();
    }

    private void onClickDateFilter(View view) {
        mRangeDatePicker.show(getFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void onClickTypeFilter(View view) {
        // Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // set title
        builder.setTitle("Select Types");

        // set dialog non cancelable
        builder.setCancelable(false);

        builder.setMultiChoiceItems(eventTypes, selectedTypes, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
                selectedTypes[i] = isChecked;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean containsTrue = false;
                for (int j = 0; j < selectedTypes.length; j++) {
                    if (selectedTypes[j]) {
                        containsTrue = true;
                        break;
                    }
                }
                if (containsTrue) {
                    binding.typeFilter.setChipBackgroundColor(getColorStateList(getContext(), R.color.primary));
                    binding.typeFilter.setChipIconTint(getColorStateList(getContext(), R.color.white));
                    binding.typeFilter.setCloseIconTint(getColorStateList(getContext(), R.color.white));
                    binding.typeFilter.setTextColor(getColorStateList(getContext(), R.color.white));
                } else {
                    binding.typeFilter.setChipBackgroundColor(getColorStateList(getContext(), R.color.light_grey));
                    binding.typeFilter.setChipIconTint(getColorStateList(getContext(), R.color.black));
                    binding.typeFilter.setCloseIconTint(getColorStateList(getContext(), R.color.black));
                    binding.typeFilter.setTextColor(getColorStateList(getContext(), R.color.black));
                }
                if (mListViewModel.getEventsData().getValue().isEmpty())
                    loadSearchResultsByTypes();
                else
                    loadSearchResults();
            }
        });
        builder.setNeutralButton("Clear All", (dialogInterface, i) -> clearTypeSelection());

        builder.show();
    }

    private void clearTypeSelection() {
        for (int j = 0; j < selectedTypes.length; j++) {
            selectedTypes[j] = false;
        }
        binding.typeFilter.setChipBackgroundColor(getColorStateList(getContext(), R.color.light_grey));
        binding.typeFilter.setChipIconTint(getColorStateList(getContext(), R.color.black));
        binding.typeFilter.setCloseIconTint(getColorStateList(getContext(), R.color.black));
        binding.typeFilter.setTextColor(getColorStateList(getContext(), R.color.black));
        loadSearchResults();
    }

    private void onClickSearch(View view) {
        loadSearchResults();
    }

    private boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            loadSearchResults();
        }
        return false;
    }

    private void loadSearchResults() {
        mListViewModel.clearEventData();

        String term = binding.searchBar.getText().toString().toLowerCase();
        for (String searchKey : searchKeys) {
            mDatabase.getReference().child("events").orderByChild(searchKey).startAt(term).endAt(term + "\uf8ff").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
                    try {
                        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                            for (DataSnapshot data : task.getResult().getChildren()) {
                                Event event = data.getValue(Event.class);
                                if (location != null) {
                                    double distance = Utils.distanceBetweenLocations(location.getLatitude(), location.getLongitude(), event.getGeoLocation().get("lat"), event.getGeoLocation().get("lng"));
                                    event.setDistanceFromUserLocation(distance);
                                }
                                if (checkDate(event) && checkType(event))
                                    mListViewModel.addEventsData(event);
                            }
                        });
                    } catch (SecurityException e) {
                        Log.e("Exception: %s", e.getMessage(), e);
                    }
                } else {
                    Log.e(TAG, "loadSearchResults: error loading events", task.getException());
                }
            });
        }
    }

    private void loadSearchResultsByTypes() {
        mListViewModel.clearEventData();

        for (int j = 0; j < selectedTypes.length; j++) {
            if (selectedTypes[j]) {
                mDatabase.getReference().child("events").orderByChild("types/" + eventTypes[j].toLowerCase()).equalTo(true).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DataSnapshot data : task.getResult().getChildren()) {
                            Event event = data.getValue(Event.class);
                            mListViewModel.addEventsData(event);
                        }
                    } else {
                        Log.e(TAG, "loadSearchResults: error loading events", task.getException());
                    }
                });
            }
        }
    }

    private boolean checkType(Event event) {
        for (int i = 0; i < selectedTypes.length; i++) {
            if (selectedTypes[i]) {
                if (!event.getTypes().containsKey(eventTypes[i].toLowerCase()))
                    return false;
            }
        }
        return true;
    }

    private boolean checkDate(Event event) {
        if (mStartDate == null || mEndDate == null)
            return true;
        if (event.getDate().compareTo(mStartDate) >= 0 && event.getDate().compareTo(mEndDate) <= 0)
            return true;
        return false;
    }
}

