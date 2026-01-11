package com.sanjana.oneforall.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.R;

import java.util.List;

public class DailyEventsAdapter extends RecyclerView.Adapter<DailyEventsAdapter.EventViewHolder> {

    private List<String> eventTitles;

    public DailyEventsAdapter(List<String> eventTitles) {
        this.eventTitles = eventTitles;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        String title = eventTitles.get(position);
        holder.tvEventTitle.setText(title);
    }

    @Override
    public int getItemCount() {
        return eventTitles.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
        }
    }
}
