package com.example.eventme.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static final String TAG = "Utils";

    public static String formatDate(String date) {
        try {
            Locale locale = Locale.getDefault();
            SimpleDateFormat curFormatter = new SimpleDateFormat("yyyy/MM/dd", locale);
            Date dateObj = curFormatter.parse(date);
            SimpleDateFormat postFormatter = new SimpleDateFormat("EEE, d, MMM, yyyy", locale);
            return postFormatter.format(dateObj);
        } catch (ParseException e) {
            Log.e(TAG, "formatDate: ", e);
        }
        return date;
    }

    public static double distanceBetweenLocations(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat1 - lat2);
        double dLon = Math.toRadians(lng1 - lng2);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));

        return rad * c;
    }

    public static Integer[] parseTime(String timeStr) {
        String[] timeArr = timeStr.toLowerCase().split("-");

        String startTimeStr = timeArr[0].substring(0, timeArr[0].length() - 2);
        String[] startTime = startTimeStr.split(":");
        Integer startHour = Integer.valueOf(startTime[0]);

        if (timeArr[0].endsWith("pm"))
            startHour += 12;
        Integer startMin = 0;
        if (startTime.length == 2)
            startMin = Integer.valueOf(startTime[1]);

        String endTimeStr = timeArr[1].substring(0, timeArr[1].length() - 2);
        String[] endTime = endTimeStr.split(":");
        Integer endHour = Integer.valueOf(endTime[0]);

        if (timeArr[1].endsWith("pm"))
            endHour += 12;
        Integer endMin = 0;
        if (endTime.length == 2)
            endMin = Integer.valueOf(endTime[1]);

        return new Integer[]{startHour, startMin, endHour, endMin};
    }
}
