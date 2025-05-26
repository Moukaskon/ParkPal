package com.example.parkpal;

import android.os.Bundle;
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

            if(passwoedRetype.getText().toString().equals(password.getText().toString())) {
                String result = registerUser.register(username.getText().toString(),
                        password.getText().toString(), email.getText().toString(), url);

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
