package com.example.parkpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnParkUser = view.findViewById(R.id.btnPark); // Assuming this is your button
        // You might want separate buttons for testing guest vs user if R.id.btnPark is only one
        // Or add another button to your fragment_home.xml, e.g., btnParkGuest

        btnParkUser.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                // Test LOGGED-IN USER
                String username = "tester"; // The user you created with Postman
                // String username = "john123"; // Or your hardcoded one
                String spotIdToFree = "SPOT1"; // A spot that exists and you want to "end parking" for.
                // Make sure this spot exists in your parkingSpots table.
                // And ideally, for a full test, manually set its availability to 0 in DB.
                ((MainActivity) getActivity()).loadFragment(new EndParkingFragment(username, spotIdToFree));
            }
        });

        // Example: Add another button in fragment_home.xml for guest testing
        // <Button android:id="@+id/btnParkGuest" ... />
        Button btnParkGuest = view.findViewById(R.id.btnParkGuest); // Assuming you add this
        if (btnParkGuest != null) {
            btnParkGuest.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    // Test GUEST
                    ((MainActivity) getActivity()).loadFragment(new EndParkingFragment(true)); // Pass true for isGuest
                }
            });
        }


        return view;
    }
}