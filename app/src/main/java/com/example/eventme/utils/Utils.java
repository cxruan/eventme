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
}
