package com.example.eventme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventme.databinding.ActivityEventRegistrationBinding;
import com.example.eventme.models.Event;
import com.example.eventme.models.User;
import com.example.eventme.utils.GlideApp;
import com.example.eventme.utils.Utils;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class EventRegistrationActivity extends AppCompatActivity {
    private static final String TAG = "EventRegistrationActivity";

    private ActivityEventRegistrationBinding binding;

    private String mEventId;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private Event mEvent;
    private AlertDialog mConflictingAlert;
    private AlertDialog mUnregisterAlert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEventId = getIntent().getStringExtra("com.example.eventme.EventRegistration.eventId");
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        // Set up conflicting event alert dialog
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Are you sure to proceed?")
                .setTitle("Conflicting with registered events");
        builder1.setPositiveButton("OK", (dialog, which) -> {
            registerEvent();
        });
        builder1.setNegativeButton("Cancel", (dialog, which) -> {
        });

        mConflictingAlert = builder1.create();

        // Set up un-registration alert dialog
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Confirm to unregister");
        builder2.setPositiveButton("Yes", (dialog, which) -> {
            unRegisterEvent();
        });
        builder2.setNegativeButton("No", (dialog, which) -> {
        });
        mUnregisterAlert = builder2.create();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Hide event info until data fetched
        binding.infoContainer.setVisibility(View.INVISIBLE);
        loadData();
    }

    private void loadData() {
        mDatabase.getReference().child("events").child(mEventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mEvent = task.getResult().getValue(Event.class);

                String uri = mEvent.getPhotoURI();
                if (uri != null)
                    loadProfilePicture(uri);
                else
                    binding.photo.setImageResource(R.drawable.default_event_photo);
                binding.name.setText(mEvent.getName());
                binding.date.setText(Utils.formatDate(mEvent.getDate()));
                binding.time.setText(mEvent.getTime());
                binding.location.setText(mEvent.getLocation());
                binding.sponsor.setText(mEvent.getSponsor());
                if (mEvent.getParkingAvailable()) {
                    binding.parking.setText("Parking Available");
                    binding.parking.setTextColor(getColor(R.color.green));
                } else {
                    binding.parking.setText("Parking Unavailable");
                    binding.parking.setTextColor(getColor(R.color.red));
                }
                binding.people.setText(String.valueOf(mEvent.getRegisteredUsers().size()));
                binding.description.setText(mEvent.getDescription());

                binding.infoContainer.setVisibility(View.VISIBLE);

                if (mAuth.getCurrentUser() == null) {
                    binding.button.setText("Register");
                    binding.button.setOnClickListener(this::onClickUnauthenticated);
                } else if (mEvent.getRegisteredUsers().containsKey(mAuth.getUid())) {
                    binding.button.setText("Unregister");
                    binding.button.setOnClickListener(this::onClickUnRegister);
                } else {
                    binding.button.setText("Register");
                    binding.button.setOnClickListener(this::onClickRegister);
                }

                for (String type : mEvent.getTypes().keySet()) {
                    Chip chip = new Chip(this);
                    chip.setText(type);
                    binding.types.addView(chip);
                }
            } else {
                Log.e(TAG, "Error getting event data", task.getException());
            }
        });
    }

    private void onClickUnauthenticated(View view) {
        Toast.makeText(getApplicationContext(), "To register events, log in first", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, MainActivity.class));
    }

    private void onClickRegister(View view) {
        AtomicBoolean conflicted = new AtomicBoolean(false);
        AtomicInteger count = new AtomicInteger(0);

        mDatabase.getReference().child("users").child(mAuth.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().getValue(User.class);
                // Get registered events
                if (user.getRegisteredEvents().isEmpty())
                    registerEvent();

                for (String id : user.getRegisteredEvents().keySet()) {
                    mDatabase.getReference().child("events").child(id).get().addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful()) {
                            Event event = eventTask.getResult().getValue(Event.class);
                            count.addAndGet(1);

                            if (Event.checkTimeConflict(event, mEvent))
                                conflicted.set(true);

                            // Finished loading all events
                            if (count.get() == user.getRegisteredEvents().size()) {
                                if (conflicted.get()) {
                                    mConflictingAlert.show();
                                } else {
                                    registerEvent();
                                }
                            }

                        } else {
                            Log.e(TAG, "Error getting event", eventTask.getException());
                        }
                    });
                }
            } else {
                Log.e(TAG, "Error getting user", task.getException());
            }
        });

    }

    private void registerEvent() {
        mDatabase.getReference().child("events").child(mEventId).child("registeredUsers").child(mAuth.getUid()).setValue(true).addOnCompleteListener(eventTask -> {
            if (eventTask.isSuccessful()) {
                mDatabase.getReference().child("users").child(mAuth.getUid()).child("registeredEvents").child(mEventId).setValue(true).addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Event registered successfully", Toast.LENGTH_LONG).show();
                        loadData();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed registering event", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onClickUnRegister: ", userTask.getException());
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Failed registering event", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onClickUnRegister: ", eventTask.getException());
            }
        });
    }

    private void onClickUnRegister(View view) {
        mUnregisterAlert.show();
    }

    private void unRegisterEvent() {
        mDatabase.getReference().child("events").child(mEventId).child("registeredUsers").child(mAuth.getUid()).removeValue().addOnCompleteListener(eventTask -> {
            if (eventTask.isSuccessful()) {
                mDatabase.getReference().child("users").child(mAuth.getUid()).child("registeredEvents").child(mEventId).removeValue().addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Event unregistered successfully", Toast.LENGTH_LONG).show();
                        loadData();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed unregistering event", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onClickUnRegister: ", userTask.getException());
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Failed unregistering event", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onClickUnRegister: ", eventTask.getException());
            }
        });
    }

    private void loadProfilePicture(String uri) {
        StorageReference ref = mStorage.getReferenceFromUrl(uri);
        GlideApp.with(getBaseContext())
                .load(ref)
                .fitCenter()
                .into(binding.photo);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return (super.onOptionsItemSelected(item));
    }
}

