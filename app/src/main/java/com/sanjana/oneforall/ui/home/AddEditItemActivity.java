package com.sanjana.oneforall.ui.home;

import android.app.DatePickerDialog;
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

public class AddEditItemActivity extends AppCompatActivity {

    private EditText editTitle, editCurrent, editTotal, editStart, editEnd, editScore, editNotes;
    private Spinner spinnerCategory;
    private Button btnWatching, btnCompleted, btnOnHold, btnDropped, btnPlan;
    private Button btnSave, btnDelete;
    private ImageButton btnIncreaseProgress, btnDecreaseProgress, btnStartDate, btnEndDate;
    private ProgressBar progressBar;

    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private List<Category> categories = new ArrayList<>();
    private int selectedCategoryId;
    private String selectedStatus = "Plan to Watch";

    private boolean isEditMode = false;
    private Item existingItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        db = AppDatabase.getInstance(this);

        bindViews();
        setupStatusButtons();
        setupProgressButtons();
        setupDatePickers();
        loadCategories();

        if (getIntent().hasExtra("itemId")) {
            isEditMode = true;
            btnDelete.setVisibility(View.VISIBLE);
            loadItem(getIntent().getIntExtra("itemId", -1));
        }

        btnSave.setOnClickListener(v -> saveItem());
        btnDelete.setOnClickListener(v -> deleteItem());
    }

    // ---------------------- BIND ----------------------
    private void bindViews() {
        editTitle = findViewById(R.id.editItemTitle);
        editCurrent = findViewById(R.id.editCurrentProgress);
        editTotal = findViewById(R.id.editTotalProgress);
        editStart = findViewById(R.id.editStartDate);
        editEnd = findViewById(R.id.editEndDate);
        editScore = findViewById(R.id.editScore);
        editNotes = findViewById(R.id.editNotes);

        spinnerCategory = findViewById(R.id.spinnerCategory);

        btnWatching = findViewById(R.id.btnWatching);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnOnHold = findViewById(R.id.btnOnHold);
        btnDropped = findViewById(R.id.btnDropped);
        btnPlan = findViewById(R.id.btnPlan);

        btnIncreaseProgress = findViewById(R.id.btnIncreaseProgress);
        btnDecreaseProgress = findViewById(R.id.btnDecreaseProgress);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        progressBar = findViewById(R.id.itemProgressBar);
    }

    // ---------------------- STATUS ----------------------
    private void setupStatusButtons() {
        View.OnClickListener l = v -> {
            clearStatusSelection();
            v.setSelected(true);

            if (v == btnWatching) selectedStatus = "Watching";
            else if (v == btnCompleted) selectedStatus = "Completed";
            else if (v == btnOnHold) selectedStatus = "On-Hold";
            else if (v == btnDropped) selectedStatus = "Dropped";
            else selectedStatus = "Plan to Watch";
        };

        btnWatching.setOnClickListener(l);
        btnCompleted.setOnClickListener(l);
        btnOnHold.setOnClickListener(l);
        btnDropped.setOnClickListener(l);
        btnPlan.setOnClickListener(l);
    }

    private void clearStatusSelection() {
        btnWatching.setSelected(false);
        btnCompleted.setSelected(false);
        btnOnHold.setSelected(false);
        btnDropped.setSelected(false);
        btnPlan.setSelected(false);
    }

    // ---------------------- PROGRESS ----------------------
    private void setupProgressButtons() {
        btnIncreaseProgress.setOnClickListener(v -> changeProgress(true));
        btnDecreaseProgress.setOnClickListener(v -> changeProgress(false));
    }

    private void changeProgress(boolean increase) {
        int current = parseInt(editCurrent.getText().toString());
        int total = parseInt(editTotal.getText().toString());
        int old = current;

        if (increase && current < total) current++;
        if (!increase && current > 0) current--;

        editCurrent.setText(String.valueOf(current));
        progressBar.setMax(total);
        progressBar.setProgress(current);

        String title = editTitle.getText().toString().trim();
        String today = LocalDate.now().toString();

        // ------------------ STARTED ------------------
        if (old == 0 && current == 1) {
            String start = editStart.getText().toString().trim();
            if (start.isEmpty()) start = today;
            editStart.setText(start);
            addCalendarEvent(start, title + " (Started)", "Started");
            selectedStatus = "Watching";
        }

        // ------------------ DAILY PROGRESS ------------------
        if (current > 0 && current < total) {
            final int finalCurrent = current;
            executor.execute(() -> {
                int itemId = isEditMode ? existingItem.id : 0;
                DailyProgress dp = db.dailyProgressDao().getByItemAndDate(itemId, today);
                if (dp == null) {
                    dp = new DailyProgress(itemId, today, finalCurrent, finalCurrent);
                    db.dailyProgressDao().insert(dp);
                } else {
                    dp.lastEp = finalCurrent;
                    if (dp.firstEp > finalCurrent) dp.firstEp = finalCurrent;
                    db.dailyProgressDao().update(dp);
                }

                // ------------------ WATCHING EVENT ------------------
                if (dp.firstEp > 0) {
                    addOrUpdateWatchingEvent(today, title, dp.firstEp, dp.lastEp);
                }
            });

            selectedStatus = "Watching";
        }

        // ------------------ ENDED ------------------
        if (current == total && total > 0) {
            removeWatchingEventByPrefix(today, title);
            addCalendarEvent(today, title + " (Ended)", "Completed");
            selectedStatus = "Completed";
        }

        // ------------------ DECREASE ------------------
        if (!increase) {
            if (old == total && current < total) removeCalendarEvent(today, title + " (Ended)");
            if (current == 0) {
                String start = editStart.getText().toString().trim();
                removeCalendarEvent(start, title + " (Started)");
                removeWatchingEventByPrefix(today, title);
                selectedStatus = "Plan to Watch";
            }
        }

        updateStatusButtons(selectedStatus);
    }

    private void updateStatusButtons(String status) {
        clearStatusSelection();
        switch (status) {
            case "Watching": btnWatching.setSelected(true); break;
            case "Completed": btnCompleted.setSelected(true); break;
            case "On-Hold": btnOnHold.setSelected(true); break;
            case "Dropped": btnDropped.setSelected(true); break;
            default: btnPlan.setSelected(true);
        }
    }

    // ---------------------- DATES ----------------------
    private void setupDatePickers() {
        btnStartDate.setOnClickListener(v -> showDatePicker(editStart));
        btnEndDate.setOnClickListener(v -> showDatePicker(editEnd));
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (v, y, m, d) -> target.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ---------------------- CATEGORIES ----------------------
    private void loadCategories() {
        executor.execute(() -> {
            categories = db.categoryDao().getAllCategories();
            runOnUiThread(() -> {
                List<String> names = new ArrayList<>();
                for (Category c : categories) names.add(c.name);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);

                spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(AdapterView<?> p, View v, int i, long l) { selectedCategoryId = categories.get(i).id; }
                    @Override public void onNothingSelected(AdapterView<?> p) {}
                });
            });
        });
    }

    // ---------------------- LOAD ITEM ----------------------
    private void loadItem(int id) {
        executor.execute(() -> {
            existingItem = db.itemDao().getItemById(id);
            if (existingItem != null) runOnUiThread(() -> fillData(existingItem));
        });
    }

    private void fillData(Item i) {
        editTitle.setText(i.title);
        editCurrent.setText(String.valueOf(i.currentProgress));
        editTotal.setText(String.valueOf(i.totalProgress));
        editStart.setText(i.startDate);
        editEnd.setText(i.endDate);
        editScore.setText(String.valueOf(i.score));
        editNotes.setText(i.notes);

        progressBar.setMax(i.totalProgress);
        progressBar.setProgress(i.currentProgress);

        selectedStatus = i.status;
        updateStatusButtons(i.status);
    }

    // ---------------------- SAVE ----------------------
    private void saveItem() {
        String title = editTitle.getText().toString().trim();
        if (title.isEmpty()) { Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show(); return; }

        int current = parseInt(editCurrent.getText().toString());
        int total = parseInt(editTotal.getText().toString());

        Item item = new Item(title, selectedCategoryId, selectedStatus, current, total,
                editStart.getText().toString().trim(), editEnd.getText().toString().trim(),
                parseInt(editScore.getText().toString()), editNotes.getText().toString().trim());

        executor.execute(() -> {
            if (isEditMode) {
                item.id = existingItem.id;
                db.itemDao().update(item);
            } else {
                long id = db.itemDao().insert(item);
                item.id = (int) id;
            }

            // Calendar events
            if (current > 0) addCalendarEvent(item.startDate, title + " (Started)", "Started");
            if (current >= total && total > 0) addCalendarEvent(item.endDate, title + " (Ended)", "Completed");

            runOnUiThread(this::finish);
        });
    }

    // ---------------------- DELETE ----------------------
    private void deleteItem() {
        if (existingItem == null) return;

        executor.execute(() -> {
            db.itemDao().delete(existingItem);
            removeCalendarEvent(existingItem.startDate, existingItem.title + " (Started)");
            removeCalendarEvent(existingItem.endDate, existingItem.title + " (Ended)");
            removeWatchingEventByPrefix(LocalDate.now().toString(), existingItem.title);
            runOnUiThread(this::finish);
        });
    }

    // ---------------------- CALENDAR ----------------------
    private void addOrUpdateWatchingEvent(String date, String title, int firstEp, int lastEp) {
        if (date == null || date.isEmpty()) return;

        String prefix = title + " (Watching)";
        CalendarEvent existing = db.calendarEventDao().getEventByTitleAndDatePrefix(prefix, date);

        String newTitle = prefix + " " + (lastEp - firstEp + 1) + " eps (" + firstEp + "-" + lastEp + ")";

        if (existing == null) {
            db.calendarEventDao().insert(new CalendarEvent(newTitle, date, "Watching"));
        } else {
            existing.title = newTitle;
            db.calendarEventDao().update(existing);
        }
    }

    private void addCalendarEvent(String date, String title, String status) {
        if (date == null || date.isEmpty()) return;
        CalendarEvent existing = db.calendarEventDao().getEventByTitleAndDate(title, date);
        if (existing == null) db.calendarEventDao().insert(new CalendarEvent(title, date, status));
    }

    private void removeCalendarEvent(String date, String title) {
        if (date == null || date.isEmpty()) return;
        CalendarEvent e = db.calendarEventDao().getEventByTitleAndDate(title, date);
        if (e != null) db.calendarEventDao().delete(e);
    }

    private void removeWatchingEventByPrefix(String date, String title) {
        if (date == null || date.isEmpty()) return;
        String prefix = title + " (Watching)";
        CalendarEvent e = db.calendarEventDao().getEventByTitleAndDatePrefix(prefix, date);
        if (e != null) db.calendarEventDao().delete(e);
    }

    private int parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }
}
