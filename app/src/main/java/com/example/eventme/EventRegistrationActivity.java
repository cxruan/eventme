package com.example.eventme;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventme.databinding.ActivityEventRegistrationBinding;
import com.example.eventme.models.Event;
import com.example.eventme.utils.GlideApp;
import com.example.eventme.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EventRegistrationActivity extends AppCompatActivity {
    private static final String TAG = "EventRegistrationActivity";

    private ActivityEventRegistrationBinding binding;

    private String mEventId;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mEventId = getIntent().getStringExtra("com.example.eventme.EventRegistration.eventId");
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Hide event info until data fetched
        binding.infoContainer.setVisibility(View.INVISIBLE);

        mDatabase.getReference().child("events").child(mEventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Event event = task.getResult().getValue(Event.class);

                String uri = event.getPhotoURI();
                if (uri != null)
                    loadProfilePicture(uri);
                else
                    binding.photo.setImageResource(R.drawable.default_event_photo);
                binding.name.setText(event.getName());
                binding.date.setText(Utils.formatDate(event.getDate()));
                binding.time.setText(event.getTime());
                binding.location.setText(event.getLocation());
                binding.sponsor.setText(event.getSponsor());
                if (event.getParkingAvailable()) {
                    binding.parking.setText("Parking Available");
                    binding.parking.setTextColor(getColor(R.color.green));
                } else {
                    binding.parking.setText("Parking Unavailable");
                    binding.parking.setTextColor(getColor(R.color.red));
                }
                binding.people.setText(String.valueOf(event.getRegisteredUsers().size()));
                binding.description.setText(event.getDescription());

                binding.infoContainer.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "Error getting event data", task.getException());
            }
        });
    }

    private void loadProfilePicture(String uri) {
        StorageReference ref = mStorage.getReferenceFromUrl(uri);
        GlideApp.with(this)
                .load(ref)
                .fitCenter()
                .into(binding.photo);
    }
}

