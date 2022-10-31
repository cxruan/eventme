package com.example.eventme.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventme.R;
import com.example.eventme.databinding.FragmentProfileUnloggedinBinding;

public class ProfileUnloggedInFragment extends Fragment {
    private FragmentProfileUnloggedinBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileUnloggedinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Click listeners
        binding.button.setOnClickListener(this::onClickSignInOrUp);
    }

    private void onClickSignInOrUp(View view) {
        // Go to sign in
        NavHostFragment.findNavController(this).navigate(R.id.action_profileUnloggedInFragment_to_mainActivity);
    }
}
