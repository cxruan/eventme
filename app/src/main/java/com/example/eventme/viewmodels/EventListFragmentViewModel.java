package com.example.eventme.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventme.models.Event;

import java.util.ArrayList;
import java.util.List;

public class EventListFragmentViewModel extends ViewModel {
    private static final String TAG = "EventListFragmentViewModel";

    private MutableLiveData<List<Event>> eventsData = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Event>> getEventsData() {
        return eventsData;
    }

    public void setEventsData(List<Event> events) {
        eventsData.setValue(events);
    }

    public void addEventsData(Event event) {
        List<Event> copy = eventsData.getValue();
        if (!copy.contains(event)) {
            copy.add(event);
            eventsData.setValue(copy);
        }
    }

    public void clearEventData() {
        eventsData.setValue(new ArrayList<>());
    }
}
