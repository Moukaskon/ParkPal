package com.example.parkpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

public class AdminChangesFragment extends Fragment {
    String spotCode;
    String url = Constants.BASE_URL;
    public AdminChangesFragment(String code){
        spotCode = code;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_changes, container, false);
        Button btnSave = view.findViewById(R.id.btnSaveChanges);
        TextView txtSpot = view.findViewById(R.id.txtSelectedID);
        Button btnBack = view.findViewById(R.id.btnBackMod);

        txtSpot.setText(spotCode);

        btnSave.setOnClickListener(v ->{
            AdminRequest adminReq = new AdminRequest();
            EditText inCost = view.findViewById(R.id.inTextCostMod);
            new Thread(() -> {
                String response = adminReq.updateSpotCost(spotCode, Double.parseDouble(inCost.getText().toString()), url);
                try {
                    JSONObject json = new JSONObject(response);
                    String status = json.getString("status");
                    String message = json.getString("message");

                    if (status.equals("success")) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show()
                        );
                    } else {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Update failed: " + message, Toast.LENGTH_SHORT).show()
                        );
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
