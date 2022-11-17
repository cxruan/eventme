package com.example.eventme;

import org.junit.Test;

import com.example.eventme.utils.Utils;

import static org.junit.Assert.*;

public class UtilsUnitTest {
    @Test
    public void testStuff() {
        assertEquals(0, Utils.distanceBetweenLocations(0, 0, 0, 0), 0);
    }

    @Test
    public void testDistanceLAtoNullIsland() {
        assertEquals(12580, Utils.distanceBetweenLocations(0, 0, 34.0283998, -118.2896084), 10);
    }
}
