package com.project.safebite.ui.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.safebite.R;
import com.project.safebite.ui.fragment.HistoryFragment;
import com.project.safebite.ui.fragment.HomeFragment;
import com.project.safebite.ui.fragment.ProfileFragment;
import com.project.safebite.ui.fragment.SavedFragment;
import com.project.safebite.ui.fragment.ScanFragment;

public class MainActivity extends AppCompatActivity {

    Context context = MainActivity.this;
    BottomNavigationView navBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        navBottom = findViewById(R.id.navBottom);

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());

            navBottom.setOnItemSelectedListener((item) -> {

                Fragment selectedFragment = null;
                int currentFragmentSelectedId = item.getItemId();

                if (currentFragmentSelectedId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                }
                else if (currentFragmentSelectedId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment();
                }
                else if (currentFragmentSelectedId == R.id.navigation_scan){
                    selectedFragment = new ScanFragment();
                }
                else if(currentFragmentSelectedId == R.id.navigation_history){
                    selectedFragment = new HistoryFragment();
                }
                else if(currentFragmentSelectedId == R.id.navigation_saved){
                    selectedFragment = new SavedFragment();
                }

                return loadFragment(selectedFragment);
            });
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }

}