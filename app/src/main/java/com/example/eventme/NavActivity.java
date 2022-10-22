package com.example.eventme;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.eventme.databinding.ActivityNavBinding;

public class NavActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNavBinding binding = ActivityNavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up Navigation
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.setGraph(R.navigation.main_nav_graph);

        // Set up bottom navigation bar
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }

    // Tie BottomNavigation MenuItems with Navigation destinations
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }
}
