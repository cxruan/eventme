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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventme.R;
import com.example.eventme.databinding.FragmentExploreBinding;
import com.example.eventme.models.*;
import com.example.eventme.viewmodels.EventListFragmentViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class ExploreFragment extends Fragment {
    private static final String TAG = "ExploreFragment";

    private FragmentExploreBinding binding;

    private EventListFragmentViewModel mListViewModel;
    private FirebaseDatabase mDatabase;

    private String[] searchKeys = {"nameLowercase", "locationLowercase", "sponsorLowercase"};
    private String[] eventTypes = {"Music", "Arts", "Food & Drinks", "Outdoors"};
    boolean[] selectedTypes = new boolean[eventTypes.length];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance();

        // Binding view models
        mListViewModel = new ViewModelProvider(this).get(EventListFragmentViewModel.class);

        binding.searchBtn.setOnClickListener(this::onClickSearch);
        binding.searchBar.setOnEditorActionListener(this::onEditorAction);
        binding.typeFilter.setOnClickListener(this::onClickTypeFilter);
    }

    private void onClickTypeFilter(View view) {
        // Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // set title
        builder.setTitle("Select Type");

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
                loadSearchResultsByTypes();
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
        mListViewModel.clearEventData();
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
        String term = binding.searchBar.getText().toString().toLowerCase();

        clearTypeSelection();

        for (String searchKey : searchKeys) {
            mDatabase.getReference().child("events").orderByChild(searchKey).startAt(term).endAt(term + "\uf8ff").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DataSnapshot data : task.getResult().getChildren()) {
                        mListViewModel.addEventsData(data.getValue(Event.class));
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
                            mListViewModel.addEventsData(data.getValue(Event.class));
                        }
                    } else {
                        Log.e(TAG, "loadSearchResults: error loading events", task.getException());
                    }
                });
            }
        }

    }
}

