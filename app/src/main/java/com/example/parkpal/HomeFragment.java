package com.example.parkpal;

import android.content.Context; // For SharedPreferences
import android.content.SharedPreferences; // For SharedPreferences
import android.os.Bundle;
import android.util.Log; // For logging
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast; // For messages

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnParkUser = view.findViewById(R.id.btnPark);
        Button btnParkGuest = view.findViewById(R.id.btnParkGuest); // Make sure this ID exists in fragment_home.xml

        // Retrieve the logged-in username
        String loggedInUsername = null;
        if (getActivity() instanceof MainActivity) {
            loggedInUsername = ((MainActivity) getActivity()).getCurrentUsername();
        } else if (getContext() != null) { // Fallback if not directly in MainActivity context (less likely)
            SharedPreferences prefs = getContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
            if (prefs.getBoolean(MainActivity.PREF_KEY_IS_LOGGED_IN, false)) {
                loggedInUsername = prefs.getString(MainActivity.PREF_KEY_USERNAME, null);
            }
        }

        final String finalLoggedInUsername = loggedInUsername; // For use in lambda

        if (btnParkUser != null) {
            btnParkUser.setOnClickListener(v -> {
                if (finalLoggedInUsername != null) {
                    // User is logged in
                    String spotIdToFree = "SPOT1"; // This should eventually be dynamic (e.g., selected spot)
                    Log.d(TAG, "User " + finalLoggedInUsername + " ending parking for " + spotIdToFree);
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loadFragment(new EndParkingFragment(finalLoggedInUsername, spotIdToFree));
                    }
                } else {
                    // User is not logged in (or acting as guest if btnParkUser is also for guests)
                    // This logic might need refinement based on your UI flow for guests initiating parking
                    Log.d(TAG, "Attempting to park as guest or non-logged-in user.");
                    Toast.makeText(getActivity(), "Please log in or proceed as guest.", Toast.LENGTH_SHORT).show();
                    // Optionally, direct to EndParkingFragment in guest mode or login
                    // Example: ((MainActivity) getActivity()).loadFragment(new EndParkingFragment(true)); for guest
                    // Or: ((MainActivity) getActivity()).loadFragment(new LoginFragment());
                }
            });
        } else {
            Log.e(TAG, "btnPark (R.id.btnPark) not found in layout.");
        }


        if (btnParkGuest != null) {
            btnParkGuest.setOnClickListener(v -> {
                Log.d(TAG, "Proceeding as guest to end parking.");
                if (getActivity() instanceof MainActivity) {
                    // Ensure guest mode is handled correctly in EndParkingFragment
                    ((MainActivity) getActivity()).loadFragment(new EndParkingFragment(true)); // true for isGuest
                }
            });
        } else {
            Log.w(TAG, "btnParkGuest (R.id.btnParkGuest) not found in layout. This is optional.");
        }
        return view;
    }
}