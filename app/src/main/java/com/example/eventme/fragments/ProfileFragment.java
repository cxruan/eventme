package com.example.eventme.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventme.R;
import com.example.eventme.adapters.EventBoxAdapter;
import com.example.eventme.databinding.FragmentProfileBinding;
import com.example.eventme.utils.GlideApp;
import com.example.eventme.viewmodels.ProfileFragmentViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private EventBoxAdapter mEventBoxAdapter;
    ActivityResultLauncher<String> mGetContent;

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

        // If unauthenticated, prompt to log in or sign up
        if (mAuth.getCurrentUser() == null) {
            NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_profileUnloggedInFragment);
            return;
        }

        // Set up Adapter
        mEventBoxAdapter = new EventBoxAdapter();

        // Set up RecyclerView
        mManager = new LinearLayoutManager(getActivity());
        mRecycler = binding.eventList;
        mRecycler.setLayoutManager(mManager);
        mRecycler.setAdapter(mEventBoxAdapter);

        // Hide info until data fetched
        binding.name.setVisibility(View.INVISIBLE);
        binding.infoRow.setVisibility(View.INVISIBLE);

        // Subscribe to ViewModel data
        ProfileFragmentViewModel model = new ViewModelProvider(requireActivity()).get(ProfileFragmentViewModel.class);
        model.getUserData().observe(getViewLifecycleOwner(), user -> {
            binding.name.setText(user.getFirstName() + " " + user.getLastName());
            binding.birthday.setText(user.getBirthday());
            binding.email.setText(user.getEmail());

            // Show info now
            binding.name.setVisibility(View.VISIBLE);
            binding.infoRow.setVisibility(View.VISIBLE);
            String path = user.getProfilePicture();
            if (path != null) // Profile picture found
                loadProfilePicture(path);
            else // No profile picture, use default person drawable
                binding.profilePic.setImageResource(R.drawable.ic_baseline_person_24);
        });
        model.getRegisteredEventsData().observe(getViewLifecycleOwner(), events -> {
            mEventBoxAdapter.setItems(events);
        });


        // Set up ActivityResultLauncher for picture uploading
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
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
                    .addOnSuccessListener(taskSnapshot -> mDatabase.getReference().child("users").child(mAuth.getUid()).child("profilePicture").setValue(path, (error, reference) -> {
                        loadProfilePicture(path);
                        model.updateUserData(); // Update ViewModel after profile picture uploaded successfully
                        Toast.makeText(getContext(), "Profile picture uploaded successfully", Toast.LENGTH_LONG).show();
                    }));
        });

        // Click listeners
        binding.signOut.setOnClickListener(this::onClickSignOut);
        binding.profilePic.setOnClickListener(this::onClickUploadProfilePic);
    }


    private void loadProfilePicture(String path) {
        StorageReference ref = mStorage.getReference().child(path);
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
