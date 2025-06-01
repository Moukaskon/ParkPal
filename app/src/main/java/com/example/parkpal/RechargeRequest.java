package com.example.parkpal;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.FormBody;

public class RechargeRequest {
	private final OkHttpClient client = new OkHttpClient();

	public String recharge(String urlString, String username, float amount) {
		try {
			RequestBody formBody = new FormBody.Builder()
					.add("username", username)
					.add("amount", String.valueOf(amount))
					.build();

			Request request = new Request.Builder()
					.url(urlString + "recharge.php")
					.post(formBody)
					.build();

			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful()) {
					return "{\"status\":\"error\",\"message\":\"HTTP error code: " + response.code() + "\"}";
				}
				return response.body().string();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"status\":\"error\",\"message\":\"Exception: " + e.getMessage() + "\"}";
		}
	}
}
