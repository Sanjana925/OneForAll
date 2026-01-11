package com.sanjana.oneforall.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.Category;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private Context context;
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
        db = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        // Display category name in its color
        holder.categoryName.setText(category.name);
        holder.categoryName.setTextColor(category.color != 0 ? category.color : Color.BLACK);

        // Edit button
        holder.editButton.setOnClickListener(v -> showEditDialog(category, holder));

        // Delete button
        holder.deleteButton.setOnClickListener(v -> executor.execute(() -> {
            db.categoryDao().delete(category);

            // Update UI on main thread safely
            holder.itemView.post(() -> {
                categories.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
            });
        }));
    }

    private void showEditDialog(Category category, CategoryViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Category");

        EditText input = new EditText(context);
        input.setText(category.name);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                category.name = newName;

                // Update in background thread
                executor.execute(() -> {
                    db.categoryDao().update(category);

                    // Notify adapter on main thread safely
                    holder.itemView.post(this::notifyDataSetChanged);
                });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageButton editButton, deleteButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            editButton = itemView.findViewById(R.id.editCategory);
            deleteButton = itemView.findViewById(R.id.deleteCategory);
        }
    }
}
