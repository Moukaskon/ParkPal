package com.example.parkpal;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class UserRechargeFragment extends Fragment {
	public UserRechargeFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_user_recharge_credit, container, false);

		EditText inAmount = view.findViewById(R.id.inCreditAmount);
		Button btnAdd = view.findViewById(R.id.addCreditButton);

		btnAdd.setOnClickListener(v -> {
			String amountStr = inAmount.getText().toString().trim();

			if (!amountStr.isEmpty()) {
				try {
					float amount = Float.parseFloat(amountStr);
					RechargeRequest rechargeRequest = new RechargeRequest();

					String response = rechargeRequest.recharge("http://192.168.1.12/ParkPall/", ((MainActivity) getActivity()).getCurrentUsername(), amount);
					System.out.println("Recharge Response: " + response);
					// You can show a Toast based on success/failure here if needed.
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});

		return view;
	}
}