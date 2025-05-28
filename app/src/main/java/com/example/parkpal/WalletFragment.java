package com.example.parkpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

public class WalletFragment extends Fragment {

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container,
							  Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_wallet, container, false);

		Button btnRecharge = view.findViewById(R.id.rechargeCreditBTN);

		btnRecharge.setOnClickListener(v -> {
			if (getActivity() instanceof MainActivity) {
				((MainActivity) getActivity()).loadFragment(new UserRechargeFragment());
			}
		});


		TextView balanceText = view.findViewById(R.id.remainingCreditTXT);

		// thread to get the users balance form the server
		// so that it does not freeze the UI
		new Thread(() -> {
			BalanceRequest balanceRequest = new BalanceRequest();
			String response = balanceRequest.getBalance("http://10.0.2.2/ParkPall", ((MainActivity) getActivity()).getCurrentUsername());


			try {
				JSONObject json = new JSONObject(response);
				if (json.getString("status").equals("success")) {
					double balance = json.getDouble("balance");
					requireActivity().runOnUiThread(() ->
							balanceText.setText("" + balance + "$"));
				} else {
					requireActivity().runOnUiThread(() ->
					{
						try {
							balanceText.setText("Error: " + json.getString("message"));
						} catch (JSONException e) {
							throw new RuntimeException(e);
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
				requireActivity().runOnUiThread(() ->
						balanceText.setText("Error parsing balance"));
			}
		}).start();

		return view;
	}
}