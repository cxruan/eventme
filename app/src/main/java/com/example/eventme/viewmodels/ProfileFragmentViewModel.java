package com.example.eventme.viewmodels;

import android.util.Log;
import android.view.View;

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

    private MutableLiveData<User> userData;
    private MutableLiveData<List<Event>> registeredEventsData;

    public ProfileFragmentViewModel() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
    }

    public LiveData<User> getUserData() {
        if (userData == null) {
            userData = new MutableLiveData<User>();
            loadAllData();
        }
        return userData;
    }

    public LiveData<List<Event>> getRegisteredEventsData() {
        if (registeredEventsData == null) {
            registeredEventsData = new MutableLiveData<List<Event>>();
            loadAllData();
        }
        return registeredEventsData;
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


    private void loadAllData() {
        if (mAuth.getCurrentUser() != null) {
            mDatabase.getReference().child("users").child(mAuth.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult().getValue(User.class);
                    userData.setValue(user);

                    // Get registered events
                    List<Event> events = new ArrayList<>();
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
                } else {
                    Log.e(TAG, "Error getting user data", task.getException());
                }
            });
        }
    }
}
