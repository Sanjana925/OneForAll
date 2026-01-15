package com.sanjana.oneforall.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.ListFolder;

import java.util.List;

public class FolderPillAdapter extends RecyclerView.Adapter<FolderPillAdapter.PillViewHolder> {

    public interface OnFolderSelectListener {
        void onFolderSelected(ListFolder folder);
    }

    private Context context;
    private List<ListFolder> folders;
    private int selectedPosition = 0;
    private OnFolderSelectListener listener;

    public FolderPillAdapter(Context context, List<ListFolder> folders, OnFolderSelectListener listener) {
        this.context = context;
        this.folders = folders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_folder_pill, parent, false);
        return new PillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PillViewHolder holder, int position) {
        ListFolder folder = folders.get(position);
        holder.tvFolderName.setText(folder.name);
        holder.itemView.setSelected(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onFolderSelected(folder);
        });

        holder.itemView.setOnLongClickListener(v -> true);
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    static class PillViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderName;

        public PillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
        }
    }
}
