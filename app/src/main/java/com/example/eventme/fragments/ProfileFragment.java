package com.example.eventme.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventme.R;
import com.example.eventme.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;

    private FirebaseAuth mAuth;

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
        // if unauthenticated, prompt to log in or sign up
        if (mAuth.getCurrentUser() == null) {
            NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_profileUnloggedInFragment);
            return;
        }
        Log.d(TAG, "onViewCreated: authenticated");

        // Click listeners
        binding.signOut.setOnClickListener(this::onClickSignOut);
    }

    private void onClickSignOut(View view) {
        mAuth.signOut();
        NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_profileUnloggedInFragment);
    }
}
