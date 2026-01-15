package com.sanjana.oneforall.ui.calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddCalendarEventActivity extends AppCompatActivity {

    private EditText editTitle, editEpisodes, editStartEp, editEndEp, editDate;
    private Spinner spinnerCategory;
    private Button btnSave, btnDelete, btnCancel;

    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private List<Category> categories = new ArrayList<>();
    private int selectedCategoryColor = 0xFF2196F3;

    private CalendarEvent existingEvent; // null if adding new

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calendar_event);

        db = AppDatabase.getInstance(this);

        editTitle = findViewById(R.id.editTitle);
        editEpisodes = findViewById(R.id.editEpisodes);
        editStartEp = findViewById(R.id.editStartEp);
        editEndEp = findViewById(R.id.editEndEp);
        editDate = findViewById(R.id.editDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnDelete = findViewById(R.id.btnDelete);

        editDate.setText(LocalDate.now().toString());
        editDate.setOnClickListener(v -> showDatePicker());

        loadCategories();

        int eventId = getIntent().getIntExtra("calendarEventId", -1);
        if (eventId != -1) loadExistingEvent(eventId);
        else btnDelete.setVisibility(View.GONE);

        btnSave.setOnClickListener(v -> saveEvent());
        btnCancel.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> deleteEvent());
    }

    public static void start(Context context, int calendarEventId) {
        Intent intent = new Intent(context, AddCalendarEventActivity.class);
        intent.putExtra("calendarEventId", calendarEventId);
        context.startActivity(intent);
    }

    private void loadCategories() {
        executor.execute(() -> {
            categories = db.categoryDao().getAllCategories();
            runOnUiThread(() -> {
                List<String> names = new ArrayList<>();
                for (Category c : categories) names.add(c.name);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);

                spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedCategoryColor = categories.get(position).color;
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) { }
                });
            });
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (v, y, m, d) -> editDate.setText(String.format("%04d-%02d-%02d", y, m+1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void loadExistingEvent(int id) {
        executor.execute(() -> {
            CalendarEvent e = db.calendarEventDao().getEventById(id);
            if (e != null) {
                existingEvent = e;
                runOnUiThread(() -> {
                    editTitle.setText(e.title);
                    editDate.setText(e.date);
                    editEpisodes.setText(String.valueOf(e.episodeCount));
                    editStartEp.setText(String.valueOf(e.startEp));
                    editEndEp.setText(String.valueOf(e.endEp));
                    for (int i=0;i<categories.size();i++) {
                        if (categories.get(i).color == e.categoryColor) {
                            spinnerCategory.setSelection(i); break;
                        }
                    }
                    btnDelete.setVisibility(View.VISIBLE);
                    btnSave.setText("Update");
                });
            }
        });
    }

    private void saveEvent() {
        String title = editTitle.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        int episodes = parseInt(editEpisodes.getText().toString());
        int startEp = parseInt(editStartEp.getText().toString());
        int endEp = parseInt(editEndEp.getText().toString());

        if (title.isEmpty() || date.isEmpty() || episodes<=0 || startEp<=0 || endEp<startEp) {
            Toast.makeText(this,"Please fill all fields correctly",Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingEvent!=null) {
            existingEvent.title=title;
            existingEvent.date=date;
            existingEvent.episodeCount=episodes;
            existingEvent.startEp=startEp;
            existingEvent.endEp=endEp;
            existingEvent.categoryColor=selectedCategoryColor;

            executor.execute(() -> {
                db.calendarEventDao().update(existingEvent);
                runOnUiThread(this::finish);
            });
        } else {
            CalendarEvent event = new CalendarEvent(title,date,episodes,startEp,endEp,selectedCategoryColor);
            executor.execute(() -> {
                db.calendarEventDao().insert(event);
                runOnUiThread(this::finish);
            });
        }
    }

    private void deleteEvent() {
        if (existingEvent==null) return;
        executor.execute(() -> {
            db.calendarEventDao().delete(existingEvent);
            runOnUiThread(this::finish);
        });
    }

    private int parseInt(String s) { try { return Integer.parseInt(s); } catch(Exception e){return 0;} }
}
