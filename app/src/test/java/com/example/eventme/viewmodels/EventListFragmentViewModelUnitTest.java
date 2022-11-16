package com.example.eventme.viewmodels;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.eventme.models.Event;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class EventListFragmentViewModelUnitTest {
    List<Event> sampleEvents;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    EventListFragmentViewModel viewModel;
    @Mock
    Observer<List<Event>> observer;
    @Captor
    private ArgumentCaptor<List<Event>> captor;

    @Before
    public void setUp() {
        sampleEvents = new ArrayList<>();
        sampleEvents.add(new Event("a"));
        sampleEvents.add(new Event("b"));
        sampleEvents.add(new Event("c"));

        viewModel = new EventListFragmentViewModel();
        viewModel.getEventsData().observeForever(observer);
    }

    @Test
    public void getEvents_isNotNull() {
        verify(observer, atLeastOnce()).onChanged(captor.capture());
        List<Event> capturedArgument = captor.getValue();

        // Test oracle
        assertNotNull(capturedArgument);
    }

    @Test
    public void setEvents_isSuccess() {
        // Actions
        viewModel.setEventsData(sampleEvents);

        verify(observer, atLeastOnce()).onChanged(captor.capture());
        List<Event> capturedArgument = captor.getValue();

        // Test oracle
        assertEquals(capturedArgument, sampleEvents);
    }

    @Test
    public void addEvent_isSuccess() {
        // Actions
        Event eventD = new Event("d");
        viewModel.addEventsData(eventD);

        verify(observer, atLeastOnce()).onChanged(captor.capture());
        List<Event> capturedArgument = captor.getValue();

        // Test oracle
        assertTrue(capturedArgument.contains(eventD));
    }

    @Test
    public void clearEvents_isSuccess() {
        // Actions
        viewModel.setEventsData(sampleEvents);
        viewModel.clearEventData();

        verify(observer, atLeastOnce()).onChanged(captor.capture());
        List<Event> capturedArgument = captor.getValue();

        // Test oracle
        assertTrue(capturedArgument.isEmpty());
    }
}