package com.sanjana.oneforall.adapters;

import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
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
    private final BiConsumer<DragData, DayCell> dragDropCallback;

    public CalendarAdapter(Context context, List<DayCell> dayCells,
                           Consumer<DayCell> clickCallback,
                           BiConsumer<DragData, DayCell> dragDropCallback) {
        this.context = context;
        this.dayCells = dayCells;
        this.clickCallback = clickCallback;
        this.dragDropCallback = dragDropCallback;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
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

        for (CalendarEvent e : cell.events) {
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(8, 6, 8, 6);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(8f);
            bg.setColor(0xFFE0F7FA);
            bg.setStroke(1, 0xFFB0B0B0);
            card.setBackground(bg);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.topMargin = 4;
            card.setLayoutParams(lp);

            TextView tvTitle = new TextView(context);
            tvTitle.setText(e.title);
            tvTitle.setTextColor(e.categoryColor);
            tvTitle.setTextSize(12f);
            tvTitle.setSingleLine(true);
            tvTitle.setEllipsize(TextUtils.TruncateAt.END);
            card.addView(tvTitle);

            card.setOnLongClickListener(v -> {
                DragData data = new DragData(e);
                ClipData clip = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(card);
                v.startDragAndDrop(clip, shadow, data, 0);
                return true;
            });

            holder.llEventContainer.addView(card);
        }

        holder.itemView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.DragEvent.ACTION_DROP:
                    if (event.getLocalState() instanceof DragData) {
                        DragData data = (DragData) event.getLocalState();
                        dragDropCallback.accept(data, cell);
                    }
                    return true;
            }
            return true;
        });

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

    public static class DragData {
        public final CalendarEvent event;

        public DragData(CalendarEvent e) {
            this.event = e;
        }
    }

    public interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
}
