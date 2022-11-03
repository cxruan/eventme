package com.example.eventme.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventme.models.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MapFragmentViewModel extends ViewModel {
    private static final String TAG = "MapFragmentViewModel";

    private FirebaseDatabase mDatabase;

    private MutableLiveData<List<Event>> eventsData;

    public MapFragmentViewModel() {
        mDatabase = FirebaseDatabase.getInstance();
    }

    public LiveData<List<Event>> getEventsData() {
        if (eventsData == null) {
            eventsData = new MutableLiveData<>();
            loadAllData();
        }
        return eventsData;
    }

    private void loadAllData() {
        mDatabase.getReference().child("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Event> events = new ArrayList<>();
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    Event event = ds.getValue(Event.class);
                    events.add(event);
                    // Finished loading all events
                    if (events.size() == task.getResult().getChildrenCount())
                        eventsData.setValue(events);
                }
            } else {
                Log.w(TAG, "loadAllData:onCancelled", task.getException());
            }
        });
    }
}
