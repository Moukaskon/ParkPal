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

public class AdminAddSpotFragment extends Fragment {

    String url = Constants.BASE_URL;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_add_spot, container, false);
        Button btnSave = view.findViewById(R.id.btnSaveChanges);
        Button btnBack = view.findViewById(R.id.btnBack);


        btnSave.setOnClickListener(v -> {
            EditText id = view.findViewById(R.id.inTextSpot);
            EditText cost = view.findViewById(R.id.inTextCost);
            AdminRequest adminReq = new AdminRequest();
            new Thread(() -> {
                String result = adminReq.addParkingSpot(id.getText().toString(), Double.parseDouble(cost.getText().toString()), url);
                try {
                    JSONObject json = new JSONObject(result);
                    String status = json.getString("status");
                    String message = json.getString("message");

                    if (status.equals("success")) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Success! Spot added!", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new AdminFragment());
            }
        });


        return view;
    }
}
