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

import com.example.eventme.fragments.SignUpFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignUpFragmentInstrumentedTest {
    private static final String TAG = "SignUpFragmentInstrumentedTest";

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

        FragmentScenario<SignUpFragment> signUpScenario = FragmentScenario.launchInContainer(SignUpFragment.class);
        signUpScenario.onFragment(fragment -> {
            // Set the graph on the TestNavHostController
            navController.setGraph(R.navigation.signin_nav_graph);
            navController.setCurrentDestination(R.id.signUpFragment);

            // Make the NavController available via the findNavController() APIs
            Navigation.setViewNavController(fragment.requireView(), navController);
        });
    }

    @Test
    public void signUpFragment_isDisplayed() {
        onView(withId(R.id.signUp)).check(matches(isDisplayed()));
    }

    @Test
    public void singUp_isSuccess() {
        onView(withId(R.id.firstName)).perform(typeText("Michael"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.lastName)).perform(typeText("Jordan"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.email)).perform(typeText("xyz@abc.com"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.birthday)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(TestUtils.setDate(1983, 2, 4));
        onView(withText("OK")).perform(click());
        onView(withId(R.id.password)).perform(typeText("123456"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.reEnterPassword)).perform(typeText("123456"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.signUp)).perform(click());

        // Sign-up will automatically sign in the user
        assertNotNull(mAuth.getCurrentUser());

        // Delete newly created user after the test
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete();
            mDatabase.getReference().child("users").child(user.getUid()).removeValue();
            Log.d(TAG, "tearDown: user deleted");
        }
    }

    @Test
    public void singUpInvalid_isErrorDisplayed() {
        onView(withId(R.id.signUp)).perform(click());

        onView(withId(R.id.firstName)).check(matches(hasErrorText("Required")));
        onView(withId(R.id.lastName)).check(matches(hasErrorText("Required")));
        onView(withId(R.id.email)).check(matches(hasErrorText("Required")));
        onView(withId(R.id.birthday)).check(matches(hasErrorText("Required")));
        onView(withId(R.id.password)).check(matches(hasErrorText("Required")));
    }

    @Test
    public void singUpDuplicatedEmail_isError() {
        onView(withId(R.id.firstName)).perform(typeText("Michael"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.lastName)).perform(typeText("Jordan"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.email)).perform(typeText("abc@xyz.com"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.birthday)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(TestUtils.setDate(1983, 2, 4));
        onView(withText("OK")).perform(click());
        onView(withId(R.id.password)).perform(typeText("123456"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.reEnterPassword)).perform(typeText("123456"), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.signUp)).perform(click());

        assertNull(mAuth.getCurrentUser());
    }
}
