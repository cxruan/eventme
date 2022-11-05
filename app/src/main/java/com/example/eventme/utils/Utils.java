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
}
