package com.example.eventme.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventme.models.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MapFragmentViewModel extends ViewModel {
    private static final String TAG = "MapFragmentViewModel";

    private FirebaseDatabase mDatabase;

    private MutableLiveData<HashMap<String, Event>> eventsMapData;

    public MapFragmentViewModel() {
        mDatabase = FirebaseDatabase.getInstance();
    }

    public LiveData<HashMap<String, Event>> getEventsData() {
        if (eventsMapData == null) {
            eventsMapData = new MutableLiveData<>();
            loadAllData();
        }
        return eventsMapData;
    }

    public Event getEventById(String eventId) {
        return eventsMapData.getValue().get(eventId);
    }

    private void loadAllData() {
        mDatabase.getReference().child("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HashMap<String, Event> events = new HashMap<>();
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    Event event = ds.getValue(Event.class);
                    events.put(event.getEventId(), event);

                    // Finished loading all events
                    if (events.size() == task.getResult().getChildrenCount())
                        eventsMapData.setValue(events);
                }
            } else {
                Log.w(TAG, "loadAllData:onCancelled", task.getException());
            }
        });
    }
}
