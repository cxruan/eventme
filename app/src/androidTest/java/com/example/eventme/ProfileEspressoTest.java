package com.example.eventme;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.contrib.RecyclerViewActions;
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
public class ProfileEspressoTest {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_AUTH_PORT);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabase.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_DATABASE_PORT);
    }

    @After
    public void tearDown() {
        mAuth.signOut();
    }

    /**
     * Performs a test on logged-in user info display
     *
     * <ol>
     *   <li>Go to profile page
     *   <li>Check if the profile picture is displayed
     *   <li>Check if the name is displayed
     *   <li>Check if the email is displayed
     *   <li>Check if the birthday is displayed
     *   <li>Check if the sign-out button is displayed
     * </ol>
     */
    @Test
    public void loggedInUser_correctInfoDisplayed() {
        TestUtils.loginTestUser();
        onView(withId(R.id.profileFragment)).perform(click()); // Go to profile fragment

        onView(withId(R.id.profilePic)).check(matches(isDisplayed()));
        onView(withId(R.id.name)).check(matches(isDisplayed()));
        onView(withId(R.id.email)).check(matches(isDisplayed()));
        onView(withId(R.id.birthday)).check(matches(isDisplayed()));
        onView(withId(R.id.signOut)).check(matches(isDisplayed()));
    }

    /**
     * Performs a test on logged-in user registration and un-registration of a single event
     *
     * <ol>
     *   <li>Go to profile page and check if the user has no registered events
     *   <li>Go the explore page and click on the search button. 20 predetermined events should be shown
     *   <li>Register the first event
     *   <li>Go to profile page and check if the user has 1 registered events
     *   <li>Unregister the event and check if is unregistered
     * </ol>
     */
    @Test
    public void loggedInUser_registerSingleEventSuccess() {
        TestUtils.loginTestUser();
        onView(withId(R.id.profileFragment)).perform(click()); // Go to profile fragment

        onView(withId(R.id.emptyResultText)).check(matches(isDisplayed())); // Default user should have no registered events

        onView(withId(R.id.exploreFragment)).perform(click()); // Go to explore fragment
        onView(withId(R.id.ExploreTitle)).check(matches(isDisplayed()));

        onView(withId(R.id.searchBtn)).perform(click()); // Click on the search button

        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(20)); // There should be 9 predetermined events

        onView(withId(R.id.eventList)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.button)).perform(click()); // Click on the register button
        onView(isRoot()).perform(pressBack());

        onView(withId(R.id.profileFragment)).perform(click()); // Go to profile fragment
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(1)); // There should be 1 event registered

        onView(withId(R.id.eventList)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.button)).perform(click()); // Click on the unregister button
        onView(withText("YES")).perform(click());
        onView(isRoot()).perform(pressBack());

        onView(withId(R.id.emptyResultText)).check(matches(isDisplayed())); //There should be no events now
    }

    /**
     * Performs a test on logged-in user registration and un-registration of multiple events
     *
     * <ol>
     *   <li>Go to profile page and check if the user has no registered events
     *   <li>Go the explore page and click on the search button. 20 predetermined events should be shown
     *   <li>Register the first three events
     *   <li>Go to profile page and check if the user has 3 registered events
     *   <li>Unregister all of the three events and check if are unregistered
     * </ol>
     */
    @Test
    public void loggedInUser_registerMultipleEventsSuccess() {
        TestUtils.loginTestUser();
        onView(withId(R.id.profileFragment)).perform(click()); // Go to profile fragment

        onView(withId(R.id.emptyResultText)).check(matches(isDisplayed())); // Default user should have no registered events

        onView(withId(R.id.exploreFragment)).perform(click()); // Go to explore fragment
        onView(withId(R.id.ExploreTitle)).check(matches(isDisplayed()));

        onView(withId(R.id.searchBtn)).perform(click()); // Click on the search button

        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(20)); // There should be 20 predetermined events

        onView(withId(R.id.eventList)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.button)).perform(click()); // Click on the register button
        onView(isRoot()).perform(pressBack());

        onView(withId(R.id.eventList)).perform(TestUtils.nestedScrollTo(1), RecyclerViewActions.actionOnItemAtPosition(1, click()));
        onView(withId(R.id.button)).perform(click()); // Click on the register button
        onView(isRoot()).perform(pressBack());

        onView(withId(R.id.eventList)).perform(TestUtils.nestedScrollTo(2), RecyclerViewActions.actionOnItemAtPosition(2, click()));
        onView(withId(R.id.button)).perform(click()); // Click on the register button
        onView(isRoot()).perform(pressBack());

        onView(withId(R.id.profileFragment)).perform(click()); // Go to profile fragment

        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(3)); // There should be 3 events registered

        onView(withId(R.id.eventList)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.button)).perform(click()); // Click on the unregister button
        onView(withText("YES")).perform(click());
        onView(isRoot()).perform(pressBack());
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(2)); // There should be 2 events registered

        onView(withId(R.id.eventList)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.button)).perform(click()); // Click on the unregister button
        onView(withText("YES")).perform(click());
        onView(isRoot()).perform(pressBack());
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(1)); // There should be 1 events registered

        onView(withId(R.id.eventList)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.button)).perform(click()); // Click on the unregister button
        onView(withText("YES")).perform(click());
        onView(isRoot()).perform(pressBack());
        onView(withId(R.id.emptyResultText)).check(matches(isDisplayed())); //There should be no events now
    }

    /**
     * Performs a test on conflicted events registration
     *
     * <ol
     *   <li>Go to profile page and check if the user has no registered events
     *   <li>Go the explore page and search for query "USC". 9 predetermined events should be shown
     *   <li>Register the "USC Basketball Game" event (6th)
     *   <li>Register the "Music Festive" event (8th) and check if conflicting dialogue is displayed
     *   <li>Unregister the two events and check if are unregistered
     * </ol>
     */
    @Test
    public void loggedInUser_registerConflictingEventsSuccess() {
        TestUtils.loginTestUser();
        onView(withId(R.id.profileFragment)).perform(click()); // Go to profile fragment

        onView(withId(R.id.emptyResultText)).check(matches(isDisplayed())); // Default user should have no registered events

        onView(withId(R.id.exploreFragment)).perform(click()); // Go to explore fragment
        onView(withId(R.id.ExploreTitle)).check(matches(isDisplayed()));

        onView(withId(R.id.searchBar)).perform(typeText("USC")); // Click on the search button
        onView(withId(R.id.searchBtn)).perform(click()); // Click on the search button

        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(9)); // There should be 9 predetermined events

        onView(withId(R.id.eventList)).perform(TestUtils.nestedScrollTo(5), RecyclerViewActions.actionOnItemAtPosition(5, click())); // USC Basketball Game
        onView(withId(R.id.button)).perform(click()); // Click on the register button
        onView(isRoot()).perform(pressBack());

        onView(withId(R.id.eventList)).perform(TestUtils.nestedScrollTo(7), RecyclerViewActions.actionOnItemAtPosition(7, click())); // Music Festive
        onView(withId(R.id.button)).perform(click()); // Click on the register button
        onView(withText("Conflicting with registered events")).check(matches(isDisplayed())); // Dialog should be displayed
        onView(withText("OK")).perform(click());
        onView(isRoot()).perform(pressBack());

        onView(withId(R.id.profileFragment)).perform(click()); // Go to profile fragment
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(2)); // There should be 2 events register

        onView(withId(R.id.eventList)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.button)).perform(click()); // Click on the unregister button
        onView(withText("YES")).perform(click());
        onView(isRoot()).perform(pressBack());
        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(1)); // There should be 1 events registered

        onView(withId(R.id.eventList)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.button)).perform(click()); // Click on the unregister button
        onView(withText("YES")).perform(click());
        onView(isRoot()).perform(pressBack());
        onView(withId(R.id.emptyResultText)).check(matches(isDisplayed())); //There should be no events now
    }

    /**
     * Performs a test on guest user profile fragment display
     *
     * <ol>
     *   <li>Check if the correct profile fragment {@link com.example.eventme.fragments.ProfileUnloggedInFragment} is displayed
     * </ol>
     */
    @Test
    public void guestUser_profileFragmentIsCorrect() {
        TestUtils.loginGuest();
        onView(withId(R.id.profileFragment)).perform(click()); // Go to profile fragment

        onView(withText("Get Started")).check(matches(isDisplayed()));
    }

    /**
     * Performs a test on guest user event registration redirection
     *
     * <ol>
     *   <li>Check if guest user is redirected to {@link com.example.eventme.fragments.LoginFragment} when registering an event
     * </ol>
     */
    @Test
    public void guestUser_registerIsRedirected() {
        TestUtils.loginGuest();
        onView(withId(R.id.exploreFragment)).perform(click()); // Go to explore fragment

        onView(withId(R.id.searchBtn)).perform(click()); // Click on the search button

        onView(withId(R.id.eventList)).check(new TestUtils.RecyclerViewEventAssertion(20)); // There should be 20 predetermined events

        onView(withId(R.id.eventList)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click())); // USC Basketball Game
        onView(withId(R.id.button)).perform(click()); // Click on the register button

        onView(withId(R.id.container)).check(matches(isDisplayed())); // Check login fragment is displayed
    }
}
