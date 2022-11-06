package com.example.eventme.fragments;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventme.databinding.FragmentExploreBinding;
import com.example.eventme.models.*;
import com.example.eventme.viewmodels.EventListFragmentViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;


public class ExploreFragment extends Fragment {
    private static final String TAG = "ExploreFragment";

    private FragmentExploreBinding binding;

    private EventListFragmentViewModel mListViewModel;
    private FirebaseDatabase mDatabase;

    private String[] searchKeys = {"nameLowercase", "locationLowercase", "sponsorLowercase"};

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

        mListViewModel.clearEventData();

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
}

