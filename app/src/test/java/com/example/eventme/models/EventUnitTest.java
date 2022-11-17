package com.example.eventme.models;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EventUnitTest {
    @Test
    public void equal_isCorrect() {
        Event e1 = new Event("a");
        Event e2 = new Event("a");
        Event e3 = new Event("c");

        assertTrue(e1.equals(e2));
        assertTrue(!e1.equals(e3));
    }

    @Test
    public void checkTimeConflict_isConflicted() {
        Event e1 = new Event("e1");
        e1.setDate("2022/11/19");
        e1.setTime("8am-11am");
        Event e2 = new Event("e2");
        e2.setDate("2022/11/19");
        e2.setTime("10:30am-11am");
        Event e3 = new Event("e3");
        e3.setDate("2022/11/19");
        e3.setTime("11am-11:45am");
        Event e4 = new Event("e4");
        e4.setDate("2022/11/19");
        e4.setTime("11:01am-1pm");

        assertTrue(Event.checkTimeConflict(e1, e2));
        assertTrue(Event.checkTimeConflict(e3, e4));
    }

    @Test
    public void checkTimeConflict_isNotConflicted() {
        Event e1 = new Event("e1");
        e1.setDate("2022/11/19");
        e1.setTime("8am-11am");
        Event e2 = new Event("e2");
        e2.setDate("2022/11/20");
        e2.setTime("10:30am-11am");
        Event e3 = new Event("e3");
        e3.setDate("2022/11/19");
        e3.setTime("11am-11:45am");
        Event e4 = new Event("e4");
        e4.setDate("2022/11/19");
        e4.setTime("11:46am-1pm");

        assertFalse(Event.checkTimeConflict(e1, e2));
        assertFalse(Event.checkTimeConflict(e3, e4));
    }

    @Test
    public void nameComparator_isCorrect() {
        List<Event> list = new ArrayList<>();
        Event e1 = new Event("e1");
        e1.setName("a");
        list.add(e1);
        Event e2 = new Event("e2");
        e2.setName("b");
        list.add(e2);
        Event e3 = new Event("e3");
        e3.setName("c");
        list.add(e3);

        Collections.shuffle(list);
        list.sort(new Event.EventNameComparator());

        assertEquals(list.get(0), e1);
        assertEquals(list.get(1), e2);
        assertEquals(list.get(2), e3);
    }

    @Test
    public void costComparator_isCorrect() {
        List<Event> list = new ArrayList<>();
        Event e1 = new Event("e1");
        e1.setCost(0.0);
        list.add(e1);
        Event e2 = new Event("e2");
        e2.setCost(1.5);
        list.add(e2);
        Event e3 = new Event("e3");
        e3.setCost(13.2);
        list.add(e3);

        Collections.shuffle(list);
        list.sort(new Event.EventCostComparator());

        assertEquals(list.get(0), e1);
        assertEquals(list.get(1), e2);
        assertEquals(list.get(2), e3);
    }

    @Test
    public void dateComparator_isCorrect() {
        List<Event> list = new ArrayList<>();
        Event e1 = new Event("e1");
        e1.setDate("2022/11/19");
        list.add(e1);
        Event e2 = new Event("e2");
        e2.setDate("2022/11/20");
        list.add(e2);
        Event e3 = new Event("e3");
        e3.setDate("2022/12/19");
        list.add(e3);

        Collections.shuffle(list);
        list.sort(new Event.EventDateComparator());

        assertEquals(list.get(0), e1);
        assertEquals(list.get(1), e2);
        assertEquals(list.get(2), e3);
    }

    @Test
    public void distanceComparator_isCorrect() {
        List<Event> list = new ArrayList<>();
        Event e1 = new Event("e1");
        HashMap<String, Double> g1 = new HashMap<>(); // USC Village
        g1.put("lat", 34.0259332);
        g1.put("lng", -118.2852136);
        e1.setGeoLocation(g1);
        list.add(e1);

        Event e2 = new Event("e2");
        HashMap<String, Double> g2 = new HashMap<>(); // The Lorenzo
        g2.put("lat", 34.0284707);
        g2.put("lng", -118.2722588);
        e2.setGeoLocation(g2);
        list.add(e2);

        Event e3 = new Event("e3");
        HashMap<String, Double> g3 = new HashMap<>(); // Universal Studios Hollywood
        g3.put("lat", 34.136558532714844);
        g3.put("lng", -118.35588836669922);
        e3.setGeoLocation(g3);
        list.add(e3);

        Event e4 = new Event("e4");
        HashMap<String, Double> g4 = new HashMap<>(); // Shanghai, China
        g4.put("lat", 31.2322758);
        g4.put("lng", 121.4692071);
        e4.setGeoLocation(g4);
        list.add(e4);

        Collections.shuffle(list);
        list.sort(new Event.EventDistanceComparator(34.02096939086914, -118.28555297851562)); // USC as the origin

        assertEquals(list.get(0), e1);
        assertEquals(list.get(1), e2);
        assertEquals(list.get(2), e3);
        assertEquals(list.get(3), e4);
    }
}