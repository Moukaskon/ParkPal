package com.example.parkpal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private TextView txtWelcomeMessage;
    private Button btnLogout; // Declare the logout button

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        txtWelcomeMessage = view.findViewById(R.id.txtWelcome);
        btnLogout = view.findViewById(R.id.btnLogout); // Initialize the logout button

        // Retrieve the logged-in username to personalize the welcome message
        String loggedInUsername = null;
        boolean userIsLoggedIn = false; // Flag to check login status

        if (getActivity() instanceof MainActivity) {
            loggedInUsername = ((MainActivity) getActivity()).getCurrentUsername();
            // We can also get a more direct login status if MainActivity provides it
            // For now, relying on username being non-null implies logged in.
            if (loggedInUsername != null) {
                userIsLoggedIn = true;
            }
        } else if (getContext() != null) { // Fallback
            SharedPreferences prefs = getContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
            if (prefs.getBoolean(MainActivity.PREF_KEY_IS_LOGGED_IN, false)) {
                loggedInUsername = prefs.getString(MainActivity.PREF_KEY_USERNAME, null);
                userIsLoggedIn = true;
            }
        }

        // Set welcome message
        if (txtWelcomeMessage != null) {
            if (userIsLoggedIn && loggedInUsername != null) {
                txtWelcomeMessage.setText("Welcome back, " + loggedInUsername + "!");
            } else {
                txtWelcomeMessage.setText("Welcome to ParkPal!");
            }
        }

        // Configure Logout Button
        if (btnLogout != null) {
            if (userIsLoggedIn) {
                btnLogout.setVisibility(View.VISIBLE); // Show logout button only if logged in
                btnLogout.setOnClickListener(v -> {
                    performLogout();
                });
            } else {
                btnLogout.setVisibility(View.GONE); // Hide if not logged in
            }
        }

        return view;
    }

    private void performLogout() {
        if (getContext() == null) {
            Log.e(TAG, "Context is null, cannot perform logout.");
            return;
        }

        // Clear SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(MainActivity.PREF_KEY_IS_LOGGED_IN);
        editor.remove(MainActivity.PREF_KEY_USER_ID);
        editor.remove(MainActivity.PREF_KEY_USERNAME);
        editor.remove(MainActivity.PREF_KEY_IS_ADMIN);
        editor.remove(MainActivity.PREF_KEY_IS_GUEST_MODE);
        // editor.clear(); // Alternative: Clears ALL SharedPreferences for this file
        editor.apply(); // Use apply() for asynchronous save

        Log.d(TAG, "User logged out. SharedPreferences cleared.");
        Toast.makeText(getContext(), "You have been logged out.", Toast.LENGTH_SHORT).show();

        // Navigate back to LoginFragment
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(new LoginFragment());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Any further UI updates after view creation can go here.
    }
}