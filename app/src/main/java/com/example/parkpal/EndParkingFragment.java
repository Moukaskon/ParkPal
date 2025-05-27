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
    // This URL will now point to a script that handles both starting (with payment) and ending parking
    private static final String API_FINALIZE_SESSION_URL = "http://10.0.2.2/ParkPall/finalize_parking_session.php";
    private static final String API_END_EXISTING_SESSION_URL = "http://10.0.2.2/ParkPall/process_payment.php"; // Original URL for ending

    // Argument keys
    private static final String ARG_MODE = "mode"; // "start_new" or "end_existing"
    private static final String ARG_USERNAME = "username";
    private static final String ARG_SPOT_ID = "spot_id";
    private static final String ARG_LICENSE_PLATE = "license_plate";
    private static final String ARG_DURATION = "duration";
    private static final String ARG_COST = "cost"; // For new sessions, cost is pre-calculated/fetched
    private static final String ARG_IS_GUEST = "is_guest";


    private String mode; // "start_new" or "end_existing"
    private String username;
    private String spotId;
    private String licensePlate; // Only for "start_new"
    private int durationMinutes; // Only for "start_new"
    private double spotCost;     // Cost of the spot/session
    private boolean isGuest = false;

    private EditText inCard;
    private EditText inCVV;
    private Button btnAction; // Renamed from btnEnd
    private TextView tvPaymentTitle, tvPaymentDetails;


    public EndParkingFragment() {
        // Required empty public constructor
    }

    // Factory method for STARTING a NEW parking session (R1 -> R2)
    public static EndParkingFragment newInstanceForStartingSession(String username, String spotId, String licensePlate, int duration, double cost) {
        EndParkingFragment fragment = new EndParkingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, "start_new");
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_SPOT_ID, spotId);
        args.putString(ARG_LICENSE_PLATE, licensePlate);
        args.putInt(ARG_DURATION, duration);
        args.putDouble(ARG_COST, cost);
        args.putBoolean(ARG_IS_GUEST, false); // Assuming registered user for this flow initially
        fragment.setArguments(args);
        return fragment;
    }
    // Factory method for Guest STARTING a NEW parking session
    public static EndParkingFragment newInstanceForGuestStartingSession(String spotId, String licensePlate, int duration, double cost) {
        EndParkingFragment fragment = new EndParkingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, "start_new");
        args.putString(ARG_USERNAME, "guest"); // Guest username
        args.putString(ARG_SPOT_ID, spotId);
        args.putString(ARG_LICENSE_PLATE, licensePlate);
        args.putInt(ARG_DURATION, duration);
        args.putDouble(ARG_COST, cost);
        args.putBoolean(ARG_IS_GUEST, true);
        fragment.setArguments(args);
        return fragment;
    }


    // Factory method for ENDING an EXISTING parking session (Original R2)
    public static EndParkingFragment newInstanceForEndingSession(String username, String spotId) {
        EndParkingFragment fragment = new EndParkingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, "end_existing");
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_SPOT_ID, spotId);
        args.putBoolean(ARG_IS_GUEST, false); // Registered user ending their session
        // Cost for ending will be fetched from backend based on active session
        fragment.setArguments(args);
        return fragment;
    }

    // Factory method for GUEST just paying (e.g. if they initiated parking differently and now just pay)
    // This constructor might be less used if guest flow also goes through NewParkFragment
    public static EndParkingFragment newInstanceForGuestPaymentOnly(double amountToPay) {
        EndParkingFragment fragment = new EndParkingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, "guest_pay_only"); // A specific mode for guest paying arbitrary amount
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
            mode = getArguments().getString(ARG_MODE, "end_existing"); // Default to ending
            username = getArguments().getString(ARG_USERNAME);
            spotId = getArguments().getString(ARG_SPOT_ID);
            isGuest = getArguments().getBoolean(ARG_IS_GUEST, false);

            if ("start_new".equals(mode)) {
                licensePlate = getArguments().getString(ARG_LICENSE_PLATE);
                durationMinutes = getArguments().getInt(ARG_DURATION);
                spotCost = getArguments().getDouble(ARG_COST);
            } else if ("guest_pay_only".equals(mode)) {
                spotCost = getArguments().getDouble(ARG_COST);
            }
            // For "end_existing", cost will be fetched if needed.
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_end_parking, container, false); // Reuse layout for now

        tvPaymentTitle = view.findViewById(R.id.tvPaymentTitle); // Add these to your XML
        tvPaymentDetails = view.findViewById(R.id.tvPaymentDetails); // Add these to your XML

        inCard = view.findViewById(R.id.inCardNumber);
        inCVV = view.findViewById(R.id.inCardCVV);
        btnAction = view.findViewById(R.id.btnEndParking); // Keep ID, change text

        if (isGuest) {
            inCard.setVisibility(View.VISIBLE);
            inCVV.setVisibility(View.VISIBLE);
        } else {
            inCard.setVisibility(View.GONE);
            inCVV.setVisibility(View.GONE);
        }

        // Setup UI based on mode
        if ("start_new".equals(mode)) {
            tvPaymentTitle.setText("Confirm & Pay for New Parking");
            String details = String.format(Locale.US, "Spot: %s\nPlate: %s\nDuration: %d min\nCost: $%.2f",
                    spotId, licensePlate, durationMinutes, spotCost);
            tvPaymentDetails.setText(details);
            btnAction.setText("Confirm & Pay");
        } else if ("end_existing".equals(mode)) {
            tvPaymentTitle.setText("End Parking & Pay");
            // Details for ending parking would typically show duration used and final cost
            // This cost needs to be fetched if not already known.
            // For now, we'll assume 'spotCost' might be a placeholder or fetched prior.
            String details = String.format(Locale.US, "Ending parking for Spot: %s", spotId);
            // If spotCost is known for ending (e.g. flat rate or fetched)
            if(getArguments().containsKey(ARG_COST)){
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

    private void handleFinalizeNewSession() {
        if (isGuest) {
            // Guest starting a new session
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
            // For guest, payment is "simulated" successful, then call backend
            Toast.makeText(getActivity(), "Guest payment processing...", Toast.LENGTH_SHORT).show();
            callFinalizeParkingAPI(true, card, cvv);
        } else {
            // Registered user starting a new session
            // Check balance against spotCost, then call backend
            callFinalizeParkingAPI(false, null, null);
        }
    }


    private void handleEndExistingSession() {
        // This is the original logic for when a user ends parking for a spot they already occupy
        if (isGuest) { // Guests typically wouldn't "end" a session in the same way as registered users
            Toast.makeText(getActivity(), "Guest flow for ending session not fully defined here.", Toast.LENGTH_LONG).show();
            // Potentially, a guest might have a reference number to end and pay.
        } else {
            // Registered user ending an existing session
            // The API_END_EXISTING_SESSION_URL will calculate cost based on active_parking_sessions
            // and then process payment from balance.
            if (username == null || spotId == null) {
                Toast.makeText(getActivity(), "User data or Spot ID missing for ending session.", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "Ending existing session for user: " + username + " at spot: " + spotId);
            callEndExistingParkingAPI();
        }
    }
    private void processGuestOnlyPayment() {
        String card = inCard.getText().toString().trim();
        String cvv = inCVV.getText().toString().trim();
        if (card.length() != 16 || !TextUtils.isDigitsOnly(card)) {
            Toast.makeText(getActivity(), "Card must be 16 digits", Toast.LENGTH_SHORT).show(); return;
        }
        if (cvv.length() != 3 || !TextUtils.isDigitsOnly(cvv)) {
            Toast.makeText(getActivity(), "CVV must be 3 digits", Toast.LENGTH_SHORT).show(); return;
        }
        Toast.makeText(getActivity(), "Payment successful (guest direct pay)", Toast.LENGTH_SHORT).show();
        // Here you might log this guest payment to a generic transaction log if needed
        // For now, just a success message and navigate.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(new HomeFragment());
        }
    }


    // API call for STARTING and finalizing a NEW session
    private void callFinalizeParkingAPI(boolean isGuestPayment, @Nullable String card, @Nullable String cvv) {
        Log.d(TAG, "callFinalizeParkingAPI for user: " + username + " at spot: " + spotId);
        Toast.makeText(getActivity(), "Finalizing parking session...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("username", username) // "guest" for guests
                .add("spot_id", spotId)
                .add("license_plate", licensePlate)
                .add("duration_minutes", String.valueOf(durationMinutes))
                .add("cost", String.valueOf(spotCost)); // Send pre-fetched cost

        if (isGuestPayment) {
            formBuilder.add("card_number", card);
            formBuilder.add("cvv", cvv);
            formBuilder.add("is_guest", "1");
        } else {
            formBuilder.add("is_guest", "0");
        }

        RequestBody requestBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(API_FINALIZE_SESSION_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(getGenericApiCallback("Parking Started"));
    }

    // API call for ENDING an EXISTING session
    private void callEndExistingParkingAPI() {
        // This reuses the original logic from your EndParkingFragment's checkAndProcessUserPayment
        Log.d(TAG, "callEndExistingParkingAPI for user: " + username + " at spot: " + spotId);
        Toast.makeText(getActivity(), "Processing end of parking...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("spot_id", spotId) // Backend will use this to find active session and calculate cost
                .build();

        Request request = new Request.Builder()
                .url(API_END_EXISTING_SESSION_URL) // Original URL for ending/payment
                .post(formBody)
                .build();
        client.newCall(request).enqueue(getGenericApiCallback("Parking Ended"));
    }

    // Generic Callback for API responses
    private Callback getGenericApiCallback(String successActionPrefix) {
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
                            if (jsonObject.has("new_balance")) { // For registered users
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