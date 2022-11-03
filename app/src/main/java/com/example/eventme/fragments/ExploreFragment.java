package com.example.eventme.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventme.adapters.EventBoxAdapter;
import com.example.eventme.databinding.FragmentExploreBinding;
import com.example.eventme.R;
import com.example.eventme.databinding.FragmentProfileBinding;
import com.example.eventme.models.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;


public class ExploreFragment extends Fragment {
    private static final String TAG = "ExploreFragment";

    private FirebaseAuth mAuth;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private EventBoxAdapter mEventBoxAdapter;
    private DatabaseReference mEventReference;
    private FragmentExploreBinding binding;

    private ArrayList<Event> filteredEvents = new ArrayList<Event>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mEventReference = FirebaseDatabase.getInstance().getReference().child("events");

        mEventBoxAdapter = new EventBoxAdapter();

        // Set up Layout Manager
        mManager = new LinearLayoutManager(getActivity());
        ArrayAdapter<CharSequence> adapterSearch = ArrayAdapter.createFromResource(getContext(),
                R.array.searchBy_array, android.R.layout.simple_spinner_item);
        adapterSearch.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.searchBySpinner.setAdapter(adapterSearch);
        ArrayAdapter<CharSequence> adapterSort = ArrayAdapter.createFromResource(getContext(),
                R.array.sortBy_array, android.R.layout.simple_spinner_item);
        adapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sortBySpinner.setAdapter(adapterSort);
        mRecycler = binding.searchResults;
        mRecycler.setLayoutManager(mManager);
        mRecycler.setAdapter(mEventBoxAdapter);
        binding.searchBar.getText().clear();
        binding.resultsNumber.setVisibility(View.INVISIBLE);
        binding.noResults.setVisibility(View.INVISIBLE);
        binding.SearchByTypeGrid.setVisibility(View.VISIBLE);
        int childCount = binding.SearchByTypeGrid.getChildCount();
        for (int i= 0; i < childCount; i++){
            TextView container = (TextView) binding.SearchByTypeGrid.getChildAt(i);
            container.setOnClickListener(this::onClickSearchByType);
        }
        binding.searchBtn.setOnClickListener(this::onClickSearch);
        binding.SearchByTypeTitle.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                binding.searchResults.setVisibility(View.INVISIBLE);
                binding.SearchByTypeGrid.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onClickSearch(View view) {
        filteredEvents.clear();
        String searchTerm = binding.searchBar.getText().toString();
        Log.d(TAG, searchTerm);
        mEventReference.orderByChild(binding.searchBySpinner.getSelectedItem().toString().toLowerCase()).startAt(searchTerm).endAt(searchTerm + "\uf8ff").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        binding.SearchByTypeGrid.setVisibility(View.INVISIBLE);
                        binding.resultsNumber.setVisibility(View.VISIBLE);
                        binding.noResults.setVisibility(View.INVISIBLE);
                        mEventBoxAdapter.clearAllItem();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            Event event = ds.getValue(Event.class);
                            Log.d(TAG, event.getName());
                            filteredEvents.add(event);
                        }
                        if(filteredEvents.size() > 0) {
                            for (Event e : filteredEvents) {
                                mEventBoxAdapter.addItem(e);
                            }
                            binding.resultsNumber.setText(String.valueOf(filteredEvents.size()) + " results");
                        }
                        else {
                            binding.noResults.setVisibility(View.VISIBLE);
                            binding.resultsNumber.setText(String.valueOf(filteredEvents.size()) + " results");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }

    public void onClickSearchByType(View view) {
        filteredEvents.clear();
        TextView v = (TextView) view;
        mEventReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        binding.SearchByTypeGrid.setVisibility(View.INVISIBLE);
                        binding.resultsNumber.setVisibility(View.VISIBLE);
                        binding.noResults.setVisibility(View.INVISIBLE);
                        mEventBoxAdapter.clearAllItem();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            Event event = ds.getValue(Event.class);
                            Map<String, Boolean> allTypes = event.getTypes();
                            if(allTypes.containsKey(v.getText().toString().toLowerCase())){
                                filteredEvents.add(event);
                            }
                        }
                        if(filteredEvents.size() > 0) {
                            for (Event e : filteredEvents) {
                                mEventBoxAdapter.addItem(e);
                            }
                            binding.resultsNumber.setText(String.valueOf(filteredEvents.size()) + " results");
                        }
                        else {
                            binding.noResults.setVisibility(View.VISIBLE);
                            binding.resultsNumber.setText(String.valueOf(filteredEvents.size()) + " results");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }


}

