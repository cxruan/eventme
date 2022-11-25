package com.example.eventme.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventme.models.Event;
import com.example.eventme.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

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
            mDatabase.getReference().child("users").child(mAuth.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult().getValue(User.class);
                    userData.setValue(user);
                } else {
                    Log.e(TAG, "Error getting user data", task.getException());
                }
            });
        }
    }

    public void loadAllData() {
        if (mAuth.getCurrentUser() != null) {
            mDatabase.getReference().child("users").child(mAuth.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult().getValue(User.class);
                    userData.setValue(user);

                    // Get registered events
                    List<Event> events = new ArrayList<>();

                    // Handle empty events case
                    if (user.getRegisteredEvents().isEmpty())
                        registeredEventsData.setValue(events);

                    for (String id : user.getRegisteredEvents().keySet()) {
                        mDatabase.getReference().child("events").child(id).get().addOnCompleteListener(eventTask -> {
                            if (eventTask.isSuccessful()) {
                                Event event = eventTask.getResult().getValue(Event.class);
                                events.add(event);
                                // Finished loading all events
                                if (events.size() == user.getRegisteredEvents().size())
                                    registeredEventsData.setValue(events);
                            } else {
                                Log.e(TAG, "Error getting event", eventTask.getException());
                            }
                        });
                    }

                    List<Event> eventsTwo = new ArrayList<>();

                    if (user.getSavedEvents().isEmpty())
                        savedEventsData.setValue(eventsTwo);

                    for (String id : user.getSavedEvents().keySet()) {
                        mDatabase.getReference().child("events").child(id).get().addOnCompleteListener(eventTask -> {
                            if (eventTask.isSuccessful()) {
                                Event event = eventTask.getResult().getValue(Event.class);
                                eventsTwo.add(event);
                                // Finished loading all events
                                if (eventsTwo.size() == user.getSavedEvents().size())
                                    savedEventsData.setValue(eventsTwo);
                            } else {
                                Log.e(TAG, "Error getting event", eventTask.getException());
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Error getting user data", task.getException());
                }
            });
        }
    }
}
