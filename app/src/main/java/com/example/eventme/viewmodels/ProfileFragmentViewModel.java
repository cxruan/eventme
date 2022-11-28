package com.example.eventme.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventme.models.Event;
import com.example.eventme.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileFragmentViewModel extends ViewModel {
    private static final String TAG = "ProfileFragViewModel";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private MutableLiveData<User> userData = new MutableLiveData<>(new User());
    private MutableLiveData<List<Event>> registeredEventsData = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Event>> savedEventsData = new MutableLiveData<>(new ArrayList<>());
    private ValueEventListener loadUserListener;
    private ValueEventListener loadRegisteredListener;
    private ValueEventListener loadSavedListener;

    public ProfileFragmentViewModel(FirebaseAuth auth, FirebaseDatabase database) {
        mAuth = auth;
        mDatabase = database;

        addLoadUserInfoListener();
        addLoadRegisteredEventsListener();
        addLoadSavedEventsListener();
    }

    public LiveData<User> getUserData() {
        return userData;
    }

    public LiveData<List<Event>> getRegisteredEventsData() {
        return registeredEventsData;
    }

    public LiveData<List<Event>> getSavedEventsData() {
        return savedEventsData;
    }

    public void addLoadUserInfoListener() {
        if (mAuth.getCurrentUser() != null && loadUserListener == null) {
            loadUserListener = mDatabase.getReference().child("users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    userData.setValue(user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error getting user data", error.toException());
                }
            });
        }
    }

    public void removeLoadUserInfoListener() {
        if (loadUserListener != null) {
            mDatabase.getReference().child("users").child(mAuth.getUid()).removeEventListener(loadUserListener);
            loadUserListener = null;
        }
    }

    public void addLoadRegisteredEventsListener() {
        if (mAuth.getCurrentUser() != null && loadRegisteredListener == null) {
            loadRegisteredListener = mDatabase.getReference().child("users").child(mAuth.getUid()).child("registeredEvents").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    GenericTypeIndicator<Map<String, Boolean>> t = new GenericTypeIndicator<Map<String, Boolean>>() {
                    };
                    Map<String, Boolean> userRegisteredEvents = snapshot.getValue(t);

                    // Get registered events
                    List<Event> registeredEvents = new ArrayList<>();
                    // Handle empty events case
                    if (userRegisteredEvents == null || userRegisteredEvents.isEmpty()) {
                        registeredEventsData.setValue(registeredEvents);
                        return;
                    }

                    AtomicInteger registeredCount = new AtomicInteger(0);

                    for (String id : userRegisteredEvents.keySet()) {
                        mDatabase.getReference().child("events").child(id).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Event event = task.getResult().getValue(Event.class);
                                registeredEvents.add(event);

                                // Finished loading all registered events
                                if (registeredCount.addAndGet(1) == userRegisteredEvents.size())
                                    registeredEventsData.setValue(registeredEvents);
                            } else {
                                Log.e(TAG, "Error getting event", task.getException());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error getting registered events", error.toException());

                }
            });
        }
    }

    public void removeLoadRegisteredEventsListener() {
        if (loadRegisteredListener != null) {
            mDatabase.getReference().child("users").child(mAuth.getUid()).child("registeredEvents").removeEventListener(loadRegisteredListener);
            loadRegisteredListener = null;
        }
    }

    public void addLoadSavedEventsListener() {
        if (mAuth.getCurrentUser() != null && loadSavedListener == null) {
            loadSavedListener = mDatabase.getReference().child("users").child(mAuth.getUid()).child("savedEvents").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    GenericTypeIndicator<Map<String, Boolean>> t = new GenericTypeIndicator<Map<String, Boolean>>() {
                    };
                    Map<String, Boolean> userRegisteredEvents = snapshot.getValue(t);

                    // Get saved events
                    List<Event> savedEvents = new ArrayList<>();
                    // Handle empty events case
                    if (userRegisteredEvents == null || userRegisteredEvents.isEmpty()) {
                        savedEventsData.setValue(savedEvents);
                        return;
                    }

                    AtomicInteger savedCount = new AtomicInteger(0);

                    for (String id : userRegisteredEvents.keySet()) {
                        mDatabase.getReference().child("events").child(id).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Event event = task.getResult().getValue(Event.class);
                                savedEvents.add(event);

                                // Finished loading all saved events
                                if (savedCount.addAndGet(1) == userRegisteredEvents.size() && !savedEventsData.getValue().equals(savedEvents))
                                    savedEventsData.setValue(savedEvents);
                            } else {
                                Log.e(TAG, "Error getting event", task.getException());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error getting saved events", error.toException());

                }
            });
        }
    }

    public void removeLoadSavedEventsListener() {
        if (loadSavedListener != null) {
            mDatabase.getReference().child("users").child(mAuth.getUid()).child("savedEvents").removeEventListener(loadSavedListener);
            loadSavedListener = null;
        }
    }
}
