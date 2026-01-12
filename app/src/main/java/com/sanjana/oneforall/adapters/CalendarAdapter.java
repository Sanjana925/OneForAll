package com.sanjana.oneforall.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.CalendarEvent;

import java.util.List;
import java.util.function.Consumer;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private final Context context;
    private final List<DayCell> dayCells;
    private final Consumer<DayCell> clickCallback;

    public CalendarAdapter(Context context, List<DayCell> dayCells, Consumer<DayCell> clickCallback) {
        this.context = context;
        this.dayCells = dayCells;
        this.clickCallback = clickCallback;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayCell cell = dayCells.get(position);
        holder.llEventContainer.removeAllViews();

        if (cell.day == 0) {
            holder.tvDay.setText("");
            return;
        }

        holder.tvDay.setText(String.valueOf(cell.day));

        int maxItems = Math.min(3, cell.events.size());
        for (int i = 0; i < maxItems; i++) {
            CalendarEvent e = cell.events.get(i);

            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(12, 12, 12, 12);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(8f);
            bg.setColor(0xFFF5F5F5);
            bg.setStroke(1, 0xFFDDDDDD);
            card.setBackground(bg);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.topMargin = 6;
            card.setLayoutParams(lp);

            TextView tvTitle = new TextView(context);
            tvTitle.setText(e.title);
            tvTitle.setTextColor(e.categoryColor);
            tvTitle.setTextSize(13f);
            tvTitle.setSingleLine(true);
            tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);

            card.addView(tvTitle);
            holder.llEventContainer.addView(card);

            // click card -> show popup
            card.setOnClickListener(v -> clickCallback.accept(cell));
        }

        holder.itemView.setOnClickListener(v -> clickCallback.accept(cell));
    }

    @Override
    public int getItemCount() {
        return dayCells.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        LinearLayout llEventContainer;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            llEventContainer = itemView.findViewById(R.id.llEventContainer);
        }
    }
}
