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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventme.R;
import com.example.eventme.adapters.EventBoxAdapter;
import com.example.eventme.databinding.FragmentProfileBinding;
import com.example.eventme.models.Event;
import com.example.eventme.models.User;
import com.example.eventme.utils.GlideApp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private FragmentProfileBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserReference;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private EventBoxAdapter mEventBoxAdapter;
    private DatabaseReference mEventReference;
    ActivityResultLauncher<String> mGetContent;
    private FirebaseStorage mStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStorage = FirebaseStorage.getInstance();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        // if unauthenticated, prompt to log in or sign up
        if (firebaseUser == null) {
            NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_profileUnloggedInFragment);
            return;
        }

        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
        mEventReference = FirebaseDatabase.getInstance().getReference().child("events");

        mEventBoxAdapter = new EventBoxAdapter();

        // Set up RecyclerView
        mManager = new LinearLayoutManager(getActivity());
        mRecycler = binding.eventList;
        mRecycler.setLayoutManager(mManager);
        mRecycler.setAdapter(mEventBoxAdapter);

        // Set up ActivityResultLauncher for picture uploading
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                // Handle the returned Uri
                String path = "users/" + mAuth.getUid() + "/" + uri.getLastPathSegment();
                StorageReference ref = mStorage.getReference().child(path);

                UploadTask uploadTask = ref.putFile(uri);

                // Register observers to listen for when the download is done or if it fails
                uploadTask
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Log.e(TAG, "Error uploading profile picture", exception);
                                Toast.makeText(getContext(), "Error uploading profile picture", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mUserReference.child("profilePicture").setValue(path, (error, reference) -> {
                                    loadProfilePicture(path);
                                    Toast.makeText(getContext(), "Profile picture uploaded successfully", Toast.LENGTH_LONG).show();
                                });
                            }
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

        if (mAuth.getCurrentUser() != null) {
            // Hide default value before data loaded
            binding.name.setText("");
            binding.infoRow.setVisibility(View.INVISIBLE);
            binding.profilePic.setImageResource(android.R.color.transparent);

            // Get user profile data
            mUserReference.get().addOnCompleteListener(userTask -> {
                if (userTask.isSuccessful()) {
                    User user = userTask.getResult().getValue(User.class);
                    binding.name.setText(user.getFirstName() + " " + user.getLastName());
                    binding.birthday.setText(user.getBirthday());
                    binding.email.setText(user.getEmail());
                    binding.infoRow.setVisibility(View.VISIBLE);
                    String path = user.getProfilePicture();
                    if (path != null)
                        loadProfilePicture(path);

                    // Clear all previously fetched events
                    mEventBoxAdapter.clearAllItem();
                    // Get registered events
                    for (String id : user.getRegisteredEvents().keySet()) {
                        mEventReference.child(id).get().addOnCompleteListener(eventTask -> {
                            if (eventTask.isSuccessful()) {
                                Event event = eventTask.getResult().getValue(Event.class);
                                mEventBoxAdapter.addItem(event);
                            } else {
                                Log.e(TAG, "Error getting event", eventTask.getException());
                            }
                        });
                    }

                } else {
                    Log.e(TAG, "Error getting user data", userTask.getException());
                }
            });
        }

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
