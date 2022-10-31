package com.example.eventme.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventme.R;
import com.example.eventme.adapters.EventBoxAdapter;
import com.example.eventme.databinding.FragmentProfileBinding;
import com.example.eventme.models.Event;
import com.example.eventme.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private FragmentProfileBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserReference;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private EventBoxAdapter mEventBoxAdapter;
    private DatabaseReference mEventReference;

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
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        // if unauthenticated, prompt to log in or sign up
        if (firebaseUser == null) {
            NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_profileUnloggedInFragment);
            return;
        }

        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
        mEventReference = FirebaseDatabase.getInstance().getReference().child("events");

        mEventBoxAdapter = new EventBoxAdapter();

        // Set up Layout Manager
        mManager = new LinearLayoutManager(getActivity());
        mRecycler = binding.eventList;
        mRecycler.setLayoutManager(mManager);
        mRecycler.setAdapter(mEventBoxAdapter);

        // Click listeners
        binding.signOut.setOnClickListener(this::onClickSignOut);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            // Hide default value before data loaded
            binding.name.setText("");
            binding.infoRow.setVisibility(View.INVISIBLE);

            // Get user profile data
            mUserReference.get().addOnCompleteListener(userTask -> {
                if (userTask.isSuccessful()) {
                    User user = userTask.getResult().getValue(User.class);
                    binding.name.setText(user.getFirstName() + " " + user.getLastName());
                    binding.birthday.setText(user.getBirthday());
                    binding.email.setText(user.getEmail());
                    binding.infoRow.setVisibility(View.VISIBLE);

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

    private void onClickSignOut(View view) {
        mAuth.signOut();
        NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_profileUnloggedInFragment);
    }
}
