package com.example.eventme.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventme.R;
import com.example.eventme.databinding.FragmentSignupBinding;
import com.example.eventme.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// TODO: Add birthday
public class SignUpFragment extends Fragment {
    private static final String TAG = "SignUpFragment";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private FragmentSignupBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Click listeners
        binding.signUp.setOnClickListener(this::onClickSignUp);
    }

    private void onClickSignUp(View view) {
        signUp();
    }

    private void signUp() {
        Log.d(TAG, "signUp");

        if (!validate()) {
            return;
        }

        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();
        String firstName = binding.firstName.getText().toString();
        String lastName = binding.lastName.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());

                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser(), firstName, lastName);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Sign-up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean validate() {
        boolean valid = true;

        if (TextUtils.isEmpty(binding.firstName.getText().toString())) {
            binding.firstName.setError("Required");
            binding.firstName.requestFocus();
            valid = false;
        } else {
            binding.firstName.setError(null);
        }

        if (TextUtils.isEmpty(binding.lastName.getText().toString())) {
            binding.lastName.setError("Required");
            binding.lastName.requestFocus();
            valid = false;
        } else {
            binding.lastName.setError(null);
        }

        if (TextUtils.isEmpty(binding.email.getText().toString())) {
            binding.email.setError("Required");
            binding.email.requestFocus();
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()) {
            binding.email.setError("Invalid email");
            binding.email.requestFocus();
            valid = false;
        } else {
            binding.email.setError(null);
        }

        if (TextUtils.isEmpty(binding.password.getText().toString())) {
            binding.password.setError("Required");
            binding.password.requestFocus();
            valid = false;
        } else {
            binding.password.setError(null);
        }

        if (!binding.reEnterPassword.getText().toString().equals(binding.password.getText().toString())) {
            binding.reEnterPassword.setError("Unmatched passwords");
            binding.reEnterPassword.requestFocus();
            valid = false;
        } else {
            binding.reEnterPassword.setError(null);
        }

        return valid;
    }

    private void onAuthSuccess(FirebaseUser firebaseUser, String firstName, String lastName){
        String userId = firebaseUser.getUid();
        User user = new User(userId, firstName, lastName, firebaseUser.getEmail());

        // Write new user information to Firebase Realtime Database
        mDatabase.child("users").child(userId).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Go to LoginFragment
                NavHostFragment.findNavController(this).navigate(R.id.action_signUpFragment_to_loginFragment);
                Toast.makeText(getContext(), "Sign-up Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Sign-up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



}
