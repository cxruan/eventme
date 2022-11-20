package com.example.eventme;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.util.HumanReadables;

import com.example.eventme.adapters.EventBoxAdapter;
import com.example.eventme.models.Event;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

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
                        assertThat(event.getTypes().containsKey(type), is(true));
                    }
                }
            }
        }
    }

    // Adapted from https://stackoverflow.com/a/46037284
    public static ViewAction nestedScrollTo(int position) {
        return new ViewAction() {

            @Override
            public Matcher<View> getConstraints() {
                return Matchers.allOf(isDescendantOfA(isAssignableFrom(NestedScrollView.class)), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE));
            }

            @Override
            public String getDescription() {
                return "View is not NestedScrollView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                try {
                    RecyclerView recyclerView = (RecyclerView) view;
                    View child = recyclerView.getChildAt(position);
                    NestedScrollView nestedScrollView = (NestedScrollView) findFirstParentLayoutOfClass(view, NestedScrollView.class);
                    if (nestedScrollView != null) {
                        nestedScrollView.scrollTo(0, child.getTop());
                    } else {
                        throw new Exception("Unable to find NestedScrollView parent.");
                    }
                } catch (Exception e) {
                    throw new PerformException.Builder()
                            .withActionDescription(this.getDescription())
                            .withViewDescription(HumanReadables.describe(view))
                            .withCause(e)
                            .build();
                }
                uiController.loopMainThreadUntilIdle();
            }

        };
    }

    private static View findFirstParentLayoutOfClass(View view, Class<? extends View> parentClass) {
        ViewParent parent = new FrameLayout(view.getContext());
        ViewParent incrementView = null;
        int i = 0;
        while (parent != null && !(parent.getClass() == parentClass)) {
            if (i == 0) {
                parent = findParent(view);
            } else {
                parent = findParent(incrementView);
            }
            incrementView = parent;
            i++;
        }
        return (View) parent;
    }

    private static ViewParent findParent(View view) {
        return view.getParent();
    }

    private static ViewParent findParent(ViewParent view) {
        return view.getParent();
    }
}
