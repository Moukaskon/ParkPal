package com.example.parkpal;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EndParkingFragment extends Fragment {

    private static final String TAG = "EndParkingFragment";
    // Make sure your server URL is correct (use your PC's IP, not localhost, if testing on a real device/emulator)
    // For emulator localhost is 10.0.2.2
    private static final String API_URL = "http://10.0.2.2/ParkPall/process_payment.php";


    private String username;
    private boolean isGuest = false;
    private String spotId; // The ID of the parking spot, e.g., "SPOT1"

    private EditText inCard;
    private EditText inCVV;
    private Button btnEnd;

    public EndParkingFragment() {
        // Required empty public constructor
    }

    // Constructor for logged-in user
    public EndParkingFragment(String username, String spotId) {
        this.username = username;
        this.isGuest = false;
        this.spotId = spotId;
    }

    // Constructor for guest user (spotId might not be relevant here or could be handled differently)
    public EndParkingFragment(boolean isGuest) {
        this.isGuest = isGuest;
        this.username = "guest"; // Or some other placeholder
        this.spotId = "N/A_GUEST";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_end_parking, container, false);

        inCard = view.findViewById(R.id.inCardNumber);
        inCVV = view.findViewById(R.id.inCardCVV);
        btnEnd = view.findViewById(R.id.btnEndParking);

        if (isGuest) {
            inCard.setVisibility(View.VISIBLE);
            inCVV.setVisibility(View.VISIBLE);
        } else {
            inCard.setVisibility(View.GONE);
            inCVV.setVisibility(View.GONE);
        }

        btnEnd.setOnClickListener(v -> {
            if (isGuest) {
                handleGuestPayment();
            } else {
                if (username == null || spotId == null) {
                    Toast.makeText(getActivity(), "User data or Spot ID missing", Toast.LENGTH_SHORT).show();
                    return;
                }
                checkAndProcessUserPayment(username, spotId);
            }
        });

        return view;
    }

    private void handleGuestPayment() {
        String card = inCard.getText().toString().trim();
        String cvv = inCVV.getText().toString().trim();
        if (card.length() != 16 || !TextUtils.isDigitsOnly(card)) {
            Toast.makeText(getActivity(), "Card must be 16 digits", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cvv.length() != 3 || !TextUtils.isDigitsOnly(cvv)) {
            Toast.makeText(getActivity(), "CVV must be 3 digits", Toast.LENGTH_SHORT).show();
            return;
        }
        // Simulate guest payment success
        Toast.makeText(getActivity(), "Payment successful (guest)", Toast.LENGTH_SHORT).show();
        // Potentially navigate away or update UI
        if (getActivity() instanceof MainActivity) {
            // ((MainActivity) getActivity()).loadFragment(new HomeFragment()); // Example
        }
    }

    private void checkAndProcessUserPayment(String currentUsername, String currentSpotId) {
        Log.d(TAG, "Processing payment for user: " + currentUsername + " at spot: " + currentSpotId);
        Toast.makeText(getActivity(), "Processing payment...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("username", currentUsername)
                .add("spot_id", currentSpotId) // Send the string ID like "SPOT1"
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OkHttp onFailure: ", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Payment failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() == null) return; // Fragment not attached

                final String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "OkHttp onResponse: " + responseBody);

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if ("success".equals(status)) {
                            double newBalance = jsonObject.optDouble("new_balance", -1); // Get new balance
                            String successMsg = message;
                            if (newBalance != -1) {
                                successMsg += "\nNew Balance: $" + String.format("%.2f", newBalance);
                            }
                            Toast.makeText(getActivity(), successMsg, Toast.LENGTH_LONG).show();
                            // TODO: Navigate back or to a confirmation screen
                            // e.g., if (getActivity() instanceof MainActivity) {
                            //    ((MainActivity) getActivity()).loadFragment(new HomeFragment());
                            // }
                        } else {
                            Toast.makeText(getActivity(), "Payment Error: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: ", e);
                        Toast.makeText(getActivity(), "Error parsing server response.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}