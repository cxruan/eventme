package com.example.eventme.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.FirebaseDatabase;

public class MapFragmentViewModelFactory implements ViewModelProvider.Factory {
    private FirebaseDatabase mDatabase;


    public MapFragmentViewModelFactory(FirebaseDatabase database) {
        mDatabase = database;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MapFragmentViewModel(mDatabase);
    }
}