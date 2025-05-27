package com.example.parkpal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCreatedAt, tvParkingTime, tvSpotId, tvCost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCreatedAt = itemView.findViewById(R.id.dateTextView);         // corresponds to left top
            tvParkingTime = itemView.findViewById(R.id.timeTextView);       // corresponds to left bottom
            tvSpotId = itemView.findViewById(R.id.zoneTextView);            // center
            tvCost = itemView.findViewById(R.id.costTextView);              // right
        }
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);

        holder.tvCreatedAt.setText(item.getCreatedAt());
        holder.tvParkingTime.setText(item.getParkingTime() + " min");
        holder.tvSpotId.setText("Zone: " + item.getSpotId());  // optionally adjust this later to show name
        holder.tvCost.setText("€ " + String.format("%.2f", item.getCost()));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}