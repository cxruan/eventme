package com.example.eventme;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventme.databinding.ActivityProfileEditBinding;
import com.example.eventme.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;

public class ProfileEditActivity extends AppCompatActivity {
    private static final String TAG = "ProfileEditActivity";

    private ActivityProfileEditBinding binding;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener mDateListener;

    private User userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        if (BuildConfig.DEBUG) {
            mAuth.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_AUTH_PORT);
            mDatabase.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_DATABASE_PORT);
            mStorage.useEmulator("10.0.2.2", BuildConfig.FIREBASE_EMULATOR_STORAGE_PORT);
        }

        myCalendar = Calendar.getInstance();
        mDateListener = (vw, year, month, day) -> binding.birthday.setText(year + "/" + month + "/" + (day < 10 ? "0" + day : day));

        if (mAuth.getCurrentUser() != null) {
            mDatabase.getReference().child("users").child(mAuth.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult().getValue(User.class);

                    userData = user;
                    binding.firstName.setText(user.getFirstName());
                    binding.lastName.setText(user.getLastName());
                    binding.birthday.setText(user.getBirthday());

                    binding.save.setEnabled(false);
                } else {
                    Log.e(TAG, "onCreate: ", task.getException());
                }
            });
        }

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Enable save button when fields change
                if (!binding.firstName.getText().toString().equals(userData.getFirstName())
                        || !binding.lastName.getText().toString().equals(userData.getLastName())
                        || !binding.birthday.getText().toString().equals(userData.getBirthday())) {
                    binding.save.setEnabled(true);
                } else {
                    binding.save.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        binding.birthday.setOnClickListener(this::onClickBirthday);
        binding.save.setOnClickListener(this::onClickSave);
        binding.firstName.addTextChangedListener(tw);
        binding.lastName.addTextChangedListener(tw);
        binding.birthday.addTextChangedListener(tw);
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

        if (TextUtils.isEmpty(binding.birthday.getText().toString())) {
            binding.birthday.setError("Required");
            binding.birthday.requestFocus();
            valid = false;
        } else {
            binding.birthday.setError(null);
        }

        return valid;
    }

    private void onClickSave(View view) {
        if (!validate())
            return;

        if (mAuth.getCurrentUser() != null) {
            userData.setFirstName(binding.firstName.getText().toString());
            userData.setLastName(binding.lastName.getText().toString());
            userData.setBirthday(binding.birthday.getText().toString());

            mDatabase.getReference().child("users").child(mAuth.getUid()).setValue(userData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    Toast.makeText(this, "Failed saving changes", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void onClickBirthday(View view) {
        new DatePickerDialog(this, mDateListener, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return (super.onOptionsItemSelected(item));
    }
}
