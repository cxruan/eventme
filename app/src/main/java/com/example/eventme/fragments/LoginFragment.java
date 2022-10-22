package com.example.eventme.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eventme.R;
import com.example.eventme.databinding.FragmentLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Click listeners
        binding.signIn.setOnClickListener(this::onClickSignIn);
        binding.signUp.setOnClickListener(this::onClickSignUp);
    }

    private boolean validate() {
        boolean valid = true;

        if (TextUtils.isEmpty(binding.email.getText().toString()) ) {
            binding.email.setError("Required");
            binding.email.requestFocus();
            valid = false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()){
            binding.email.setError("Invalid email");
            binding.email.requestFocus();
            valid = false;
        }else {
            binding.email.setError(null);
        }

        if (TextUtils.isEmpty(binding.password.getText().toString())) {
            binding.password.setError("Required");
            binding.email.requestFocus();
            valid = false;
        } else {
            binding.password.setError(null);
        }

        return valid;
    }

    private void signIn() {
        Log.d(TAG, "signIn");

        if (!validate()) {
            return;
        }

        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), task -> {
            Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());

            if (task.isSuccessful()) {
                onSignInSuccess(task.getResult().getUser());
            } else {
                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                Toast.makeText(getContext(), "Sign In Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onSignInSuccess(FirebaseUser user) {
        // Go to NavActivity
        NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_navActivity);
    }

    private void onClickSignIn(View view) {
        signIn();
    }

    private void onClickSignUp(View view) {
        // Go to SignUpFragment
        NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_signUpFragment);
    }

}