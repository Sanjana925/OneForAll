package com.sanjana.oneforall.ui.home;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.CalendarEvent;
import com.sanjana.oneforall.database.Category;
import com.sanjana.oneforall.database.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditItemActivity extends AppCompatActivity {

    private EditText editTitle, editCurrent, editTotal, editStart, editEnd, editScore, editNotes;
    private Spinner spinnerCategory;
    private Button btnWatching, btnCompleted, btnOnHold, btnDropped, btnPlan;
    private Button btnSave, btnDelete;
    private ImageButton btnIncreaseProgress, btnDecreaseProgress;
    private ImageButton btnStartDate, btnEndDate;
    private ProgressBar progressBarEdit;

    private AppDatabase db;
    private List<Category> categories = new ArrayList<>();
    private int selectedCategoryId;
    private String selectedStatus = "Watching";

    private boolean isEditMode = false;
    private Item existingItem;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        db = AppDatabase.getInstance(this);

        // Views
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

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        btnIncreaseProgress = findViewById(R.id.btnIncreaseProgress);
        btnDecreaseProgress = findViewById(R.id.btnDecreaseProgress);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        progressBarEdit = findViewById(R.id.itemProgressBar);

        setupStatusButtons();
        setupProgressButtons();
        setupDatePickers();
        loadCategories();

        if (getIntent().hasExtra("itemId")) {
            int id = getIntent().getIntExtra("itemId", -1);
            loadItem(id);
            isEditMode = true;
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        btnSave.setOnClickListener(v -> saveItem());
        btnDelete.setOnClickListener(v -> deleteItem());
    }

    // ---------------------- STATUS BUTTONS ----------------------
    private void setupStatusButtons() {
        View.OnClickListener statusClick = v -> {
            btnWatching.setSelected(false);
            btnCompleted.setSelected(false);
            btnOnHold.setSelected(false);
            btnDropped.setSelected(false);
            btnPlan.setSelected(false);

            v.setSelected(true);

            if (v == btnWatching) selectedStatus = "Watching";
            else if (v == btnCompleted) selectedStatus = "Completed";
            else if (v == btnOnHold) selectedStatus = "On-Hold";
            else if (v == btnDropped) selectedStatus = "Dropped";
            else if (v == btnPlan) selectedStatus = "Plan to Watch";

            // Update calendar for current date
            addOrUpdateCalendarEvent(selectedStatus);
        };

        btnWatching.setOnClickListener(statusClick);
        btnCompleted.setOnClickListener(statusClick);
        btnOnHold.setOnClickListener(statusClick);
        btnDropped.setOnClickListener(statusClick);
        btnPlan.setOnClickListener(statusClick);
    }

    // ---------------------- PROGRESS ----------------------
    private void setupProgressButtons() {
        btnIncreaseProgress.setOnClickListener(v -> changeProgress(true));
        btnDecreaseProgress.setOnClickListener(v -> changeProgress(false));
    }

    private void changeProgress(boolean increase) {
        int current = parseInt(editCurrent.getText().toString().trim());
        int total = parseInt(editTotal.getText().toString().trim());

        if (increase && current < total) current++;
        if (!increase && current > 0) current--;

        editCurrent.setText(String.valueOf(current));
        progressBarEdit.setProgress(current);

        String newStatus;
        if (current >= total) {
            newStatus = "Completed";
        } else if (current > 0) {
            newStatus = "Watching";
        } else {
            newStatus = null;
        }

        if (newStatus != null) {
            selectedStatus = newStatus;
            updateStatusButtons(newStatus);
            addOrUpdateCalendarEvent(newStatus); // Use current date
        } else {
            removeCalendarEvent(); // Remove for current date
        }
    }

    private void updateStatusButtons(String status) {
        btnWatching.setSelected(false);
        btnCompleted.setSelected(false);
        btnOnHold.setSelected(false);
        btnDropped.setSelected(false);
        btnPlan.setSelected(false);

        switch (status) {
            case "Watching": btnWatching.setSelected(true); break;
            case "Completed": btnCompleted.setSelected(true); break;
            case "On-Hold": btnOnHold.setSelected(true); break;
            case "Dropped": btnDropped.setSelected(true); break;
            case "Plan to Watch": btnPlan.setSelected(true); break;
        }
    }

    // ---------------------- DATE PICKERS ----------------------
    private void setupDatePickers() {
        btnStartDate.setOnClickListener(v -> showDatePicker(editStart));
        btnEndDate.setOnClickListener(v -> showDatePicker(editEnd));
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> editText.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                y, m, d);
        dialog.show();
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

                if (!categories.isEmpty()) selectedCategoryId = categories.get(0).id;

                spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedCategoryId = categories.get(position).id;
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {
                        if (!categories.isEmpty()) selectedCategoryId = categories.get(0).id;
                    }
                });

                if (isEditMode && existingItem != null) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).id == existingItem.categoryId) {
                            spinnerCategory.setSelection(i);
                            selectedCategoryId = existingItem.categoryId;
                            break;
                        }
                    }
                }
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

    private void fillData(Item item) {
        editTitle.setText(item.title);
        editCurrent.setText(String.valueOf(item.currentProgress));
        editTotal.setText(String.valueOf(item.totalProgress));
        editStart.setText(item.startDate);
        editEnd.setText(item.endDate);
        editScore.setText(String.valueOf(item.score));
        editNotes.setText(item.notes);

        progressBarEdit.setMax(item.totalProgress);
        progressBarEdit.setProgress(item.currentProgress);

        updateStatusButtons(item.status);
        selectedStatus = item.status;
    }

    // ---------------------- SAVE / DELETE ----------------------
    private void saveItem() {
        String title = editTitle.getText().toString().trim();
        if (title.isEmpty()) { Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show(); return; }

        int current = parseInt(editCurrent.getText().toString().trim());
        int total = parseInt(editTotal.getText().toString().trim());
        int score = parseInt(editScore.getText().toString().trim());
        String start = editStart.getText().toString().trim();
        String end = editEnd.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();

        Item item = new Item(title, selectedCategoryId, selectedStatus, current, total, start, end, score, notes);

        executor.execute(() -> {
            if (isEditMode) {
                item.id = existingItem.id;
                db.itemDao().update(item);
                removeCalendarEvent(); // Remove old event for today
            } else {
                db.itemDao().insert(item);
            }

            if (selectedStatus.equals("Watching") || selectedStatus.equals("Completed")) {
                addOrUpdateCalendarEvent(selectedStatus); // For today
            }

            runOnUiThread(this::finish);
        });
    }

    private void deleteItem() {
        if (existingItem != null) {
            executor.execute(() -> {
                db.itemDao().delete(existingItem);
                removeCalendarEvent(); // Remove today
                runOnUiThread(this::finish);
            });
        }
    }

    // ---------------------- HELPER: PARSE ----------------------
    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    // ---------------------- CALENDAR HELPERS ----------------------
    private void addOrUpdateCalendarEvent(String status) {
        String title = editTitle.getText().toString().trim();
        if (title.isEmpty()) return;

        String today = dateFormat.format(Calendar.getInstance().getTime());

        executor.execute(() -> {
            CalendarEvent existing = db.calendarEventDao().getEventByTitleAndDate(title, today);
            if (existing != null) db.calendarEventDao().delete(existing);

            if (status.equals("Watching") || status.equals("Completed")) {
                CalendarEvent newEvent = new CalendarEvent(title, today, status);
                db.calendarEventDao().insert(newEvent);
            }
        });
    }

    private void removeCalendarEvent() {
        String title = editTitle.getText().toString().trim();
        if (title.isEmpty()) return;

        String today = dateFormat.format(Calendar.getInstance().getTime());

        executor.execute(() -> {
            CalendarEvent event = db.calendarEventDao().getEventByTitleAndDate(title, today);
            if (event != null) db.calendarEventDao().delete(event);
        });
    }
}
