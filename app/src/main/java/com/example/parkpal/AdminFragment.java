package com.example.parkpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminFragment extends Fragment {
	String url = Constants.BASE_URL;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin, container, false);
		Spinner spinner = view.findViewById(R.id.spinnerSpots);
		Button addButton = view.findViewById(R.id.btnAdd);
		Button modifyButton = view.findViewById(R.id.btnModify);
		Button deleteButton = view.findViewById(R.id.btnDelete);

		new Thread(() -> {
			AdminRequest request = new AdminRequest();
			String jsonResponse = request.fetchParkingSpots(url);

			try {
				JSONArray array = new JSONArray(jsonResponse);
				ArrayList<String> spotNames = new ArrayList<>();

				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					String id = obj.getString("id");
					double cost = obj.getDouble("cost");
					int available = obj.getInt("availability");

					String display = id;
					spotNames.add(display);
				}

				if(isAdded()) {
					requireActivity().runOnUiThread(() -> {
						ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
								android.R.layout.simple_spinner_item, spotNames);
						adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						spinner.setAdapter(adapter);
					});
				}


			} catch (JSONException e) {
				e.printStackTrace();
			}
		}).start();

		deleteButton.setOnClickListener(v -> {
			String selectedItem = spinner.getSelectedItem().toString();
			AdminRequest adminReq = new AdminRequest();
			new Thread(() -> {
				String result = adminReq.deleteParkingSpot(selectedItem, url);
				try {
					JSONObject json = new JSONObject(result);
					String status = json.getString("status");
					String message = json.getString("message");

					if (status.equals("success")) {
						String updatedSpotsJson = adminReq.fetchParkingSpots(url);
						JSONArray spotsArray = new JSONArray(updatedSpotsJson);

						// Extract new list of spot IDs
						ArrayList<String> updatedSpotsList = new ArrayList<>();
						for (int i = 0; i < spotsArray.length(); i++) {
							JSONObject spot = spotsArray.getJSONObject(i);
							updatedSpotsList.add(spot.getString("id"));
						}

						getActivity().runOnUiThread(() -> {
							// Update spinner adapter with new data
							ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, updatedSpotsList);
							adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							spinner.setAdapter(adapter);

							Toast.makeText(getActivity(), "Spot: " + selectedItem + " deleted successfully!", Toast.LENGTH_SHORT).show();
						});
					} else {
						getActivity().runOnUiThread(() -> {
							Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();

		});

		addButton.setOnClickListener(v -> {
			if (getActivity() instanceof MainActivity) {
				((MainActivity) getActivity()).loadFragment(new AdminAddSpotFragment());
			}
		});

		modifyButton.setOnClickListener(v -> {
			String selectedItem = spinner.getSelectedItem().toString();
			if (getActivity() instanceof MainActivity) {
				((MainActivity) getActivity()).loadFragment(new AdminChangesFragment(selectedItem));
			}
		});


		return view;
	}
}