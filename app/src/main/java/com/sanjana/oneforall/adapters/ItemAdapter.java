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
        View v = LayoutInflater.from(context).inflate(R.layout.item_home, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder h, int position) {
        Item item = items.get(position);

        h.tvTitle.setText(item.title);
        h.tvStatus.setText(item.status);

        // Load category (name + color)
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

        h.btnIncrease.setOnClickListener(v -> changeProgress(item, position, true));
        h.btnDecrease.setOnClickListener(v -> changeProgress(item, position, false));

        h.btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(context, AddEditItemActivity.class);
            i.putExtra("itemId", item.id);
            context.startActivity(i);
        });

        h.btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(context)
                        .setTitle("Delete Item")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", (d, w) -> executor.execute(() -> {
                            db.itemDao().delete(item);

                            List<CalendarEvent> events =
                                    db.calendarEventDao().getEventsByTitle(item.title);
                            for (CalendarEvent e : events) {
                                db.calendarEventDao().delete(e);
                            }

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

    // ---------------- PROGRESS LOGIC ----------------

    private void changeProgress(Item item, int position, boolean increase) {
        executor.execute(() -> {
            int old = item.currentProgress;
            int max = item.totalProgress > 0 ? item.totalProgress : 100;

            if (increase && old < max) item.currentProgress++;
            if (!increase && old > 0) item.currentProgress--;

            String today = LocalDate.now().toString();

            // STARTED
            if (old == 0 && item.currentProgress == 1) {
                item.startDate = today;
                addCalendarEvent(item, today, 1, 1);
                item.status = "Watching";
            }

            // WATCHING
            if (item.currentProgress > 0 && item.currentProgress < max) {
                addOrUpdateWatchingEvent(item, today);
                item.status = "Watching";
            }

            // COMPLETED
            if (item.currentProgress == max) {
                removeWatchingEvent(today, item.title);
                addCalendarEvent(item, today, max, max);
                item.status = "Completed";
            }

            db.itemDao().update(item);
            notifyUi(position);
        });
    }

    private void notifyUi(int position) {
        new android.os.Handler(android.os.Looper.getMainLooper())
                .post(() -> notifyItemChanged(position));
    }

    // ---------------- CALENDAR HELPERS ----------------

    private void addCalendarEvent(Item item, String date, int startEp, int endEp) {
        CalendarEvent e = new CalendarEvent(
                item.title,
                date,
                endEp - startEp + 1,
                startEp,
                endEp,
                getCategoryColor(item.categoryId)
        );
        db.calendarEventDao().insert(e);
    }

    private void addOrUpdateWatchingEvent(Item item, String date) {
        String prefix = item.title;
        CalendarEvent existing =
                db.calendarEventDao().getEventByTitleAndDatePrefix(prefix, date);

        CalendarEvent e = new CalendarEvent(
                item.title,
                date,
                item.currentProgress,
                1,
                item.currentProgress,
                getCategoryColor(item.categoryId)
        );

        if (existing == null) {
            db.calendarEventDao().insert(e);
        } else {
            existing.episodeCount = e.episodeCount;
            existing.startEp = e.startEp;
            existing.endEp = e.endEp;
            db.calendarEventDao().update(existing);
        }
    }

    private void removeWatchingEvent(String date, String title) {
        CalendarEvent e =
                db.calendarEventDao().getEventByTitleAndDatePrefix(title, date);
        if (e != null) db.calendarEventDao().delete(e);
    }

    private int getCategoryColor(int categoryId) {
        Category c = db.categoryDao().getCategoryById(categoryId);
        return c != null ? c.color : 0xFF000000;
    }

    // ---------------- REQUIRED ----------------

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ---------------- VIEW HOLDER ----------------

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
