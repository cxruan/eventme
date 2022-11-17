package com.example.eventme.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileFragmentViewModelFactory implements ViewModelProvider.Factory {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;


    public ProfileFragmentViewModelFactory(FirebaseAuth auth, FirebaseDatabase database) {
        mAuth = auth;
        mDatabase = database;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new ProfileFragmentViewModel(mAuth, mDatabase);
    }
}