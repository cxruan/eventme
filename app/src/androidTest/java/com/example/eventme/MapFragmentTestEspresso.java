package com.example.eventme;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.util.Log;
import android.widget.DatePicker;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static org.junit.Assert.*;

import com.example.eventme.fragments.MapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.runner.RunWith;
import androidx.test.filters.LargeTest;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapFragmentTestEspresso {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private TestNavHostController navController;

    @Before
    public void setUp() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_AUTH_PORT);
        mAuth.signOut();

        mDatabase = FirebaseDatabase.getInstance();
        mDatabase.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_DATABASE_PORT);

        // Create a TestNavHostController
        navController = new TestNavHostController(ApplicationProvider.getApplicationContext());

        FragmentScenario<MapFragment> signUpScenario = FragmentScenario.launchInContainer(MapFragment.class);
        signUpScenario.onFragment(fragment -> {
            // Set the graph on the TestNavHostController
            navController.setGraph(R.navigation.main_nav_graph);
            navController.setCurrentDestination(R.id.mapFragment);

            // Make the NavController available via the findNavController() APIs
            Navigation.setViewNavController(fragment.requireView(), navController);
        });
    }

    @Test
    public void signUpFragment_isDisplayed() {
        onView(withId(R.id.text)).check(matches(isDisplayed()));
    }
}
