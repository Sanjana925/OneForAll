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
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.Category;
import com.sanjana.oneforall.database.CalendarEvent;
import com.sanjana.oneforall.database.Item;
import com.sanjana.oneforall.ui.home.AddEditItemActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context context;
    private List<Item> items;
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        db = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_home, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);

        // Title
        holder.tvTitle.setText(item.title);

        // Load category in background
        executor.execute(() -> {
            List<Category> categories = db.categoryDao().getAllCategories();
            Category category = null;
            for (Category c : categories) if (c.id == item.categoryId) category = c;

            Category finalCategory = category;
            holder.tvCategory.post(() ->
                    holder.tvCategory.setText(finalCategory != null ? finalCategory.name : "Unknown")
            );
        });

        // Progress
        holder.progressBar.setMax(item.totalProgress > 0 ? item.totalProgress : 100);
        holder.progressBar.setProgress(item.currentProgress);
        holder.tvProgress.setText(item.currentProgress + "/" + (item.totalProgress > 0 ? item.totalProgress : 100));

        // Status
        holder.tvStatus.setText(item.status != null ? item.status : "N/A");

        // Show/hide + button
        holder.btnIncreaseProgress.setVisibility(item.currentProgress >= item.totalProgress ? View.GONE : View.VISIBLE);

        // ---------------------- + BUTTON CLICK ----------------------
        holder.btnIncreaseProgress.setOnClickListener(v -> {
            if (item.currentProgress < item.totalProgress) {
                item.currentProgress++;

                // Update UI
                holder.progressBar.setProgress(item.currentProgress);
                holder.tvProgress.setText(item.currentProgress + "/" + item.totalProgress);

                // Determine new status
                String newStatus = item.currentProgress >= item.totalProgress ? "Completed" : "Watching";
                item.status = newStatus;
                holder.tvStatus.setText(newStatus);

                // Hide + button if completed
                if (item.currentProgress >= item.totalProgress) {
                    holder.btnIncreaseProgress.setVisibility(View.GONE);
                }

                // Update database
                executor.execute(() -> {
                    db.itemDao().update(item);
                    updateCalendarEventToday(item.title, newStatus);
                });
            }
        });

        // ---------------------- EDIT ----------------------
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditItemActivity.class);
            intent.putExtra("itemId", item.id);
            context.startActivity(intent);
        });

        // ---------------------- DELETE ----------------------
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        executor.execute(() -> {
                            db.itemDao().delete(item);
                            removeCalendarEventToday(item.title);

                            holder.itemView.post(() -> {
                                items.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ---------------------- HELPER: CALENDAR EVENTS ----------------------
    private void updateCalendarEventToday(String title, String status) {
        String today = dateFormat.format(Calendar.getInstance().getTime());
        CalendarEvent existing = db.calendarEventDao().getEventByTitleAndDate(title, today);
        if (existing != null) db.calendarEventDao().delete(existing);

        if (status.equals("Watching") || status.equals("Completed")) {
            CalendarEvent newEvent = new CalendarEvent(title, today, status);
            db.calendarEventDao().insert(newEvent);
        }
    }

    private void removeCalendarEventToday(String title) {
        String today = dateFormat.format(Calendar.getInstance().getTime());
        CalendarEvent event = db.calendarEventDao().getEventByTitleAndDate(title, today);
        if (event != null) db.calendarEventDao().delete(event);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvStatus, tvProgress;
        ProgressBar progressBar;
        ImageButton btnEdit, btnDelete, btnIncreaseProgress;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.itemTitle);
            tvCategory = itemView.findViewById(R.id.itemCategory);
            tvStatus = itemView.findViewById(R.id.itemStatus);
            tvProgress = itemView.findViewById(R.id.itemProgressText);
            progressBar = itemView.findViewById(R.id.itemProgress);
            btnEdit = itemView.findViewById(R.id.editItem);
            btnDelete = itemView.findViewById(R.id.deleteItem);
            btnIncreaseProgress = itemView.findViewById(R.id.btnIncreaseProgress);
        }
    }
}
