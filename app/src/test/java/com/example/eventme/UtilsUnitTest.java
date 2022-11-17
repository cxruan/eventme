package com.example.eventme;

import org.junit.Test;

import com.example.eventme.utils.Utils;

import static org.junit.Assert.*;

public class UtilsUnitTest {
    private final double DISTANCE_DELTA = 0.1;

    @Test
    public void formatDate_isCorrect() {
        assertEquals("Sat, 5, Nov, 2022", Utils.formatDate("2022/11/5"));
        assertEquals("Wed, 14, Dec, 2022", Utils.formatDate("2022/12/14"));
        assertEquals("Mon, 6, Feb, 2023", Utils.formatDate("2023/2/6"));
    }

    @Test
    public void distanceBetweenLocations_isCorrect() {
        assertEquals(0, Utils.distanceBetweenLocations(0, 0, 0, 0), DISTANCE_DELTA);
        assertEquals(12579.17, Utils.distanceBetweenLocations(0, 0, 34.0283998, -118.2896084), DISTANCE_DELTA); // Los Angeles to Null Island
        assertEquals(1.22674, Utils.distanceBetweenLocations(34.0259332, -118.2852136, 34.0284707, -118.2722588), DISTANCE_DELTA); // USC Village to the Lorenzo
    }

    @Test
    public void parseTime_12Clock_isCorrect() {
        Integer[] times = Utils.parseTime("9:56pm-11:30pm");
        assertEquals(Integer.valueOf(21), times[0]);
        assertEquals(Integer.valueOf(56), times[1]);
        assertEquals(Integer.valueOf(23), times[2]);
        assertEquals(Integer.valueOf(30), times[3]);
    }

    @Test
    public void parseTime_24Clock_isCorrect() {
        Integer[] times = Utils.parseTime("9:10-20:00");
        assertEquals(Integer.valueOf(9), times[0]);
        assertEquals(Integer.valueOf(10), times[1]);
        assertEquals(Integer.valueOf(20), times[2]);
        assertEquals(Integer.valueOf(0), times[3]);
    }

    @Test
    public void parseTime_Mix_isCorrect() {
        Integer[] times = Utils.parseTime("9:10-9pm");
        assertEquals(Integer.valueOf(9), times[0]);
        assertEquals(Integer.valueOf(10), times[1]);
        assertEquals(Integer.valueOf(21), times[2]);
        assertEquals(Integer.valueOf(0), times[3]);
    }

}
