package com.project.safebite.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.safebite.R;

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

                if (item.getItemId() == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                }
                else if (item.getItemId() == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment();
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