package com.example.parkpal;// UserHistoryFragment.java - Fragment to display user parking history
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkpal.HistoryAdapter;
import com.example.parkpal.HistoryItem;

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
    private
    List<HistoryItem> historyList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_history, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        if (getActivity() != null) {
            // Retrieve the logged-in user's ID (from SharedPreferences)
            SharedPreferences prefs = getActivity()
                    .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1); // default -1 if not set
            if (userId != -1) {
                fetchUserHistory(userId);
            } else {
                System.out.println("Cannot find user id");
            }
        }

        return rootView;
    }

    private void fetchUserHistory(int userId) {
        OkHttpClient client = new OkHttpClient();

        // Build the URL with the user_id query parameter
        HttpUrl url = HttpUrl.parse("https://yourdomain.com/get_user_history.php")
                .newBuilder()
                .addQueryParameter("user_id", String.valueOf(userId))
                .build();

        // Create the request
        Request request = new Request.Builder().url(url).build();

        // Perform asynchronous network call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle request failure (e.g., log or display an error)
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String createdAt = obj.getString("created_at");
                            int parkingTime = obj.getInt("parking_time");
                            int spotId = obj.getInt("spot_id");
                            double cost = obj.getDouble("cost");
                            HistoryItem item = new HistoryItem(createdAt, parkingTime, spotId, cost);
                            historyList.add(item);
                        }
                        // Update the RecyclerView on the main thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle unsuccessful response (e.g., show error)
                }
            }
        });
    }
}
