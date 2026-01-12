package com.sanjana.oneforall.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.*;
import com.sanjana.oneforall.ui.home.AddEditItemActivity;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final Context context;
    private final List<Item> items;
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        this.db = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_home, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder h, int position) {
        Item item = items.get(position);

        h.tvTitle.setText(item.title);
        h.tvStatus.setText(item.status);

        // Load category
        executor.execute(() -> {
            Category c = db.categoryDao().getCategoryById(item.categoryId);
            h.itemView.post(() -> {
                if (c != null) {
                    h.tvTitle.setTextColor(c.color);
                    h.tvCategory.setText(c.name);
                }
            });
        });

        int max = item.totalProgress > 0 ? item.totalProgress : 100;
        h.progressBar.setMax(max);
        h.progressBar.setProgress(item.currentProgress);
        h.tvProgress.setText(item.currentProgress + "/" + max);

        h.btnIncrease.setVisibility(item.currentProgress < max ? View.VISIBLE : View.GONE);
        h.btnDecrease.setVisibility(item.currentProgress > 0 ? View.VISIBLE : View.GONE);

        // ------------------- INCREASE -------------------
        h.btnIncrease.setOnClickListener(v -> changeProgress(item, position, true));

        // ------------------- DECREASE -------------------
        h.btnDecrease.setOnClickListener(v -> changeProgress(item, position, false));

        // ------------------- EDIT -------------------
        h.btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(context, AddEditItemActivity.class);
            i.putExtra("itemId", item.id);
            context.startActivity(i);
        });

        // ------------------- DELETE -------------------
        h.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle("Delete Item")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (d, w) -> executor.execute(() -> {
                    db.itemDao().delete(item);

                    removeCalendarEvent(item.startDate, item.title + " (Started)");
                    removeCalendarEvent(item.endDate, item.title + " (Ended)");
                    removeWatchingEventByPrefix(LocalDate.now().toString(), item.title);

                    h.itemView.post(() -> {
                        items.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    });
                }))
                .setNegativeButton("Cancel", null)
                .show()
        );
    }

    // ------------------- CHANGE PROGRESS -------------------
    private void changeProgress(Item item, int position, boolean increase) {
        executor.execute(() -> {
            int old = item.currentProgress;
            int max = item.totalProgress > 0 ? item.totalProgress : 100;

            if (increase && old < max) item.currentProgress++;
            if (!increase && old > 0) item.currentProgress--;

            String today = LocalDate.now().toString();

            // ------------------ STARTED ------------------
            if (old == 0 && item.currentProgress == 1) {
                if (item.startDate == null || item.startDate.isEmpty()) item.startDate = today;
                addCalendarEvent(item.startDate, item.title + " (Started)", "Started");
                item.status = "Watching";
            }

            // ------------------ DAILY PROGRESS ------------------
            if (item.currentProgress > 0 && item.currentProgress < max) {
                DailyProgress dp = db.dailyProgressDao().getByItemAndDate(item.id, today);
                if (dp == null) {
                    dp = new DailyProgress(item.id, today, item.currentProgress, item.currentProgress);
                    db.dailyProgressDao().insert(dp);
                } else {
                    dp.lastEp = item.currentProgress;
                    if (dp.firstEp > item.currentProgress) dp.firstEp = item.currentProgress;
                    db.dailyProgressDao().update(dp);
                }

                // Update or add watching event dynamically
                if (dp.firstEp > 0) {
                    addOrUpdateWatchingEvent(today, item.title, dp.firstEp, dp.lastEp);
                }

                item.status = "Watching";
            }

            // ------------------ ENDED ------------------
            if (item.currentProgress == max && max > 0) {
                removeWatchingEventByPrefix(today, item.title);
                addCalendarEvent(today, item.title + " (Ended)", "Completed");
                item.status = "Completed";
            }

            // ------------------ DECREASE ------------------
            if (!increase) {
                if (old == max && item.currentProgress < max) removeCalendarEvent(today, item.title + " (Ended)");
                if (item.currentProgress == 0) {
                    removeCalendarEvent(item.startDate, item.title + " (Started)");
                    item.status = "Plan to Watch";
                }
            }

            db.itemDao().update(item);
            notifyUi(position);
        });
    }

    private void notifyUi(int position) {
        new android.os.Handler(android.os.Looper.getMainLooper())
                .post(() -> notifyItemChanged(position));
    }

    // ------------------- CALENDAR -------------------
    private void addOrUpdateWatchingEvent(String date, String title, int firstEp, int lastEp) {
        if (date == null || date.isEmpty()) return;

        String prefix = title + " (Watching)";
        CalendarEvent existing = db.calendarEventDao().getEventByTitleAndDatePrefix(prefix, date);

        String newTitle = prefix + " " + (lastEp - firstEp + 1) + " eps (" + firstEp + "-" + lastEp + ")";

        if (existing == null) {
            db.calendarEventDao().insert(new CalendarEvent(newTitle, date, "Watching"));
        } else {
            existing.title = newTitle;
            db.calendarEventDao().update(existing);
        }
    }

    private void addCalendarEvent(String date, String title, String status) {
        if (date == null || date.isEmpty()) return;
        CalendarEvent existing = db.calendarEventDao().getEventByTitleAndDate(title, date);
        if (existing == null) db.calendarEventDao().insert(new CalendarEvent(title, date, status));
    }

    private void removeCalendarEvent(String date, String title) {
        if (date == null || date.isEmpty()) return;
        CalendarEvent e = db.calendarEventDao().getEventByTitleAndDate(title, date);
        if (e != null) db.calendarEventDao().delete(e);
    }

    private void removeWatchingEventByPrefix(String date, String title) {
        if (date == null || date.isEmpty()) return;
        String prefix = title + " (Watching)";
        CalendarEvent e = db.calendarEventDao().getEventByTitleAndDatePrefix(prefix, date);
        if (e != null) db.calendarEventDao().delete(e);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ------------------- VIEW HOLDER -------------------
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvStatus, tvProgress;
        ProgressBar progressBar;
        ImageButton btnEdit, btnDelete, btnIncrease, btnDecrease;

        ItemViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.itemTitle);
            tvCategory = v.findViewById(R.id.itemCategory);
            tvStatus = v.findViewById(R.id.itemStatus);
            tvProgress = v.findViewById(R.id.itemProgressText);
            progressBar = v.findViewById(R.id.itemProgress);
            btnEdit = v.findViewById(R.id.editItem);
            btnDelete = v.findViewById(R.id.deleteItem);
            btnIncrease = v.findViewById(R.id.btnIncreaseProgress);
            btnDecrease = v.findViewById(R.id.btnDecreaseProgress);
        }
    }
}
