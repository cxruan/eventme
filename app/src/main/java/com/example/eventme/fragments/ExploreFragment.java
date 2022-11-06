package com.example.eventme.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventme.EventRegistrationActivity;
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
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
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
    private String startDate;
    private String endDate;


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
        mEventBoxAdapter.setOnItemClickListener((position, v) -> {
            // Pass eventId to Registration activity when clicking event box
            Event event = mEventBoxAdapter.getItemByPos(position);
            Intent intent = new Intent(requireActivity(), EventRegistrationActivity.class);
            intent.putExtra("com.example.eventme.EventRegistration.eventId", event.getEventId());
            startActivity(intent);
        });

        // Set up Layout Manager
        mManager = new LinearLayoutManager(getActivity());
        mRecycler = binding.searchResults;
        mRecycler.setLayoutManager(mManager);
        mRecycler.setAdapter(mEventBoxAdapter);
        ArrayAdapter<CharSequence> adapterSearch = ArrayAdapter.createFromResource(getContext(),
                R.array.searchBy_array, android.R.layout.simple_spinner_item);
        adapterSearch.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.searchBySpinner.setAdapter(adapterSearch);
        ArrayAdapter<CharSequence> adapterSort = ArrayAdapter.createFromResource(getContext(),
                R.array.sortBy_array, android.R.layout.simple_spinner_item);
        adapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sortBySpinner.setAdapter(adapterSort);
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
                binding.SearchByTypeGrid.setVisibility(View.VISIBLE);
                mEventBoxAdapter.clearAllItem();
                binding.resultsNumber.setVisibility(View.INVISIBLE);
                binding.noResults.setVisibility(View.INVISIBLE);
            }
        });

        binding.startDate.setPaintFlags(binding.startDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.endDate.setPaintFlags(binding.endDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day);
        binding.startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        i1 += 1;
                        startDate = i + "/";
                        if(i1 < 10) startDate += "0";
                        startDate += i1 + "/";
                        if(i2 < 10) startDate += "0";
                        startDate += i2;
                        binding.startDate.setHint(startDate);
                    }
                }, year, month, day);
                long minDate = calendar.getTimeInMillis();
                dialog.getDatePicker().setMinDate(minDate);
                dialog.show();
            }
        });

        startDate = "";
        endDate = "";
        binding.endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker daePicker, int i, int i1, int i2) {
                        i1 += 1;
                        endDate = i + "/";
                        if(i1 < 10) endDate += "0";
                        endDate += i1 + "/";
                        if(i2 < 10) endDate += "0";
                        endDate += i2;
                        binding.endDate.setHint(endDate);
                    }
                }, year, month, day);
                long minDate = calendar.getTimeInMillis();
                dialog.getDatePicker().setMinDate(minDate);
                dialog.show();
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
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Event event = ds.getValue(Event.class);
                            Log.d(TAG, event.getName());
                            if(startDate.compareTo("") != 0 && endDate.compareTo("") != 0){
                                if(event.getDate().compareTo(startDate) >= 0 && event.getDate().compareTo(endDate) <= 0){
                                    filteredEvents.add(event);
                                }
                            } else if (startDate.compareTo("") != 0 && endDate.compareTo("") == 0){
                                if(event.getDate().compareTo(startDate) >= 0){
                                    filteredEvents.add(event);
                                }
                            } else if (startDate.compareTo("") == 0 && endDate.compareTo("") != 0){
                                if(event.getDate().compareTo(endDate) <= 0){
                                    filteredEvents.add(event);
                                }
                            } else {
                                filteredEvents.add(event);
                            }
                        }
                        sortEvents();
                        if (filteredEvents.size() > 0) {
                            for (Event e : filteredEvents) {
                                mEventBoxAdapter.addItem(e);
                            }
                            binding.resultsNumber.setText(String.valueOf(filteredEvents.size()) + " results");
                        } else {
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
        Log.e(TAG, "Type clicked");
        mEventReference.addListenerForSingleValueEvent(
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
                                if(startDate.compareTo("") != 0 && endDate.compareTo("") != 0){
                                    if(event.getDate().compareTo(startDate) >= 0 && event.getDate().compareTo(endDate) <= 0){
                                        filteredEvents.add(event);
                                    }
                                } else if (startDate.compareTo("") != 0 && endDate.compareTo("") == 0){
                                    if(event.getDate().compareTo(startDate) >= 0){
                                        filteredEvents.add(event);
                                    }
                                } else if (startDate.compareTo("") == 0 && endDate.compareTo("") != 0){
                                    if(event.getDate().compareTo(endDate) <= 0){
                                        filteredEvents.add(event);
                                    }
                                } else {
                                    filteredEvents.add(event);
                                }
                            }
                        }
                        sortEvents();
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

    private void sortEvents(){
        if(binding.sortBySpinner.getSelectedItem().toString().equals("Cost")){
            filteredEvents.sort(new Comparator<Event>() {
                @Override
                public int compare(Event lhs, Event rhs) {

                    return lhs.getCost() < rhs.getCost() ? -1 : (lhs.getCost() > rhs.getCost()) ? 1 : 0;
                }
            });
        } else if(binding.sortBySpinner.getSelectedItem().toString().equals("Date")){
            filteredEvents.sort(new Comparator<Event>() {
                @Override
                public int compare(Event lhs, Event rhs) {
                    // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    if(lhs.getDate().compareTo(rhs.getDate()) >= 1){
                        return 1;
                    } else if(lhs.getDate().compareTo(rhs.getDate()) <= -1){
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        }
    }


}

