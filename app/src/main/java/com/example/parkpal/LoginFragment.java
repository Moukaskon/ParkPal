package com.example.parkpal;

import android.content.Context; // For SharedPreferences
import android.content.SharedPreferences; // For SharedPreferences
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log; // For logging
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

// Removed java.io.IOException and java.util.HashMap as they are not directly used here
// OkHttp related imports are in LoginRequest.java

public class LoginFragment extends Fragment {
    // Use 10.0.2.2 for emulator to connect to host's localhost
    String url = "http://10.0.2.2/ParkPall/"; // Corrected for emulator assuming ParkPall is in htdocs root
    private static final String TAG = "LoginFragment";

    // SharedPreferences keys (can also be defined in MainActivity or a Constants file)
    public static final String PREFS_NAME = "MyPrefs";
    public static final String PREF_KEY_USER_ID = "user_id";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_IS_ADMIN = "is_admin";
    public static final String PREF_KEY_IS_LOGGED_IN = "is_logged_in";


    public LoginFragment() {
        // StrictMode policy - consider removing/refactoring for async operations
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnGuest = view.findViewById(R.id.btnGuest);
        Button btnRegister = view.findViewById(R.id.btnRegister); // Assuming you have a RegisterFragment

        EditText inUsername = view.findViewById(R.id.inTxtUsername); // Defined here for clarity
        EditText inPassword = view.findViewById(R.id.inTxtPassword); // Defined here for clarity

        btnRegister.setOnClickListener(v1 -> {
            // Navigate to RegisterFragment
            if (getActivity() instanceof MainActivity) {
                // ((MainActivity) getActivity()).loadFragment(new RegisterFragment()); // Example
                Toast.makeText(getActivity(), "Register functionality to be implemented.", Toast.LENGTH_SHORT).show();
            }
            // Your existing RegisterRequest call was for testing, actual navigation is better.
            // RegisterRequest registerUser = new RegisterRequest();
            // String result = registerUser.register(url, "myUsername", "myPassword");
        });

        btnLogin.setOnClickListener(v -> {
            String username = inUsername.getText().toString().trim();
            String password = inPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Username and password cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            // WARNING: Synchronous network call. Should be Asynchronous.
            LoginRequest loginRequest = new LoginRequest();
            String result = loginRequest.login(username, password, url);
            Log.d(TAG, "Login API Response: " + result);

            try {
                JSONObject json = new JSONObject(result);
                String status = json.getString("status");

                if ("success".equals(status)) {
                    // Extract user details from JSON response
                    int userId = json.getInt("user_id");
                    String loggedInUsername = json.getString("username");
                    boolean isAdmin = json.getBoolean("is_admin");
                    String message = json.getString("message");

                    Log.d(TAG, message + " | UserID: " + userId + " | Username: " + loggedInUsername + " | IsAdmin: " + isAdmin);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                    // Save details to SharedPreferences
                    if (getActivity() != null) {
                        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(PREF_KEY_IS_LOGGED_IN, true);
                        editor.putInt(PREF_KEY_USER_ID, userId);
                        editor.putString(PREF_KEY_USERNAME, loggedInUsername);
                        editor.putBoolean(PREF_KEY_IS_ADMIN, isAdmin);
                        editor.apply(); // Use apply() for asynchronous save

                        // Navigate to HomeFragment
                        ((MainActivity) getActivity()).loadFragment(new HomeFragment());
                    }
                } else {
                    String message = json.getString("message");
                    Toast.makeText(getActivity(), "Login Failed: " + message, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Login Failed: " + message);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing login response or request failed", e);
                Toast.makeText(getActivity(), "Error during login. Check connection.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        btnGuest.setOnClickListener(v2 -> {
            // For guest, clear any existing login state
            if (getActivity() != null) {
                SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PREF_KEY_IS_LOGGED_IN, false); // Mark as not logged in (or as guest)
                editor.remove(PREF_KEY_USER_ID);
                editor.remove(PREF_KEY_USERNAME);
                editor.remove(PREF_KEY_IS_ADMIN);
                editor.apply();

                // Pass a flag or use a different constructor for HomeFragment if guest mode has specific UI
                ((MainActivity) getActivity()).loadFragment(new HomeFragment()); // Or new HomeFragment(true) for guest
            }
        });

        return view;
    }
}