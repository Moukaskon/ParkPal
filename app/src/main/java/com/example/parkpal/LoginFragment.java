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

public class LoginFragment extends Fragment {
    String url = "http://10.0.2.2/ParkPall/";
    private static final String TAG = "LoginFragment";

    public static final String PREFS_NAME = "MyPrefs";
    public static final String PREF_KEY_USER_ID = "user_id";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_IS_ADMIN = "is_admin";
    public static final String PREF_KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_KEY_IS_GUEST_MODE = "is_guest_mode"; // New key for guest mode


    public LoginFragment() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnGuest = view.findViewById(R.id.btnGuest);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        EditText inUsername = view.findViewById(R.id.inTxtUsername);
        EditText inPassword = view.findViewById(R.id.inTxtPassword);

        btnRegister.setOnClickListener(v1 -> {
            if (getActivity() instanceof MainActivity) {
                Toast.makeText(getActivity(), "Register functionality to be implemented.", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            String usernameInput = inUsername.getText().toString().trim(); // Renamed to avoid conflict
            String passwordInput = inPassword.getText().toString().trim(); // Renamed

            if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(getActivity(), "Username and password cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest loginRequest = new LoginRequest();
            String result = loginRequest.login(usernameInput, passwordInput, url);
            Log.d(TAG, "Login API Response: " + result);

            try {
                JSONObject json = new JSONObject(result);
                String status = json.getString("status");

                if ("success".equals(status)) {
                    int userId = json.getInt("user_id");
                    String loggedInUsername = json.getString("username");
                    boolean isAdmin = json.getBoolean("is_admin");
                    String message = json.getString("message");

                    Log.d(TAG, message + " | UserID: " + userId + " | Username: " + loggedInUsername + " | IsAdmin: " + isAdmin);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                    if (getActivity() != null) {
                        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(PREF_KEY_IS_LOGGED_IN, true);
                        editor.putInt(PREF_KEY_USER_ID, userId);
                        editor.putString(PREF_KEY_USERNAME, loggedInUsername);
                        editor.putBoolean(PREF_KEY_IS_ADMIN, isAdmin);
                        editor.putBoolean(PREF_KEY_IS_GUEST_MODE, false); // Explicitly not guest
                        editor.apply();

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
            if (getActivity() != null) {
                SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PREF_KEY_IS_LOGGED_IN, false); // Not a registered user login
                editor.remove(PREF_KEY_USER_ID);
                editor.putString(PREF_KEY_USERNAME, "guest"); // Store "guest" as username
                editor.remove(PREF_KEY_IS_ADMIN);
                editor.putBoolean(PREF_KEY_IS_GUEST_MODE, true); // Set guest mode to true
                editor.apply();

                Log.d(TAG, "Proceeding as Guest.");
                ((MainActivity) getActivity()).loadFragment(new HomeFragment());
            }
        });

        return view;
    }
}