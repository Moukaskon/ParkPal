package com.example.parkpal;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
// No FormBody or POST request from here anymore for starting parking
import okhttp3.Response;

public class NewParkFragment extends Fragment {

    private static final String TAG = "NewParkFragment";
    private static final String ARG_USERNAME = "username"; // Can be "guest"
    private static final String ARG_SPOT_ID = "spot_id";
    private static final String ARG_IS_GUEST_SESSION = "is_guest_session"; // New argument

    // ... (API URLs remain the same)
    private static final String API_GET_USER_EMAIL_URL = "http://10.0.2.2/ParkPall/get_user_email.php";
    private static final String API_GET_SPOT_DETAILS_URL = "http://10.0.2.2/ParkPall/get_spot_details.php";


    private String currentUsername; // Will be "guest" for guest mode
    private String currentSpotId;
    private double currentSpotCost = -1.0;
    private boolean isGuestSession = false; // Flag for guest session

    private TextView tvParkSpotId;
    private TextView tvParkUserEmail;
    private TextView tvParkSpotCost;
    private EditText etParkLicensePlate;
    private EditText etParkDuration;
    private Button btnProceedToPayment;
    private TextView labelUserEmail; // TextView label for "Your Email:"
    // You might want to group email TextView and its label in a LinearLayout for easier hiding
    private LinearLayout layoutUserEmailSection; // Assuming you wrap email views in this

    public NewParkFragment() { /* ... */ }

    // Factory method for registered users
    public static NewParkFragment newInstance(String username, String spotId) {
        NewParkFragment fragment = new NewParkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_SPOT_ID, spotId);
        args.putBoolean(ARG_IS_GUEST_SESSION, false); // Not a guest session
        fragment.setArguments(args);
        return fragment;
    }

    // New factory method for GUESTS
    public static NewParkFragment newInstanceForGuest(String spotId) {
        NewParkFragment fragment = new NewParkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, "guest"); // Set username to "guest"
        args.putString(ARG_SPOT_ID, spotId);
        args.putBoolean(ARG_IS_GUEST_SESSION, true); // This is a guest session
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUsername = getArguments().getString(ARG_USERNAME); // Will be "guest" for guests
            currentSpotId = getArguments().getString(ARG_SPOT_ID);
            isGuestSession = getArguments().getBoolean(ARG_IS_GUEST_SESSION, false);
        }
    }

    // ... (onCreateView remains the same) ...
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_park, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvParkSpotId = view.findViewById(R.id.tvParkSpotId);
        tvParkUserEmail = view.findViewById(R.id.tvParkUserEmail);
        tvParkSpotCost = view.findViewById(R.id.tvParkSpotCost);
        etParkLicensePlate = view.findViewById(R.id.etParkLicensePlate);
        etParkDuration = view.findViewById(R.id.etParkDuration);
        btnProceedToPayment = view.findViewById(R.id.btnConfirmParking);
        btnProceedToPayment.setText("Proceed to Payment");

        // Assuming you have these IDs in your XML for the email label and its container
        labelUserEmail = view.findViewById(R.id.labelParkUserEmail); // e.g., <TextView android:id="@+id/labelParkUserEmail" ... text="Your Email:"/>
        layoutUserEmailSection = view.findViewById(R.id.layoutUserEmailSection); // e.g., <LinearLayout android:id="@+id/layoutUserEmailSection" ...> containing label and tvParkUserEmail


        if (currentSpotId != null) {
            tvParkSpotId.setText(currentSpotId);
            fetchSpotDetails(currentSpotId);
        } else {
            tvParkSpotCost.setText("Spot ID missing");
        }

        if (isGuestSession) {
            tvParkUserEmail.setVisibility(View.GONE);
            if (labelUserEmail != null) labelUserEmail.setVisibility(View.GONE);
            if (layoutUserEmailSection!=null) layoutUserEmailSection.setVisibility(View.GONE);
            // Optionally, set a placeholder or hide the email field completely
            // currentUsername is already "guest"
        } else if (currentUsername != null) {
            fetchUserEmail(currentUsername);
            tvParkUserEmail.setVisibility(View.VISIBLE);
            if (labelUserEmail != null) labelUserEmail.setVisibility(View.VISIBLE);
            if (layoutUserEmailSection!=null) layoutUserEmailSection.setVisibility(View.VISIBLE);
        } else {
            tvParkUserEmail.setText("N/A (User not identified)");
        }

        btnProceedToPayment.setOnClickListener(v -> collectDataAndProceed());
    }
    private void fetchUserEmail(String username) {
        // ... (fetchUserEmail method remains the same as previous answer)
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(API_GET_USER_EMAIL_URL).newBuilder();
        urlBuilder.addQueryParameter("username", username);
        Request request = new Request.Builder().url(urlBuilder.build()).build();

        Log.d(TAG, "Fetching email for: " + username + " from " + request.url());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch email: ", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> tvParkUserEmail.setText("Error loading email"));
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() == null) return;
                final String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Get Email Response: " + responseBody);
                getActivity().runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            tvParkUserEmail.setText("Error: " + response.message());
                            return;
                        }
                        JSONObject jsonObject = new JSONObject(responseBody);
                        if (jsonObject.getString("status").equals("success")) {
                            tvParkUserEmail.setText(jsonObject.getString("email"));
                        } else {
                            tvParkUserEmail.setText(jsonObject.optString("message", "Email not found"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error for email: ", e);
                        tvParkUserEmail.setText("Error parsing email data");
                    }
                });
            }
        });
    }

    private void fetchSpotDetails(String spotId) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(API_GET_SPOT_DETAILS_URL).newBuilder();
        urlBuilder.addQueryParameter("spot_id", spotId);
        Request request = new Request.Builder().url(urlBuilder.build()).build();

        Log.d(TAG, "Fetching details for spot: " + spotId + " from " + request.url());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch spot details: ", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvParkSpotCost.setText("Error loading cost");
                        Toast.makeText(getContext(), "Could not load spot details.", Toast.LENGTH_SHORT).show();
                        btnProceedToPayment.setEnabled(false); // Disable if details fail
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() == null) return;
                final String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Get Spot Details Response: " + responseBody);
                getActivity().runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()) {
                            tvParkSpotCost.setText("Error: " + response.message());
                            btnProceedToPayment.setEnabled(false);
                            return;
                        }
                        JSONObject jsonObject = new JSONObject(responseBody);
                        if (jsonObject.getString("status").equals("success")) {
                            currentSpotCost = jsonObject.getDouble("cost");
                            tvParkSpotCost.setText(String.format(Locale.US, "$%.2f", currentSpotCost));
                            if (jsonObject.getInt("availability") != 1) {
                                Toast.makeText(getContext(), "Spot is no longer available!", Toast.LENGTH_LONG).show();
                                btnProceedToPayment.setEnabled(false); // Disable if not available
                            } else {
                                btnProceedToPayment.setEnabled(true);
                            }
                        } else {
                            String message = jsonObject.optString("message", "Spot details not found.");
                            tvParkSpotCost.setText(message);
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                            btnProceedToPayment.setEnabled(false);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error for spot details: ", e);
                        tvParkSpotCost.setText("Error parsing cost");
                        btnProceedToPayment.setEnabled(false);
                    }
                });
            }
        });
    }

    private void collectDataAndProceed() {
        // ... (validation remains the same) ...
        String licensePlate = etParkLicensePlate.getText().toString().trim();
        String durationStr = etParkDuration.getText().toString().trim();

        if (TextUtils.isEmpty(licensePlate)) { /*...*/ return; }
        if (TextUtils.isEmpty(durationStr)) { /*...*/ return; }
        int durationMinutes;
        try { durationMinutes = Integer.parseInt(durationStr); if (durationMinutes <= 0) { /*...*/ return; }
        } catch (NumberFormatException e) { /*...*/ return; }

        if (currentSpotId == null || currentSpotCost < 0) { /*...*/ return; }
        // currentUsername will be "guest" for guests

        Log.d(TAG, "Proceeding to payment. User/Mode: " + currentUsername +
                ", Spot: " + currentSpotId + ", Cost: " + currentSpotCost +
                ", Plate: " + licensePlate + ", Duration: " + durationMinutes);

        if (getActivity() instanceof MainActivity) {
            if (isGuestSession) {
                ((MainActivity) getActivity()).loadFragment(
                        EndParkingFragment.newInstanceForGuestStartingSession(
                                currentSpotId,
                                licensePlate,
                                durationMinutes,
                                currentSpotCost
                        )
                );
            } else { // Registered user
                if (currentUsername == null) { // Should not happen if not guest
                    Toast.makeText(getContext(), "User information missing.", Toast.LENGTH_SHORT).show(); return;
                }
                ((MainActivity) getActivity()).loadFragment(
                        EndParkingFragment.newInstanceForStartingSession(
                                currentUsername,
                                currentSpotId,
                                licensePlate,
                                durationMinutes,
                                currentSpotCost
                        )
                );
            }
        }
    }
}