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

    // SharedPreferences constants defined here for central access
    public static final String PREFS_NAME = "MyPrefs";
    public static final String PREF_KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_USER_ID = "user_id"; // If needed elsewhere
    public static final String PREF_KEY_IS_ADMIN = "is_admin"; // If needed elsewhere


    public void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    // .addToBackStack(null) // Optional: reconsider if backstack is needed for all transitions
                    .commit();
        }
    }

    /**
     * Retrieves the currently logged-in username from SharedPreferences.
     * @return The username if logged in, otherwise null.
     */
    public String getCurrentUsername() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(PREF_KEY_IS_LOGGED_IN, false)) {
            return prefs.getString(PREF_KEY_USERNAME, null);
        }
        return null;
    }

    /**
     * Retrieves the currently logged-in user ID from SharedPreferences.
     * @return The user ID if logged in, otherwise -1.
     */
    public int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(PREF_KEY_IS_LOGGED_IN, false)) {
            return prefs.getInt(PREF_KEY_USER_ID, -1);
        }
        return -1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) { // Load initial fragment only once
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean(PREF_KEY_IS_LOGGED_IN, false);

            if (isLoggedIn) {
                Log.d(TAG, "User already logged in. Loading HomeFragment.");
                loadFragment(new HomeFragment());
            } else {
                Log.d(TAG, "User not logged in. Loading LoginFragment.");
                loadFragment(new LoginFragment());
            }
        }

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                String currentUsername = getCurrentUsername(); // Get username for fragments that need it

                // Prevent navigation if not logged in for protected fragments
                if (currentUsername == null && (itemId == R.id.nav_history || itemId == R.id.nav_profile || itemId == R.id.nav_home /*if home is protected*/)) {
                    Toast.makeText(MainActivity.this, "Please log in to access this section.", Toast.LENGTH_SHORT).show();
                    loadFragment(new LoginFragment()); // Redirect to login
                    return false; // Indicate item selection was not handled for navigation
                }


                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_search) {
                    selectedFragment = new SearchFragment(); // Assuming SearchFragment doesn't require login
                } else if (itemId == R.id.nav_history) {
                    if (currentUsername != null) {
                        selectedFragment = UserHistoryFragment.newInstance(currentUsername);
                    } else {
                        // This case should ideally be caught by the check above
                        Toast.makeText(MainActivity.this, "Login required for History.", Toast.LENGTH_SHORT).show();
                        selectedFragment = new LoginFragment();
                    }
                } else if (itemId == R.id.nav_profile) {
                    // Assuming ProfileFragment also needs username or is for logged-in users
                    if (currentUsername != null) {
                        selectedFragment = new ProfileFragment(); // Adjust if ProfileFragment needs username
                    } else {
                        // This case should ideally be caught by the check above
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