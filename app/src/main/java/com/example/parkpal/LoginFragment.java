package com.example.parkpal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginFragment extends Fragment {
    String url = Constants.BASE_URL;

    private static final String TAG = "LoginFragment";

    public static final String PREFS_NAME = "MyPrefs";
    public static final String PREF_KEY_USER_ID = "user_id";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_IS_ADMIN = "is_admin";
    public static final String PREF_KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_KEY_IS_GUEST_MODE = "is_guest_mode"; // New key for guest mode
    public LoginFragment(){
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

        btnRegister.setOnClickListener(v1 -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new RegisterFragment());
            }
        });

        btnLogin.setOnClickListener(v -> {
            LoginRequest loginRequest = new LoginRequest();
            EditText username = view.findViewById(R.id.inTxtUsername);
            EditText password = view.findViewById(R.id.inTxtPassword);

            String result = loginRequest.login(username.getText().toString(), password.getText().toString(), url);
            try {
                JSONObject json = null;
                try {
                    json = new JSONObject(result);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String status = json.getString("status");
                String message = json.getString("message");

                if (status.equals("success")) {
                    boolean isAdmin = json.getBoolean("is_admin");
                    System.out.println(message + " " + isAdmin);

                    int userId = json.getInt("user_id");
                    String loggedInUsername = json.getString("username");

                    Log.d(TAG, message + " | UserID: " + userId + " | Username: " + loggedInUsername + " | IsAdmin: " + isAdmin);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                    if(isAdmin) {
                        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(PREF_KEY_IS_LOGGED_IN, true);
                        editor.putInt(PREF_KEY_USER_ID, userId);
                        editor.putString(PREF_KEY_USERNAME, loggedInUsername);
                        editor.putBoolean(PREF_KEY_IS_ADMIN, isAdmin);
                        editor.putBoolean(PREF_KEY_IS_GUEST_MODE, false); // Explicitly not guest
                        editor.apply();

                        ((MainActivity) getActivity()).loadFragment(new AdminFragment());
                    } else {
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
                    }


                } else {
                    Toast.makeText(getActivity(), "Wrong Credentials!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }}
        );

        btnGuest.setOnClickListener(v2 -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new HomeFragment());
            }
        });

        return view;
    }
}