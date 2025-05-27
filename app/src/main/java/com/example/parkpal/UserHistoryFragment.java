package com.example.parkpal;
// Imports remain the same...
import android.content.Context; // Still needed for Toast
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserHistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyList = new ArrayList<>();
    private String currentUsername; // To store the username passed via constructor

    private static final String TAG = "UserHistoryFragment";
    // URL remains the same, but the parameter sent will change
    private static final String API_GET_HISTORY_URL = "http://10.0.2.2/ParkPall/get_user_history.php";


    // Required empty public constructor for fragment recreation by system
    public UserHistoryFragment() {
    }

    // Constructor to receive username
    public UserHistoryFragment(String username) {
        this.currentUsername = username;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_history, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        // If username was passed through constructor, use it.
        // Also handle case where fragment might be recreated and username needs to be restored from arguments
        if (currentUsername == null && getArguments() != null) {
            currentUsername = getArguments().getString("username_arg");
        }

        if (currentUsername != null && !currentUsername.isEmpty()) {
            Log.d(TAG, "Fetching history for username: " + currentUsername);
            fetchUserHistory(currentUsername);
        } else {
            Log.w(TAG, "Username not provided to UserHistoryFragment.");
            Toast.makeText(getContext(), "User information not available.", Toast.LENGTH_LONG).show();
            // Optionally, navigate away or show a placeholder
        }
        return rootView;
    }

    // Helper to create instance with arguments (good practice for fragments)
    public static UserHistoryFragment newInstance(String username) {
        UserHistoryFragment fragment = new UserHistoryFragment();
        Bundle args = new Bundle();
        args.putString("username_arg", username);
        fragment.setArguments(args);
        return fragment;
    }

    private void fetchUserHistory(String usernameToFetch) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(API_GET_HISTORY_URL).newBuilder();
        // NOW SEND 'username' INSTEAD OF 'user_id'
        urlBuilder.addQueryParameter("username", usernameToFetch);
        String url = urlBuilder.build().toString();
        Log.d(TAG, "Request URL: " + url);

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "API call failed: ", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to load history: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() == null) return;

                final String responseData = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "API Response: " + responseData);

                if (response.isSuccessful()) {
                    try {
                        historyList.clear();
                        JSONArray jsonArray = new JSONArray(responseData);
                        // Check if the response is an error object from PHP instead of an array
                        if (jsonArray.length() > 0 && jsonArray.getJSONObject(0).has("status") && "error".equals(jsonArray.getJSONObject(0).getString("status"))) {
                            String errorMessage = jsonArray.getJSONObject(0).getString("message");
                            Log.e(TAG, "PHP Error: " + errorMessage);
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show());
                            return;
                        }


                        if (jsonArray.length() == 0) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "No history found for " + usernameToFetch, Toast.LENGTH_SHORT).show());
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String createdAt = obj.getString("created_at_formatted");
                            int parkingTime = obj.getInt("parking_time");
                            String spotDisplayId = obj.getString("spot_id_str");
                            double cost = obj.getDouble("cost");
                            HistoryItem item = new HistoryItem(createdAt, parkingTime, spotDisplayId, cost);
                            historyList.add(item);
                        }
                        getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (JSONException e) {
                        // Check if responseData itself is a JSON object error message (e.g. from PHP die(json_encode...))
                        try {
                            JSONObject errorObject = new JSONObject(responseData);
                            if (errorObject.has("status") && "error".equals(errorObject.getString("status"))) {
                                String errorMessage = errorObject.getString("message");
                                Log.e(TAG, "PHP Error (single object): " + errorMessage);
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show());
                                return;
                            }
                        } catch (JSONException jsonException) {
                            // Not a JSON object error, proceed with original error handling
                        }
                        Log.e(TAG, "JSON parsing error: ", e);
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error parsing history data.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e(TAG, "API request unsuccessful: " + response.code() + " " + response.message());
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error loading history: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}