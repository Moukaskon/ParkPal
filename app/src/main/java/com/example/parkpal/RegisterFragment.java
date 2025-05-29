package com.example.parkpal;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterFragment extends Fragment {
    String url = Constants.BASE_URL;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        Button btnSubmit = view.findViewById(R.id.btnSubmitReg);
        Button btnBack = view.findViewById(R.id.btnBackReg);

        btnSubmit.setOnClickListener(v ->{
            RegisterRequest registerUser = new RegisterRequest();
            EditText username = view.findViewById(R.id.inTextUsernameReg);
            EditText email = view.findViewById(R.id.inTextEmailReg);
            EditText password = view.findViewById(R.id.inTextPasswordReg);
            EditText passwoedRetype = view.findViewById(R.id.inTextRetypePassReg);

            String usernameText = username.getText().toString().trim();
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();
            String retypePasswordText = passwoedRetype.getText().toString().trim();

            if (usernameText.isEmpty() ||
                    emailText.isEmpty() ||
                    passwordText.isEmpty() ||
                    retypePasswordText.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (passwordText.length() < 8 || !passwordText.matches(".*[A-Z].*") ||
                    !passwordText.matches(".*[a-z].*") || !passwordText.matches(".*\\d.*")) {
                Toast.makeText(getActivity(), "Password must be 8+ characters, include upper/lowercase and a number.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(getActivity(), "Invalid email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!usernameText.matches("^[a-zA-Z0-9_]{3,15}$")) {
                Toast.makeText(getActivity(), "Username must be 3–15 characters, letters, digits, or underscores.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if(passwordText.equals(retypePasswordText)) {
                String result = registerUser.register(usernameText,
                       passwordText, emailText, url);

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
                } catch (
                        JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(getActivity(), "Passwords don't match!", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new LoginFragment());
            }
        });

        return view;
    }

}
