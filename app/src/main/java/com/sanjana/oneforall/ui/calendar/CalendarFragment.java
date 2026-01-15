package com.sanjana.oneforall.ui.calendar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.adapters.CalendarAdapter;
import com.sanjana.oneforall.adapters.DayCell;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.CalendarEvent;
import com.sanjana.oneforall.database.Item;
import com.sanjana.oneforall.R;
import com.sanjana.oneforall.ui.calendar.AddCalendarEventActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
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
            for (int i = 0; i < firstDayOffset; i++) cells.add(new DayCell(0));

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
                CalendarAdapter adapter = new CalendarAdapter(
                        getContext(),
                        cells,
                        this::showPopup,
                        this::onDragDrop
                );
                rvCalendar.setAdapter(adapter);
            });
        });
    }

    private void showPopup(DayCell cell) {
        if (cell.events.isEmpty()) return;

        View popupView = LayoutInflater.from(requireContext())
                .inflate(R.layout.popup_calendar_day, null);

        TextView tvPopupTitle = popupView.findViewById(R.id.tvPopupTitle);
        LinearLayout llEventList = popupView.findViewById(R.id.llEventList);
        ImageButton btnClose = popupView.findViewById(R.id.btnPopupClose);

        tvPopupTitle.setText("Events • " + cell.date);
        llEventList.removeAllViews();

        for (CalendarEvent e : cell.events) {
            LinearLayout eventItem = (LinearLayout) LayoutInflater.from(requireContext())
                    .inflate(R.layout.popup_calendar_day_item, llEventList, false);

            TextView tvTitle = eventItem.findViewById(R.id.tvEventTitle);
            TextView tvWatched = eventItem.findViewById(R.id.tvWatched);
            TextView tvRange = eventItem.findViewById(R.id.tvRange);
            ImageButton btnEdit = eventItem.findViewById(R.id.btnEditEvent);
            ImageButton btnDelete = eventItem.findViewById(R.id.btnDeleteEvent);

            tvTitle.setText(e.title);
            tvWatched.setText("Watched: " + e.episodeCount + " eps");
            tvRange.setText("Range: Ep " + e.startEp + " - " + e.endEp);

            btnEdit.setOnClickListener(v -> AddCalendarEventActivity.start(requireContext(), e.id));

            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete this event?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Executors.newSingleThreadExecutor().execute(() -> {
                                // 1️⃣ Delete the CalendarEvent
                                db.calendarEventDao().delete(e);

                                // 2️⃣ Reset Item's startDate and endDate if all events deleted
                                Item item = db.itemDao().getItemByTitle(e.title);
                                if (item != null) {
                                    List<CalendarEvent> remaining = db.calendarEventDao().getEventsByTitle(item.title);
                                    if (remaining.isEmpty()) {
                                        item.startDate = null;
                                        item.endDate = null;
                                        item.status = "Planned"; // reset status
                                        db.itemDao().update(item);
                                    }
                                }

                                requireActivity().runOnUiThread(() -> {
                                    llEventList.removeView(eventItem);
                                    loadMonth();
                                });
                            });
                        })
                        .setNegativeButton("No", null)
                        .show();
            });

            llEventList.addView(eventItem);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(popupView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void onDragDrop(CalendarAdapter.DragData data, DayCell targetCell) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.calendarEventDao().updateEventDate(data.event.id, targetCell.date);
            requireActivity().runOnUiThread(this::loadMonth);
        });
    }
}
