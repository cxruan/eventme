package com.example.eventme;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExploreEspressoTest {
    private View decorView;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private Activity mMainActivity;
    private RecyclerView mRecyclerView;
    private int itemCount = 0;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_AUTH_PORT);
        mAuth.signOut();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabase.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_DATABASE_PORT);

        onView(withId(R.id.email)).perform(typeText("abc@xyz.com"));
        onView(withId(R.id.password)).perform(typeText("123456"));
        onView(withId(R.id.signIn)).perform(click());
    }

    @After
    public void tearDown() {
        mAuth.signOut();
    }

    @Test
    public void exploreFragment_isDisplayed() {
        onView(withId(R.id.ExploreTitle)).check(matches(isDisplayed()));
    }

    @Test
    public void explore_noResults() {
        onView(withId(R.id.searchBar)).perform(typeText("USCVSUCLA"));
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.emptyResultText)).check(matches(isDisplayed()));
    }

    @Test
    public void explore_searchAllEvents() {
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(20));
    }

    @Test
    public void explore_basicSearch() {
        onView(withId(R.id.searchBar)).perform(typeText("USC"));
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(9, "USC"));

    }

    @Test
    public void explore_sortByCost() {
        onView(withId(R.id.searchBar)).perform(typeText("USC"));
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.sortBySpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Cost"))).perform(click());
        onView(withId(R.id.sortBySpinner)).check(matches(withSpinnerText("Cost")));
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(9, "USC", "Cost"));
    }

    @Test
    public void explore_sortAlphabetically() {
        onView(withId(R.id.searchBar)).perform(typeText("USC"));
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.sortBySpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Alphabetical"))).perform(click());
        onView(withId(R.id.sortBySpinner)).check(matches(withSpinnerText("Alphabetical")));
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(9, "USC", "Alphabetical"));
    }

    @Test
    public void explore_sortByDate() {
        onView(withId(R.id.searchBar)).perform(typeText("USC"));
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.sortBySpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Date"))).perform(click());
        onView(withId(R.id.sortBySpinner)).check(matches(withSpinnerText("Date")));
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(9, "USC", "Date"));
    }

    @Test
    public void explore_sortByLocation() {
        onView(withId(R.id.searchBar)).perform(typeText("USC"));
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.sortBySpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Location"))).perform(click());
        onView(withId(R.id.sortBySpinner)).check(matches(withSpinnerText("Location")));
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(9, "USC", "Location"));
    }

    @Test
    public void explore_typeFilterMusic() {
        onView(withId(R.id.searchBar)).perform(typeText("USC"));
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.typeFilter)).perform(click());
        onView(withText("Music")).perform(click());
        onView(withText("OK")).perform(click());
        String[] types = {"Music"};
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(1, "USC", "Cost", types));
    }

    @Test
    public void explore_typeFilterMultiple() {
        onView(withId(R.id.searchBar)).perform(typeText("USC"));
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.typeFilter)).perform(click());
        onView(withText("Food & Drinks")).perform(click());
        onView(withText("Outdoors")).perform(click());
        onView(withText("OK")).perform(click());
        String[] types = {"Food & Drinks", "Outdoors"};
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(1, "USC", "Cost", types));
    }

    @Test
    public void explore_typeFilterNoResults() {
        onView(withId(R.id.searchBar)).perform(typeText("Music"));
        onView(withId(R.id.searchBtn)).perform(click());
        onView(withId(R.id.typeFilter)).perform(click());
        onView(withText("Outdoors")).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.emptyResultText)).check(matches(isDisplayed()));
    }



}
