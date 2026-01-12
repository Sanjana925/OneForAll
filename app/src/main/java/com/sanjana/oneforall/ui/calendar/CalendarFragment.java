package com.sanjana.oneforall.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.adapters.CalendarAdapter;
import com.sanjana.oneforall.adapters.DayCell;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.CalendarEvent;
import com.sanjana.oneforall.database.DailyProgress;
import com.sanjana.oneforall.R;

import android.graphics.drawable.GradientDrawable;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class CalendarFragment extends Fragment {

    private AppDatabase db;
    private RecyclerView rvCalendar;
    private TextView tvMonth;
    private Calendar currentMonth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        rvCalendar = v.findViewById(R.id.rvCalendar);
        rvCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));

        tvMonth = v.findViewById(R.id.tvMonth);
        db = AppDatabase.getInstance(requireContext());
        currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

        loadMonth();

        v.findViewById(R.id.btnPrev).setOnClickListener(btn -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadMonth();
        });

        v.findViewById(R.id.btnNext).setOnClickListener(btn -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadMonth();
        });

        return v;
    }

    public void loadMonth() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Calendar cal = (Calendar) currentMonth.clone();
            int firstDayOffset = cal.get(Calendar.DAY_OF_WEEK) - 1;
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            List<DayCell> cells = new ArrayList<>();
            for (int i = 0; i < firstDayOffset; i++) {
                cells.add(new DayCell(0));
            }

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            for (int day = 1; day <= daysInMonth; day++) {
                cal.set(Calendar.DAY_OF_MONTH, day);
                String date = df.format(cal.getTime());

                DayCell cell = new DayCell(day);
                List<CalendarEvent> events = db.calendarEventDao().getEventsByDate(date);
                cell.events.addAll(events);
                cell.date = date;

                cells.add(cell);
            }

            String monthStr = new SimpleDateFormat("MMMM yyyy", Locale.US).format(currentMonth.getTime());

            requireActivity().runOnUiThread(() -> {
                tvMonth.setText(monthStr);
                rvCalendar.setAdapter(new CalendarAdapter(getContext(), cells, this::showPopup));
            });
        });
    }

    private void showPopup(DayCell cell) {
        if (cell.events.isEmpty()) return;

        Executors.newSingleThreadExecutor().execute(() -> {

            LinearLayout root = new LinearLayout(requireContext());
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(16,16,16,16);

            for (CalendarEvent e : cell.events) {

                LinearLayout card = new LinearLayout(requireContext());
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(14,14,14,14);

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(8f);
                bg.setColor(0xFFFFFFFF);
                bg.setStroke(1, 0xFFDDDDDD);
                card.setBackground(bg);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.topMargin = 10;
                card.setLayoutParams(lp);

                TextView tvTitle = new TextView(requireContext());
                tvTitle.setText(e.title);
                tvTitle.setTextSize(16f);
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTitle.setTextColor(e.categoryColor); // ✅ category color applied

                TextView tvWatched = new TextView(requireContext());
                tvWatched.setTextSize(14f);
                tvWatched.setTextColor(e.categoryColor); // ✅ category color applied

                TextView tvRange = new TextView(requireContext());
                tvRange.setText("Range: Ep " + e.startEp + " - " + e.endEp);
                tvRange.setTextSize(14f);
                tvRange.setTextColor(e.categoryColor); // ✅ category color applied

                card.addView(tvTitle);
                card.addView(tvWatched);
                card.addView(tvRange);

                root.addView(card);
            }

            requireActivity().runOnUiThread(() -> {
                ScrollView sv = new ScrollView(requireContext());
                sv.addView(root);
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Details • " + cell.date)
                        .setView(sv)
                        .setPositiveButton("Close", null)
                        .show();
            });
        });
    }
}
