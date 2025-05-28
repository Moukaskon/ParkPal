package com.example.parkpal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List; // Keep this if ParkingSpot is an inner class or in same package
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final String API_GET_SPOTS_URL = "http://10.0.2.2/ParkPall/getSpots.php";

    private Spinner spinnerParkingSpots;
    private Button btnProceedToPark;
    private TextView tvSelectedSpotDetails;

    private ArrayList<String> spotDisplayList;
    private ArrayList<ParkingSpot> availableSpotsList;
    private ArrayAdapter<String> spinnerAdapter;

    private ParkingSpot selectedParkingSpot = null;

    private boolean isCurrentUserGuest = false;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Determine if this is a guest flow
        if (getActivity() instanceof MainActivity) {
            isCurrentUserGuest = ((MainActivity) getActivity()).isGuestMode();
        } else if (getContext() != null) { // Fallback
            SharedPreferences prefs = getContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
            isCurrentUserGuest = prefs.getBoolean(MainActivity.PREF_KEY_IS_GUEST_MODE, false);
        }
        Log.d(TAG, "SearchFragment created. isCurrentUserGuest: " + isCurrentUserGuest);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // ... (initialization of views remains the same) ...

        spinnerParkingSpots = view.findViewById(R.id.spinnerParkingSpots);
        btnProceedToPark = view.findViewById(R.id.btnProceedToPark);
        tvSelectedSpotDetails = view.findViewById(R.id.tvSelectedSpotDetails);

        spotDisplayList = new ArrayList<>();
        availableSpotsList = new ArrayList<>();
        spotDisplayList.add("Select an available spot...");

        if (getContext() == null) {
            Log.e(TAG, "Context is null in onViewCreated, cannot create adapter.");
            return;
        }
        spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spotDisplayList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParkingSpots.setAdapter(spinnerAdapter);

        btnProceedToPark.setEnabled(false);
        tvSelectedSpotDetails.setText("Please select a spot from the dropdown.");

        spinnerParkingSpots.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // ... (onItemSelected and onNothingSelected remain the same) ...
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= availableSpotsList.size()) { // Check bounds
                    selectedParkingSpot = availableSpotsList.get(position - 1);
                    String details = String.format(Locale.US, "ID: %s\nCost: $%.2f\nAvailability: %s",
                            selectedParkingSpot.getId(),
                            selectedParkingSpot.getCost(),
                            selectedParkingSpot.isAvailable() ? "Available" : "Not Available");
                    tvSelectedSpotDetails.setText(details);
                    btnProceedToPark.setEnabled(true);
                } else {
                    selectedParkingSpot = null;
                    btnProceedToPark.setEnabled(false);
                    tvSelectedSpotDetails.setText("Please select a spot from the dropdown.");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedParkingSpot = null;
                btnProceedToPark.setEnabled(false);
                tvSelectedSpotDetails.setText("Please select a spot from the dropdown.");
            }
        });


        btnProceedToPark.setOnClickListener(v -> {
            if (selectedParkingSpot != null) {
                if (getActivity() instanceof MainActivity) {
                    if (isCurrentUserGuest) {
                        Log.d(TAG, "Proceeding to park as GUEST. Spot: " + selectedParkingSpot.getId());
                        ((MainActivity) getActivity()).loadFragment(
                                NewParkFragment.newInstanceForGuest(selectedParkingSpot.getId()) // New factory method
                        );
                    } else {
                        String currentUsername = ((MainActivity) getActivity()).getCurrentUsername();
                        if (currentUsername != null) {
                            Log.d(TAG, "Proceeding to park. User: " + currentUsername + ", Spot: " + selectedParkingSpot.getId());
                            ((MainActivity) getActivity()).loadFragment(
                                    NewParkFragment.newInstance(currentUsername, selectedParkingSpot.getId())
                            );
                        } else {
                            // Should not happen if isCurrentUserGuest is false, but as a fallback
                            Toast.makeText(getContext(), "Login session error. Please log in again.", Toast.LENGTH_SHORT).show();
                            ((MainActivity) getActivity()).loadFragment(new LoginFragment());
                        }
                    }
                }
            } else {
                Toast.makeText(getContext(), "Please select a parking spot first.", Toast.LENGTH_SHORT).show();
            }
        });

        fetchAvailableSpots();
    }

    // ... (fetchAvailableSpots and ParkingSpot POJO remain the same) ...
    private void fetchAvailableSpots() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_GET_SPOTS_URL)
                .build();

        Log.d(TAG, "Fetching spots from: " + API_GET_SPOTS_URL);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch spots: ", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error fetching parking spots: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() == null) return;

                final String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Spots API Response: " + responseBody);

                getActivity().runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            Toast.makeText(getContext(), "Failed to load spots: " + response.message(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "API request not successful: " + response.code() + " " + response.message());
                            return;
                        }

                        JSONArray jsonArray = new JSONArray(responseBody);
                        spotDisplayList.clear();
                        availableSpotsList.clear();
                        spotDisplayList.add("Select an available spot...");

                        if (jsonArray.length() == 0) {
                            spotDisplayList.set(0, "No available spots found.");
                            if(getContext() != null) Toast.makeText(getContext(), "No available parking spots found.", Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject spotObject = jsonArray.getJSONObject(i);
                                String id = spotObject.getString("id");
                                double cost = spotObject.getDouble("cost");
                                boolean availability = spotObject.getInt("availability") == 1;

                                ParkingSpot spot = new ParkingSpot(id, cost, availability);
                                availableSpotsList.add(spot);
                                spotDisplayList.add(String.format(Locale.US, "%s - $%.2f", id, cost));
                            }
                        }
                        if (spinnerAdapter != null) spinnerAdapter.notifyDataSetChanged();
                        if (spinnerParkingSpots != null) spinnerParkingSpots.setSelection(0);
                        if (btnProceedToPark != null) btnProceedToPark.setEnabled(false);
                        if (tvSelectedSpotDetails != null) tvSelectedSpotDetails.setText("Please select a spot from the dropdown.");

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: ", e);
                        if(getContext() != null) Toast.makeText(getContext(), "Error parsing spot data.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) { // Catch any other unexpected errors
                        Log.e(TAG, "Unexpected error during spot processing: ", e);
                        if(getContext() != null) Toast.makeText(getContext(), "An unexpected error occurred.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private static class ParkingSpot {
        private String id;
        private double cost;
        private boolean available;
        public ParkingSpot(String id, double cost, boolean available) {
            this.id = id; this.cost = cost; this.available = available;
        }
        public String getId() { return id; }
        public double getCost() { return cost; }
        public boolean isAvailable() { return available; }
        @NonNull @Override public String toString() { return id; }
    }
}
