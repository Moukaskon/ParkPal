package com.example.parkpal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class BalanceRequest {
	public String getBalance(String urlString, String username) {
		try {
			URL url = new URL(urlString + "/get_balance.php");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);

			String postData = "username=" + username;
			OutputStream os = conn.getOutputStream();
			os.write(postData.getBytes());
			os.flush();
			os.close();

			Scanner in = new Scanner(conn.getInputStream());
			StringBuilder response = new StringBuilder();
			while (in.hasNextLine()) {
				response.append(in.nextLine());
			}
			in.close();

			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"status\":\"error\",\"message\":\"Exception occurred\"}";
		}
	}
}