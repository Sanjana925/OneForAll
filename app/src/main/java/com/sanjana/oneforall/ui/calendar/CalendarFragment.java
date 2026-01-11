package com.sanjana.oneforall.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.adapters.DailyEventsAdapter;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.CalendarEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvEvents;
    private DailyEventsAdapter adapter;
    private List<String> eventTitles = new ArrayList<>();
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        rvEvents = view.findViewById(R.id.rvEvents);

        db = AppDatabase.getInstance(requireContext());

        setupRecyclerView();
        loadEventsForDate(new Date(calendarView.getDate())); // load initial events

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            loadEventsForDate(selected.getTime());
        });

        return view;
    }

    private void setupRecyclerView() {
        adapter = new DailyEventsAdapter(eventTitles);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEvents.setAdapter(adapter);
    }

    private void loadEventsForDate(Date date) {
        String dateStr = dateFormat.format(date);
        executor.execute(() -> {
            List<CalendarEvent> events = db.calendarEventDao().getEventsByDate(dateStr);

            eventTitles.clear();
            for (CalendarEvent e : events) {
                eventTitles.add(e.title + " (" + e.status + ")");
            }

            requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
        });
    }
}
