package com.example.parkpal;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // For displaying info
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout; // Import TextInputLayout


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EndParkingFragment extends Fragment {

    private static final String TAG = "EndParkingFragment";
    private static final String API_FINALIZE_SESSION_URL = "http://10.0.2.2/ParkPall/finalize_parking_session.php";
    private static final String API_END_EXISTING_SESSION_URL = "http://10.0.2.2/ParkPall/process_payment.php";

    private static final String ARG_MODE = "mode";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_SPOT_ID = "spot_id";
    private static final String ARG_LICENSE_PLATE = "license_plate";
    private static final String ARG_DURATION = "duration";
    private static final String ARG_COST = "cost";
    private static final String ARG_IS_GUEST = "is_guest";

    private String mode;
    private String username;
    private String spotId;
    private String licensePlate;
    private int durationMinutes;
    private double spotCost;
    private boolean isGuest = false;
    private EditText inCardHolderName;
    private EditText inCard;
    private EditText inEXP;
    private EditText inCVV;
    private Button btnAction;
    private TextView tvPaymentTitle, tvPaymentDetails;
    private TextInputLayout cardHolderLayout, layoutCardNumber, EXPlayout, layoutCardCVV; // Declare TextInputLayouts


    public EndParkingFragment() { /* ... */ }
    public static EndParkingFragment newInstanceForStartingSession(String username, String spotId, String licensePlate, int duration, double cost) { /* ... */
        EndParkingFragment fragment = new EndParkingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, "start_new");
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_SPOT_ID, spotId);
        args.putString(ARG_LICENSE_PLATE, licensePlate);
        args.putInt(ARG_DURATION, duration);
        args.putDouble(ARG_COST, cost);
        args.putBoolean(ARG_IS_GUEST, false);
        fragment.setArguments(args);
        return fragment;
    }
    public static EndParkingFragment newInstanceForGuestStartingSession(String spotId, String licensePlate, int duration, double cost) { /* ... */
        EndParkingFragment fragment = new EndParkingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, "start_new");
        args.putString(ARG_USERNAME, "guest");
        args.putString(ARG_SPOT_ID, spotId);
        args.putString(ARG_LICENSE_PLATE, licensePlate);
        args.putInt(ARG_DURATION, duration);
        args.putDouble(ARG_COST, cost);
        args.putBoolean(ARG_IS_GUEST, true); // isGuest is true
        fragment.setArguments(args);
        return fragment;
    }
    public static EndParkingFragment newInstanceForEndingSession(String username, String spotId) { /* ... */
        EndParkingFragment fragment = new EndParkingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, "end_existing");
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_SPOT_ID, spotId);
        args.putBoolean(ARG_IS_GUEST, false);
        fragment.setArguments(args);
        return fragment;
    }
    public static EndParkingFragment newInstanceForGuestPaymentOnly(double amountToPay) { /* ... */
        EndParkingFragment fragment = new EndParkingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, "guest_pay_only");
        args.putString(ARG_USERNAME, "guest");
        args.putDouble(ARG_COST, amountToPay);
        args.putBoolean(ARG_IS_GUEST, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mode = getArguments().getString(ARG_MODE, "end_existing");
            username = getArguments().getString(ARG_USERNAME);
            spotId = getArguments().getString(ARG_SPOT_ID);
            isGuest = getArguments().getBoolean(ARG_IS_GUEST, false); // Retrieve isGuest

            Log.d(TAG, "onCreate: mode=" + mode + ", username=" + username + ", isGuest=" + isGuest + ", spotId=" + spotId);

            if ("start_new".equals(mode)) {
                licensePlate = getArguments().getString(ARG_LICENSE_PLATE);
                durationMinutes = getArguments().getInt(ARG_DURATION);
                spotCost = getArguments().getDouble(ARG_COST);
                Log.d(TAG, "onCreate (start_new): plate=" + licensePlate + ", duration=" + durationMinutes + ", cost=" + spotCost);
            } else if ("guest_pay_only".equals(mode)) {
                spotCost = getArguments().getDouble(ARG_COST);
            }
        } else {
            Log.e(TAG, "onCreate: getArguments() is NULL!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_end_parking, container, false);

        tvPaymentTitle = view.findViewById(R.id.tvPaymentTitle);
        tvPaymentDetails = view.findViewById(R.id.tvPaymentDetails);
        inCardHolderName = view.findViewById(R.id.inCardHolderName);
        inCard = view.findViewById(R.id.inCardNumber);
        inEXP = view.findViewById(R.id.inExpDate);
        inCVV = view.findViewById(R.id.inCardCVV);
        btnAction = view.findViewById(R.id.btnEndParking);

        // Initialize TextInputLayout views
        cardHolderLayout = view.findViewById(R.id.cardHolderlayout);
        layoutCardNumber = view.findViewById(R.id.layoutCardNumber);
        EXPlayout = view.findViewById(R.id.layoutExpDate);
        layoutCardCVV = view.findViewById(R.id.layoutCardCVV);

        Log.d(TAG, "onCreateView: isGuest flag before UI update is: " + isGuest); // Log before check

        if (isGuest) {
            Log.d(TAG, "Guest mode UI: Setting card fields VISIBLE.");
            if (cardHolderLayout != null) {
                cardHolderLayout.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "cardHolderLayout is NULL in onCreateView!");
            }

            if (layoutCardNumber != null) {
                layoutCardNumber.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "layoutCardNumber is NULL in onCreateView!");
            }

            if (EXPlayout != null) {
                EXPlayout.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "EXPlayout is NULL in onCreateView!");
            }

            if (layoutCardCVV != null) {
                layoutCardCVV.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "layoutCardCVV is NULL in onCreateView!");
            }
            // Ensure EditTexts themselves are enabled
            if (inCard != null) inCard.setEnabled(true);
            if (inCVV != null) inCVV.setEnabled(true);
        } else {
            Log.d(TAG, "Registered user UI: Setting card fields GONE.");
            if (cardHolderLayout != null) cardHolderLayout.setVisibility(View.GONE);
            if (layoutCardNumber != null) layoutCardNumber.setVisibility(View.GONE);
            if (EXPlayout != null) EXPlayout.setVisibility(View.GONE);
            if (layoutCardCVV != null) layoutCardCVV.setVisibility(View.GONE);
        }

        // Setup UI text based on mode (remains the same)
        if ("start_new".equals(mode)) { /* ... */ }
        else if ("end_existing".equals(mode)) { /* ... */ }
        else if ("guest_pay_only".equals(mode)) { /* ... */ }
        // ... (rest of onCreateView, including setOnClickListener)

        if ("start_new".equals(mode)) {
            tvPaymentTitle.setText("Confirm & Pay for New Parking");
            String details = String.format(Locale.US, "Spot: %s\nPlate: %s\nDuration: %d min\nCost: $%.2f",
                    spotId, licensePlate, durationMinutes, spotCost);
            tvPaymentDetails.setText(details);
            btnAction.setText("Confirm & Pay");
        } else if ("end_existing".equals(mode)) {
            tvPaymentTitle.setText("End Parking & Pay");
            String details = String.format(Locale.US, "Ending parking for Spot: %s", spotId);
            if(getArguments() != null && getArguments().containsKey(ARG_COST)){ // Check if ARG_COST was passed for ending mode
                details += String.format(Locale.US, "\nEstimated Cost: $%.2f", getArguments().getDouble(ARG_COST));
            }
            tvPaymentDetails.setText(details);
            btnAction.setText("End Parking & Pay");
        } else if ("guest_pay_only".equals(mode)) {
            tvPaymentTitle.setText("Guest Payment");
            String details = String.format(Locale.US, "Amount to Pay: $%.2f", spotCost);
            tvPaymentDetails.setText(details);
            btnAction.setText("Pay Now");
        }


        btnAction.setOnClickListener(v -> {
            if ("start_new".equals(mode)) {
                handleFinalizeNewSession();
            } else if ("end_existing".equals(mode)) {
                handleEndExistingSession();
            } else if ("guest_pay_only".equals(mode)) {
                processGuestOnlyPayment();
            }
        });


        return view;
    }

    // ... (handleFinalizeNewSession, handleEndExistingSession, processGuestOnlyPayment, callFinalizeParkingAPI, callEndExistingParkingAPI, getGenericApiCallback methods remain the same)
    private void handleFinalizeNewSession() {
        if (isGuest) {
            // Guest starting a new session
            String name = "";
            String card = "";
            String exp = "";
            String cvv = "";

            if (inCardHolderName != null) name = inCardHolderName.getText().toString().trim();
            if (inCard != null) card = inCard.getText().toString().trim();
            if (inEXP != null) exp = inEXP.getText().toString().trim();
            if (inCVV != null) cvv = inCVV.getText().toString().trim();

            if (!name.equals(name.toUpperCase())) {
                Toast.makeText(getActivity(), "Name must be in capitals", Toast.LENGTH_SHORT).show(); return;
            }
            if (card.length() != 16 || !TextUtils.isDigitsOnly(card)) {
                Toast.makeText(getActivity(), "Card must be 16 digits", Toast.LENGTH_SHORT).show(); return;
            }
            if (cvv.length() != 3 || !TextUtils.isDigitsOnly(cvv)) {
                Toast.makeText(getActivity(), "CVV must be 3 digits", Toast.LENGTH_SHORT).show(); return;
            }
            if (exp.matches("/^(0[1-9]|1[0-2])(\\/|-)([0-9]{2})$/gm")) {
                Toast.makeText(getActivity(), "EXP must be MM/YY", Toast.LENGTH_SHORT).show(); return;
            }
            Toast.makeText(getActivity(), "Guest payment processing...", Toast.LENGTH_SHORT).show();
            callFinalizeParkingAPI(true, card, cvv);
        } else {
            callFinalizeParkingAPI(false, null, null);
        }
    }

    private void handleEndExistingSession() { /* ... */
        if (isGuest) {
            Toast.makeText(getActivity(), "Guest flow for ending session not fully defined here.", Toast.LENGTH_LONG).show();
        } else {
            if (username == null || spotId == null) {
                Toast.makeText(getActivity(), "User data or Spot ID missing for ending session.", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "Ending existing session for user: " + username + " at spot: " + spotId);
            callEndExistingParkingAPI();
        }
    }

    private void processGuestOnlyPayment() { /* ... */
        String name = "";
        String card = "";
        String exp = "";
        String cvv = "";

        if (inCardHolderName != null) name = inCardHolderName.getText().toString().trim();
        if (inCard != null) card = inCard.getText().toString().trim();
        if (inEXP != null) exp = inEXP.getText().toString().trim();
        if (inCVV != null) cvv = inCVV.getText().toString().trim();

        if (!name.equals(name.toUpperCase())) {
            Toast.makeText(getActivity(), "Name must be in capitals", Toast.LENGTH_SHORT).show(); return;
        }
        if (card.length() != 16 || !TextUtils.isDigitsOnly(card)) {
            Toast.makeText(getActivity(), "Card must be 16 digits", Toast.LENGTH_SHORT).show(); return;
        }
        if (cvv.length() != 3 || !TextUtils.isDigitsOnly(cvv)) {
            Toast.makeText(getActivity(), "CVV must be 3 digits", Toast.LENGTH_SHORT).show(); return;
        }
        if (!exp.matches("/^(0[1-9]|1[0-2])(\\/|-)([0-9]{2})$/gm")) {
            Toast.makeText(getActivity(), "EXP must be MM/YY", Toast.LENGTH_SHORT).show(); return;
        }
        Toast.makeText(getActivity(), "Payment successful (guest direct pay)", Toast.LENGTH_SHORT).show();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(new HomeFragment());
        }
    }

    private void callFinalizeParkingAPI(boolean isGuestPayment, @Nullable String card, @Nullable String cvv) { /* ... */
        Log.d(TAG, "callFinalizeParkingAPI for user: " + username + " at spot: " + spotId);
        Toast.makeText(getActivity(), "Finalizing parking session...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("username", username)
                .add("spot_id", spotId)
                .add("license_plate", licensePlate != null ? licensePlate : "") // Ensure not null
                .add("duration_minutes", String.valueOf(durationMinutes))
                .add("cost", String.valueOf(spotCost));

        if (isGuestPayment) {
            formBuilder.add("card_number", card != null ? card : "");
            formBuilder.add("cvv", cvv != null ? cvv : "");
            formBuilder.add("is_guest", "1");
        } else {
            formBuilder.add("is_guest", "0");
        }
        RequestBody requestBody = formBuilder.build();
        Request request = new Request.Builder().url(API_FINALIZE_SESSION_URL).post(requestBody).build();
        client.newCall(request).enqueue(getGenericApiCallback("Parking Started"));
    }

    private void callEndExistingParkingAPI() { /* ... */
        Log.d(TAG, "callEndExistingParkingAPI for user: " + username + " at spot: " + spotId);
        Toast.makeText(getActivity(), "Processing end of parking...", Toast.LENGTH_SHORT).show();
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("spot_id", spotId)
                .build();
        Request request = new Request.Builder().url(API_END_EXISTING_SESSION_URL).post(formBody).build();
        client.newCall(request).enqueue(getGenericApiCallback("Parking Ended"));
    }

    private Callback getGenericApiCallback(String successActionPrefix) { /* ... */
        return new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "API call failed ("+successActionPrefix+"): ", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() == null) return;
                final String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "API Response ("+successActionPrefix+"): " + responseBody);
                getActivity().runOnUiThread(() -> {
                    try {
                        if (!response.isSuccessful()){
                            Toast.makeText(getActivity(), "Server Error ("+successActionPrefix+"): " + response.message(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        if ("success".equals(status)) {
                            String successMsg = successActionPrefix + " Successfully! " + message;
                            if (jsonObject.has("new_balance")) {
                                successMsg += "\nNew Balance: $" + String.format(Locale.US, "%.2f", jsonObject.getDouble("new_balance"));
                            }
                            Toast.makeText(getActivity(), successMsg, Toast.LENGTH_LONG).show();
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).loadFragment(new HomeFragment());
                            }
                        } else {
                            Toast.makeText(getActivity(), "Operation Failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error ("+successActionPrefix+"): ", e);
                        Toast.makeText(getActivity(), "Error processing server response.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
    }
}