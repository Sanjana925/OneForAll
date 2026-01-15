package com.sanjana.oneforall.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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
        boolean isCompleted = item.currentProgress >= max || "Completed".equals(item.status);

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
                            db.calendarEventDao().getEventsByTitle(item.title)
                                    .forEach(db.calendarEventDao()::delete);

                            item.startDate = null;
                            item.endDate = null;
                            item.status = "Planned";
                            item.currentProgress = 0;

                            db.itemDao().delete(item);

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

    private void changeProgress(Item item, int position, boolean increase) {
        executor.execute(() -> {
            int old = item.currentProgress;
            int max = item.totalProgress > 0 ? item.totalProgress : 100;

            if (increase && old < max) item.currentProgress++;
            if (!increase && old > 0) item.currentProgress--;

            item.lastUpdated = System.currentTimeMillis();
            String today = LocalDate.now().toString();

            if (item.currentProgress > 0 && item.startDate == null) {
                item.startDate = today;
            }

            if (item.currentProgress == 0) {
                item.status = "Planned";
                CalendarEvent e = db.calendarEventDao().getEventByTitleAndDate(item.title, today);
                if (e != null) db.calendarEventDao().delete(e);
            } else if (item.currentProgress >= max) {
                item.status = "Completed";
            } else {
                item.status = "Watching";
            }

            if (item.currentProgress > 0) {
                addOrUpdateProgressEvent(item, today);
            }

            db.itemDao().update(item);

            new Handler(Looper.getMainLooper()).post(() -> notifyItemChanged(position));
        });
    }

    private void addOrUpdateProgressEvent(Item item, String date) {
        CalendarEvent existing = db.calendarEventDao().getEventByTitleAndDate(item.title, date);

        if (existing == null) {
            CalendarEvent e = new CalendarEvent(
                    item.title,
                    date,
                    1,
                    item.currentProgress,
                    item.currentProgress,
                    getCategoryColor(item.categoryId)
            );
            db.calendarEventDao().insert(e);
        } else {
            existing.endEp = item.currentProgress;
            existing.episodeCount = existing.endEp - existing.startEp + 1;
            db.calendarEventDao().update(existing);
        }
    }

    private int getCategoryColor(int categoryId) {
        Category c = db.categoryDao().getCategoryById(categoryId);
        return c != null ? c.color : 0xFF000000;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

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
