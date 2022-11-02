package com.example.eventme;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


import com.example.eventme.databinding.ActivityEventRegistrationBinding;

public class EventRegistrationActivity extends AppCompatActivity {
    private static final String TAG = "EventRegistrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEventRegistrationBinding binding = ActivityEventRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String eventId = getIntent().getStringExtra("com.example.eventme.EventRegistration.eventId");

        Log.d(TAG, "onCreate: " + eventId);

    }


}

