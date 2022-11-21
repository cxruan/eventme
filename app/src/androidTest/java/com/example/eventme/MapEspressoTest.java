package com.example.eventme;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapEspressoTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        TestUtils.loginGuest();
        onView(withId(R.id.mapFragment)).perform(click()); // Go to map fragment
        onView(withText("Events near me")).check(matches(isDisplayed()));
    }

    /**
     * Performs a test on if Google Map fragment is displayed
     */
    @Test
    public void googleMap_isDisplayed() {
        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }

    /**
     * Performs a test on if the event list view can be swiped up and down
     */
    @Test
    public void eventList_swipeIsSuccess() {
        onView(withId(R.id.indicator_line)).perform(TestUtils.swipeToTop());
        onView(withId(R.id.eventList)).check(matches(isDisplayed()));

        onView(withId(R.id.indicator_line)).perform(TestUtils.swipeToBottom());
        onView(withId(R.id.eventList)).check(matches(not(isCompletelyDisplayed())));
    }

    /**
     * Performs a test on if the event list is sorted by distance by default
     */
    @Test
    public void eventList_sortedByLocation() {
        onView(withId(R.id.indicator_line)).perform(TestUtils.swipeToTop());
        onView(withId(R.id.eventList)).check(matches(isDisplayed()));

        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(20, "", "location"));
    }

}
