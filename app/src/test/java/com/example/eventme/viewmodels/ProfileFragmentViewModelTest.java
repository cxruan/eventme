package com.example.eventme.viewmodels;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.eventme.models.Event;
import com.example.eventme.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ProfileFragmentViewModelTest {
    List<Event> sampleEvents;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    ProfileFragmentViewModel viewModel;
    @Mock
    Observer<User> userObserver;
    @Mock
    Observer<List<Event>> eventObserver;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    FirebaseAuth auth;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    FirebaseDatabase database;
    @Captor
    private ArgumentCaptor<User> userCaptor;
    @Captor
    private ArgumentCaptor<List<Event>> eventsCaptor;

    @Before
    public void setUp() {
        sampleEvents = new ArrayList<>();
        sampleEvents.add(new Event("a"));
        sampleEvents.add(new Event("b"));
        sampleEvents.add(new Event("c"));

        viewModel = new ProfileFragmentViewModel(auth, database);
        viewModel.getUserData().observeForever(userObserver);
        viewModel.getRegisteredEventsData().observeForever(eventObserver);
    }

    @Test
    public void getUser_isNotNull() {
        verify(userObserver, atLeastOnce()).onChanged(userCaptor.capture());
        User capturedArgument = userCaptor.getValue();

        // Test oracle
        assertNotNull(capturedArgument);
    }

    @Test
    public void getRegisteredEvents_isNotNull() {
        verify(eventObserver, atLeastOnce()).onChanged(eventsCaptor.capture());
        List<Event> capturedArgument = eventsCaptor.getValue();

        // Test oracle
        assertNotNull(capturedArgument);
    }
}