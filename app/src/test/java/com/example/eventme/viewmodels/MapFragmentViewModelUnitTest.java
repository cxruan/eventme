package com.example.eventme.viewmodels;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.eventme.models.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class MapFragmentViewModelUnitTest {
    HashMap<String, Event> sampleEvents;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    MapFragmentViewModel viewModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    FirebaseDatabase database;
    @Mock
    Observer<HashMap<String, Event>> observer;
    @Captor
    private ArgumentCaptor<HashMap<String, Event>> captor;

    @Before
    public void setUp() {
        sampleEvents = new HashMap<>();
        sampleEvents.put("a", new Event("a"));
        sampleEvents.put("b", new Event("b"));
        sampleEvents.put("c", new Event("c"));

        viewModel = new MapFragmentViewModel(database);
        viewModel.getEventsData().observeForever(observer);
    }

    @Test
    public void getEvents_isNotNull() {
        verify(observer, atLeastOnce()).onChanged(captor.capture());
        HashMap<String, Event> capturedArgument = captor.getValue();

        // Test oracle
        assertNotNull(capturedArgument);
    }

    @Test
    public void loadEvents_isSuccessful() {
        // mock Firebase database to directly feed back data
        when(database.getReference().child("events").get()).then(invocation -> {
            viewModel.setEventsData(sampleEvents);
            return mock(Task.class);
        });

        viewModel.loadAllData();

        verify(observer, atLeastOnce()).onChanged(captor.capture());
        HashMap<String, Event> capturedArgument = captor.getValue();

        // Test oracle
        assertEquals(sampleEvents, capturedArgument);
    }

    @Test
    public void getEventById_isSuccessful() {
        // mock Firebase database to directly feed back data
        when(database.getReference().child("events").get()).then(invocation -> {
            viewModel.setEventsData(sampleEvents);
            return mock(Task.class);
        });

        viewModel.loadAllData();

        // Test oracle
        assertNotNull(viewModel.getEventById("c"));
    }
}