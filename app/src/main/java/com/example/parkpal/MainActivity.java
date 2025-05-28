package com.example.parkpal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String PREFS_NAME = "MyPrefs";
    public static final String PREF_KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_USER_ID = "user_id";
    public static final String PREF_KEY_IS_ADMIN = "is_admin";
    public static final String PREF_KEY_IS_GUEST_MODE = "is_guest_mode"; // Added key


    public void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public String getCurrentUsername() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Return username if logged in OR if in guest mode (username will be "guest")
        if (prefs.getBoolean(PREF_KEY_IS_LOGGED_IN, false) || prefs.getBoolean(PREF_KEY_IS_GUEST_MODE, false)) {
            return prefs.getString(PREF_KEY_USERNAME, null);
        }
        return null;
    }

    public int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(PREF_KEY_IS_LOGGED_IN, false)) { // Only return valid ID if truly logged in
            return prefs.getInt(PREF_KEY_USER_ID, -1);
        }
        return -1; // Return -1 for guests or if not logged in
    }

    // New method to check guest mode
    public boolean isGuestMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_KEY_IS_GUEST_MODE, false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean(PREF_KEY_IS_LOGGED_IN, false);
            boolean isGuest = prefs.getBoolean(PREF_KEY_IS_GUEST_MODE, false);

            if (isLoggedIn || isGuest) { // If logged in OR in guest mode, go to Home
                Log.d(TAG, "User session active (logged in or guest). Loading HomeFragment.");
                loadFragment(new HomeFragment());
            } else {
                Log.d(TAG, "No active session. Loading LoginFragment.");
                loadFragment(new LoginFragment());
            }
        }

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                String currentUsername = getCurrentUsername();
                boolean isUserReallyLoggedIn = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .getBoolean(PREF_KEY_IS_LOGGED_IN, false);


                // For sections requiring actual login (not guest)
                if (!isUserReallyLoggedIn && (itemId == R.id.nav_history || itemId == R.id.nav_profile)) {
                    Toast.makeText(MainActivity.this, "Please log in to access this section.", Toast.LENGTH_SHORT).show();
                    loadFragment(new LoginFragment());
                    return false;
                }


                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_search) {
                    // Search is available for both logged-in users and guests
                    selectedFragment = new SearchFragment();
                } else if (itemId == R.id.nav_history) {
                    if (isUserReallyLoggedIn && currentUsername != null) { // History only for logged-in users
                        selectedFragment = UserHistoryFragment.newInstance(currentUsername);
                    } else {
                        Toast.makeText(MainActivity.this, "Login required for History.", Toast.LENGTH_SHORT).show();
                        selectedFragment = new LoginFragment();
                    }
                } else if (itemId == R.id.nav_profile) {
                    if (isUserReallyLoggedIn && currentUsername != null) { // Profile only for logged-in users
                        selectedFragment = new ProfileFragment();
                    } else {
                        Toast.makeText(MainActivity.this, "Login required for Profile.", Toast.LENGTH_SHORT).show();
                        selectedFragment = new LoginFragment();
                    }
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });
    }
}