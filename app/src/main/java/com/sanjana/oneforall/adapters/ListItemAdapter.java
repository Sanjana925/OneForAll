package com.sanjana.oneforall.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.ListItem;
import com.sanjana.oneforall.ui.list.AddEditListActivity;

import java.util.List;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ListItemViewHolder> {

    private Context context;
    private List<ListItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDelete(ListItem item);
    }

    public ListItemAdapter(Context context, List<ListItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_card, parent, false);
        return new ListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {
        ListItem item = items.get(position);
        holder.tvTitle.setText(item.title);

        // ✅ Rich text preview
        Spanned spannedContent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spannedContent = Html.fromHtml(item.content, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spannedContent = Html.fromHtml(item.content);
        }
        holder.tvContent.setText(spannedContent);

        // Delete
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });

        // Click card → open edit page
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditListActivity.class);
            intent.putExtra("itemId", item.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItemToTop(ListItem item) {
        items.add(0, item);
        notifyItemInserted(0);
    }

    public void updateItem(ListItem item) {
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == item.id) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            items.set(index, item);
            notifyItemChanged(index);
        }
    }

    public void removeItem(ListItem item) {
        int index = items.indexOf(item);
        if (index != -1) {
            items.remove(index);
            notifyItemRemoved(index);
        }
    }

    static class ListItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        ImageButton btnDelete;

        public ListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvContent = itemView.findViewById(R.id.tvItemContent);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
