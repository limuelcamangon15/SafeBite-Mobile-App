package com.project.safebite.ui.activity;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.safebite.R;
import com.project.safebite.model.NetworkViewModel;
import com.project.safebite.ui.fragment.HistoryFragment;
import com.project.safebite.ui.fragment.HomeFragment;
import com.project.safebite.ui.fragment.ProfileFragment;
import com.project.safebite.ui.fragment.SavedFragment;
import com.project.safebite.ui.fragment.ScanFragment;
import com.project.safebite.utils.FuncUtil;


public class MainActivity extends AppCompatActivity {

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    Context context = MainActivity.this;
    BottomNavigationView navBottom;
    LinearLayout llOffline;
    NetworkViewModel networkViewModel;

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
        llOffline = findViewById(R.id.llOffline);
        networkViewModel = new ViewModelProvider(this).get(NetworkViewModel.class);
        networkViewModel.setConnected(FuncUtil.isConnected(context));
        llOffline.setVisibility(!FuncUtil.isConnected(context) ? View.VISIBLE : View.GONE);
        observeNetwork();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(connectivityManager != null && networkCallback != null){
            connectivityManager.unregisterNetworkCallback(networkCallback);
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

    private void observeNetwork() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {

                runOnUiThread(() -> {
                    llOffline.setVisibility(View.GONE);
                    networkViewModel.setConnected(true);
                });
            }

            @Override
            public void onLost(Network network) {

                runOnUiThread(() -> {
                    llOffline.setVisibility(View.VISIBLE);
                    networkViewModel.setConnected(false);
                });
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

}