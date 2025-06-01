package com.example.parkpal;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BalanceRequest {
	private final OkHttpClient client = new OkHttpClient();

	public String getBalance(String urlString, String username) {
		try {
			String fullUrl = urlString + "/get_balance.php?username=" + username;

			Request request = new Request.Builder()
					.url(fullUrl)
					.get()
					.build();

			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful()) {
					return "{\"status\":\"error\",\"message\":\"HTTP error code: " + response.code() + "\"}";
				}
				return response.body().string();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"status\":\"error\",\"message\":\"Exception occurred\"}";
		}
	}
}