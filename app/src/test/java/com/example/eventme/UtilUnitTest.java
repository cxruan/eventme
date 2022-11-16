package com.example.eventme;

import org.junit.Before;
import org.junit.Test;
import com.example.eventme.fragments.MapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import static org.junit.Assert.*;

public class MapFragmentUnitTest {
    private MapFragment tester;
    @Before
    public void setup() {
        tester = new MapFragment();
    }
    @Test
    public void testStuff(){
        assertEquals(false, tester.isLocationPermissionGranted());
    }

}
