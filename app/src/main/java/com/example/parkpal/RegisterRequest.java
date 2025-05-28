package com.example.parkpal;
import okhttp3.*;

public class RegisterRequest {

    public String register(String url, String username, String password) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("email", "me@mail.com")
                .build();

        Request request = new Request.Builder()
                .url(url + "register.php")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "Server error: " + response.code();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Request failed: " + e.getMessage();
        }
    }
}
