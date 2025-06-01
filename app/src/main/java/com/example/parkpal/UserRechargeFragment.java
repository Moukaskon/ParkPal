package com.example.parkpal;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class UserRechargeFragment extends Fragment {
	public UserRechargeFragment() {}

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

					// Run network request on background thread
					new Thread(() -> {
						RechargeRequest rechargeRequest = new RechargeRequest();
						String response = rechargeRequest.recharge(
								"http://10.0.2.2/ParkPall/",
								((MainActivity) getActivity()).getCurrentUsername(),
								amount
						);

						// Parse response JSON
						try {
							JSONObject json = new JSONObject(response);
							String status = json.getString("status");

							Handler mainHandler = new Handler(Looper.getMainLooper());
							if (status.equals("success")) {
								mainHandler.post(() -> {
									Toast.makeText(getActivity(), amount + " credits added!", Toast.LENGTH_SHORT).show();
									((MainActivity) getActivity()).loadFragment(new WalletFragment());
								});
							} else {
								String message = json.optString("message", "Recharge failed");
								mainHandler.post(() ->
										Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_LONG).show());
							}
						} catch (Exception e) {
							e.printStackTrace();
							new Handler(Looper.getMainLooper()).post(() ->
									Toast.makeText(getActivity(), "Invalid server response", Toast.LENGTH_LONG).show());
						}
					}).start();
				} catch (NumberFormatException e) {
					Toast.makeText(getActivity(), "Invalid amount", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getActivity(), "Please enter an amount", Toast.LENGTH_SHORT).show();
			}
		});

		return view;
	}
}
