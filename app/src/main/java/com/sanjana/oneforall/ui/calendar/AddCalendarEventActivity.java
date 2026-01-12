package com.sanjana.oneforall.ui.calendar;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.CalendarEvent;
import com.sanjana.oneforall.database.Category;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddCalendarEventActivity extends AppCompatActivity {

    private EditText editTitle, editEpisodes, editStartEp, editEndEp, editDate;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel;

    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private List<Category> categories = new ArrayList<>();
    private int selectedCategoryColor = 0xFF2196F3;

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

        editDate.setText(LocalDate.now().toString());
        editDate.setOnClickListener(v -> showDatePicker());

        loadCategories();

        btnSave.setOnClickListener(v -> saveEvent());
        btnCancel.setOnClickListener(v -> finish());
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
                    public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                        selectedCategoryColor = categories.get(position).color;
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) { }
                });
            });
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new android.app.DatePickerDialog(this,
                (v, y, m, d) -> editDate.setText(String.format("%04d-%02d-%02d", y, m+1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void saveEvent() {
        String title = editTitle.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        int episodes = parseInt(editEpisodes.getText().toString());
        int startEp = parseInt(editStartEp.getText().toString());
        int endEp = parseInt(editEndEp.getText().toString());

        if (title.isEmpty() || date.isEmpty() || episodes <= 0 || startEp <= 0 || endEp < startEp) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
            return;
        }

        CalendarEvent event = new CalendarEvent(title, date, episodes, startEp, endEp, selectedCategoryColor);

        executor.execute(() -> {
            db.calendarEventDao().insert(event);
            runOnUiThread(this::finish);
        });
    }

    private int parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }
}
