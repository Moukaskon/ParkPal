package com.example.parkpal;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminRequest {
    public String deleteParkingSpot(String spotId, String baseUrl) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("id", spotId)
                .build();

        String url = baseUrl + "deleteSpot.php";
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"status\":\"error\", \"message\":\"Request failed\"}";
        }
    }

    public String fetchParkingSpots(String baseUrl) {
        OkHttpClient client = new OkHttpClient();
        String url = baseUrl + "getSpots.php";

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "[]";
        }
    }
    public String addParkingSpot(String id, double cost, String baseUrl) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("id", id)
                .add("cost", String.valueOf(cost))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "addSpot.php")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"status\":\"error\", \"message\":\"Request failed\"}";
        }
    }

    public String updateSpotCost(String id, double cost, String baseUrl) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("id", id)
                .add("cost", String.valueOf(cost))
                .build();

        String url = baseUrl + "updateCost.php";
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"status\":\"error\", \"message\":\"Request failed\"}";
        }
    }



}
