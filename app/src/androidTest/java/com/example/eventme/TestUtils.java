package com.example.eventme;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.is;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;

import com.example.eventme.adapters.EventBoxAdapter;
import com.example.eventme.models.Event;

import java.util.List;


public class TestUtils {
    static public void loginTestUser() {
        onView(withId(R.id.email)).perform(typeText("abc@xyz.com"));
        onView(withId(R.id.password)).perform(typeText("123456"));

        onView(withId(R.id.signIn)).perform(click());

        onView(withId(R.id.ExploreTitle)).check(matches(isDisplayed()));
    }

    static public void loginGuest() {
        onView(withId(R.id.anonymousSignIn)).perform(click());

        onView(withId(R.id.ExploreTitle)).check(matches(isDisplayed()));
    }

    static public class RecyclerViewEventAssertion implements ViewAssertion {
        private final int expectedCount;
        private String searchQuery;
        private String sortedBy;
        private String[] types;

        public RecyclerViewEventAssertion(int expectedCount) {
            this.expectedCount = expectedCount;
        }

        public RecyclerViewEventAssertion(int expectedCount, String searchQuery) {
            this.expectedCount = expectedCount;
            this.searchQuery = searchQuery.toLowerCase();
        }

        public RecyclerViewEventAssertion(int expectedCount, String searchQuery, String sortedBy) {
            this.expectedCount = expectedCount;
            this.searchQuery = searchQuery.toLowerCase();
            this.sortedBy = sortedBy.toLowerCase();
        }

        public RecyclerViewEventAssertion(int expectedCount, String searchQuery, String sortedBy, String[] types) {
            this.expectedCount = expectedCount;
            this.searchQuery = searchQuery.toLowerCase();
            this.sortedBy = sortedBy.toLowerCase();
            this.types = types;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            EventBoxAdapter adapter = (EventBoxAdapter) recyclerView.getAdapter();

            assertThat(adapter.getItemCount(), is(expectedCount));

            List<Event> events = adapter.getAllItems();

            if (searchQuery != null) {
                for (Event event : events) {
                    Boolean matchesQuery = event.getName().toLowerCase().startsWith(searchQuery)
                            || event.getLocation().toLowerCase().startsWith(searchQuery)
                            || event.getSponsor().toLowerCase().startsWith(searchQuery);
                    assertThat(matchesQuery, is(true));
                }
            }

            if (sortedBy != null) {
                for (int i = 1; i < events.size(); i++) {
                    if (sortedBy.equals("cost"))
                        assertThat(events.get(i).getCost() >= events.get(i - 1).getCost(), is(true));
                    else if (sortedBy.equals("date"))
                        assertThat(events.get(i).getDate().compareTo(events.get(i - 1).getDate()) >= 0, is(true));
                    else if (sortedBy.equals("alphabetical"))
                        assertThat(events.get(i).getName().compareTo(events.get(i - 1).getName()) >= 0, is(true));
                    else if (sortedBy.equals("location"))
                        assertThat(events.get(i).getDistanceFromUserLocation() >= events.get(i - 1).getDistanceFromUserLocation(), is(true));
                }
            }

            if (types != null) {
                for (Event event : events) {
                    for (String type : types) {
                        assertThat(event.getTypes().containsKey(type.toLowerCase()), is(true));
                    }
                }
            }
        }
    }
}
