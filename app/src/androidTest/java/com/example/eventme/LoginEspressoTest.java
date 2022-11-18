package com.example.eventme;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;

import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginEspressoTest {
    private View decorView;
    private FirebaseAuth mAuth;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_AUTH_PORT);
        mAuth.signOut();

        activityScenarioRule.getScenario().onActivity(activity -> {
            decorView = activity.getWindow().getDecorView();
        });
    }

    @After
    public void tearDown() {
        mAuth.signOut();
    }

    @Test
    public void loginFragment_isDisplayed() {
        onView(withId(R.id.container)).check(matches(isDisplayed()));
    }

    @Test
    public void loginInvalid_isErrorDisplayed() {
        onView(withId(R.id.signIn)).perform(click());

        onView(withId(R.id.email)).check(matches(hasErrorText("Required")));
        onView(withId(R.id.password)).check(matches(hasErrorText("Required")));
    }

    @Test
    public void loginWrongCredentials_isError() {
        onView(withId(R.id.email)).perform(typeText("abc@xyz.com"));
        onView(withId(R.id.password)).perform(typeText("12345"));

        onView(withId(R.id.signIn)).perform(click());

        onView(withText("Failed to log in"))
                .inRoot(withDecorView(not(decorView)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void login_isSuccess() {
        onView(withId(R.id.email)).perform(typeText("abc@xyz.com"));
        onView(withId(R.id.password)).perform(typeText("123456"));

        onView(withId(R.id.signIn)).perform(click());

        onView(withText("Successfully logged in"))
                .inRoot(withDecorView(not(decorView)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.ExploreTitle)).check(matches(isDisplayed()));
    }

}
