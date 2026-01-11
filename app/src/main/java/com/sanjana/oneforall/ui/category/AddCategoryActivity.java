package com.sanjana.oneforall.ui.category;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.Category;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddCategoryActivity extends AppCompatActivity {

    private EditText categoryNameInput;
    private Spinner colorSpinner;
    private Button addCategoryButton;
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        db = AppDatabase.getInstance(this);

        categoryNameInput = findViewById(R.id.categoryNameInput);
        colorSpinner = findViewById(R.id.colorSpinner);
        addCategoryButton = findViewById(R.id.addCategoryButton);

        // Spinner setup with 7 basic colors
        String[] colors = {"Red", "Blue", "Green", "Yellow", "Orange", "Purple", "Pink"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, colors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(adapter);

        // Handle add button click
        addCategoryButton.setOnClickListener(v -> {
            String name = categoryNameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter category name", Toast.LENGTH_SHORT).show();
                return;
            }

            int color = getColorFromName(colorSpinner.getSelectedItem().toString());
            Category category = new Category(name, color);

            // Insert in background thread
            executor.execute(() -> {
                db.categoryDao().insert(category);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show();
                    finish(); // close activity
                });
            });
        });
    }

    // Map color name to actual Color int
    private int getColorFromName(String colorName) {
        switch (colorName) {
            case "Red": return Color.RED;
            case "Blue": return Color.BLUE;
            case "Green": return Color.GREEN;
            case "Yellow": return Color.YELLOW;
            case "Orange": return 0xFFFFA500; // Color.ORANGE not available
            case "Purple": return 0xFF800080;
            case "Pink": return 0xFFFFC0CB;
            default: return Color.BLACK;
        }
    }
}
