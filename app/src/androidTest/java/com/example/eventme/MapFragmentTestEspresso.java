package com.example.eventme;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.runner.RunWith;
import androidx.test.filters.LargeTest;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapFragmentTestEspresso {

    @Rule public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);
}
