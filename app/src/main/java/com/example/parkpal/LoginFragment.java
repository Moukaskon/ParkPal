package com.example.parkpal;

import android.os.Bundle;
import android.os.StrictMode;
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
    String url = "http://172.29.32.1/Android/";
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
            RegisterRequest registerUser = new RegisterRequest();
            EditText username = view.findViewById(R.id.inTxtUsername);
            EditText password = view.findViewById(R.id.inTxtPassword);
            String result = registerUser.register(username.getText().toString(), password.getText().toString(), url);

            try {
                JSONObject json = new JSONObject(result);
                String status = json.getString("status");
                String message = json.getString("message");

                System.out.println("Register Status: " + status);
                System.out.println("Message: " + message);

                if (status.equals("success")) {
                    System.out.println(message);
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loadFragment(new HomeFragment());
                    }
                    Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });

        btnLogin.setOnClickListener(v -> {
            LoginRequest loginRequest = new LoginRequest();
            EditText username = view.findViewById(R.id.inTxtUsername);
            EditText password = view.findViewById(R.id.inTxtPassword);

            String result = loginRequest.login(username.getText().toString(), password.getText().toString(), url);
            try {
                JSONObject json = new JSONObject(result);
                String status = json.getString("status");
                String message = json.getString("message");

                if (status.equals("success")) {
                    boolean isAdmin = json.getBoolean("is_admin");
                    System.out.println(message + " " + isAdmin);
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loadFragment(new HomeFragment());
                    }
                } else {
                    Toast.makeText(getActivity(), "Wrong Credentials!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnGuest.setOnClickListener(v2 -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new HomeFragment());
            }
        });

        return view;
    }
}
