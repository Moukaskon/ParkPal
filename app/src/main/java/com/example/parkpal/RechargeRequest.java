package com.example.parkpal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RechargeRequest {
	public String recharge(String urlString, String username, float amount) {
		try {
			URL url = new URL(urlString + "recharge.php");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			String postData = "username=" + URLEncoder.encode(username, "UTF-8")
					+ "&amount=" + amount;

			OutputStream os = conn.getOutputStream();
			os.write(postData.getBytes());
			os.flush();
			os.close();

			java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream());
			String response = scanner.useDelimiter("\\A").next();
			scanner.close();

			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"status\":\"error\",\"message\":\"Exception: " + e.getMessage() + "\"}";
		}
	}
}