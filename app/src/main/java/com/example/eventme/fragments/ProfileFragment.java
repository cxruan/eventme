package com.example.eventme.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventme.BuildConfig;
import com.example.eventme.R;
import com.example.eventme.databinding.FragmentProfileBinding;
import com.example.eventme.models.Event;
import com.example.eventme.utils.GlideApp;
import com.example.eventme.utils.Utils;
import com.example.eventme.viewmodels.EventListFragmentViewModel;
import com.example.eventme.viewmodels.ProfileFragmentViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private FragmentProfileBinding binding;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    ActivityResultLauncher<String> mGetContent;
    ProfileFragmentViewModel mViewModel;
    private EventListFragmentViewModel mListViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        if (BuildConfig.DEBUG) {
            mAuth.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_AUTH_PORT);
            mDatabase.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_DATABASE_PORT);
            mStorage.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_STORAGE_PORT);
        }

        // If unauthenticated, prompt to log in or sign up
        if (mAuth.getCurrentUser() == null) {
            NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_profileUnloggedInFragment);
            return;
        }

        // Hide info until data fetched
        binding.name.setVisibility(View.INVISIBLE);
        binding.infoRow.setVisibility(View.INVISIBLE);

        // Subscribe to ViewModel data
        mViewModel = new ViewModelProvider(requireActivity()).get(ProfileFragmentViewModel.class);
        mListViewModel = new ViewModelProvider(this).get(EventListFragmentViewModel.class);

        mViewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            binding.name.setText(user.getFirstName() + " " + user.getLastName());
            binding.birthday.setText(user.getBirthday());
            binding.email.setText(user.getEmail());

            // Show info now
            binding.name.setVisibility(View.VISIBLE);
            binding.infoRow.setVisibility(View.VISIBLE);
            String uri = user.getProfilePictureURI();
            if (uri != null) // Profile picture found
                loadProfilePicture(uri);
            else // No profile picture, use default person drawable
            {
                binding.profilePic.setImageResource(R.drawable.ic_baseline_person_24);
                binding.profilePicOverlay.setVisibility(View.VISIBLE);
            }
        });
        mViewModel.getRegisteredEventsData().observe(getViewLifecycleOwner(), events -> {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            try {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        for (Event event : events) {
                            double distance = Utils.distanceBetweenLocations(location.getLatitude(), location.getLongitude(), event.getGeoLocation().get("lat"), event.getGeoLocation().get("lng"));
                            event.setDistanceFromUserLocation(distance);
                        }
                    }
                    mListViewModel.setEventsData(events);
                });
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage(), e);
            }
        });


        // Set up ActivityResultLauncher for picture uploading
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                // Handle the returned Uri
                String path = "users/" + mAuth.getUid() + "/" + uri.getLastPathSegment();
                StorageReference ref = mStorage.getReference().child(path);

                UploadTask uploadTask = ref.putFile(uri);

                // Register observers to listen for when the download is done or if it fails
                uploadTask
                        .addOnFailureListener(exception -> {
                            // Handle unsuccessful uploads
                            Log.e(TAG, "Error uploading profile picture", exception);
                            Toast.makeText(getContext(), "Error uploading profile picture", Toast.LENGTH_LONG).show();
                        })
                        .addOnSuccessListener(taskSnapshot -> {
                            String newUri = Uri.decode(taskSnapshot.getStorage().toString());// Has to decode cause getReferenceFromUrl accepts only decoded uri

                            mDatabase.getReference().child("users").child(mAuth.getUid()).child("profilePictureURI").setValue(newUri, (error, reference) -> {
                                loadProfilePicture(newUri);
                                mViewModel.updateUserData(); // Update ViewModel after profile picture uploaded successfully
                                Toast.makeText(getContext(), "Profile picture uploaded successfully", Toast.LENGTH_LONG).show();
                            });
                        });
            }
        });

        // Click listeners
        binding.signOut.setOnClickListener(this::onClickSignOut);
        binding.profilePic.setOnClickListener(this::onClickUploadProfilePic);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mViewModel != null)
            mViewModel.loadAllData();
    }

    private void loadProfilePicture(String uri) {
        StorageReference ref = mStorage.getReferenceFromUrl(uri);
        binding.profilePicOverlay.setVisibility(View.GONE);
        GlideApp.with(this)
                .load(ref)
                .circleCrop()
                .into(binding.profilePic);
    }

    private void onClickSignOut(View view) {
        mAuth.signOut();
        NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_profileUnloggedInFragment);
    }

    private void onClickUploadProfilePic(View view) {
        mGetContent.launch("image/*");
    }
}
