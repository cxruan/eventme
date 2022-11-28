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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileFragmentViewModel extends ViewModel {
    private static final String TAG = "ProfileFragViewModel";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private MutableLiveData<User> userData = new MutableLiveData<>(new User());
    private MutableLiveData<List<Event>> registeredEventsData = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Event>> savedEventsData = new MutableLiveData<>(new ArrayList<>());

    public ProfileFragmentViewModel(FirebaseAuth auth, FirebaseDatabase database) {
        mAuth = auth;
        mDatabase = database;
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

    public void updateUserData() {
        if (mAuth.getCurrentUser() != null) {
            mDatabase.getReference().child("users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
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

    public void loadAllData() {
        if (mAuth.getCurrentUser() != null) {
            mDatabase.getReference().child("users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    userData.setValue(user);

                    // Get registered events
                    List<Event> registeredEvents = new ArrayList<>();
                    // Handle empty events case
                    if (user.getRegisteredEvents().isEmpty())
                        registeredEventsData.setValue(registeredEvents);

                    AtomicInteger registeredCount = new AtomicInteger(0);

                    for (String id : user.getRegisteredEvents().keySet()) {
                        mDatabase.getReference().child("events").child(id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Event event = snapshot.getValue(Event.class);
                                registeredEvents.add(event);

                                // Finished loading all events
                                if (registeredCount.addAndGet(1) == user.getRegisteredEvents().size())
                                    registeredEventsData.setValue(registeredEvents);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error getting event", error.toException());
                            }
                        });
                    }

                    // Get saved events
                    List<Event> savedEvents = new ArrayList<>();
                    // Handle empty events case
                    if (user.getRegisteredEvents().isEmpty())
                        registeredEventsData.setValue(savedEvents);

                    AtomicInteger savedCount = new AtomicInteger(0);

                    for (String id : user.getSavedEvents().keySet()) {
                        mDatabase.getReference().child("events").child(id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Event event = snapshot.getValue(Event.class);
                                savedEvents.add(event);

                                // Finished loading all events
                                if (savedCount.addAndGet(1) == user.getSavedEvents().size())
                                    savedEventsData.setValue(savedEvents);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error getting event", error.toException());
                            }
                        });
                    }

                    
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error getting user", error.toException());
                }
            });
        }
    }
}
