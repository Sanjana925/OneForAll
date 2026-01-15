package com.sanjana.oneforall.ui.category;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.Category;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditCategoryActivity extends AppCompatActivity {

    private EditText categoryNameInput;
    private Spinner colorSpinner;
    private View colorPreview;
    private Button saveCategoryButton;
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Category existingCategory;
    private int selectedColor = Color.BLACK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_category);

        categoryNameInput = findViewById(R.id.categoryNameInput);
        colorSpinner = findViewById(R.id.colorSpinner);
        colorPreview = findViewById(R.id.colorPreview);
        saveCategoryButton = findViewById(R.id.saveCategoryButton);

        db = AppDatabase.getInstance(this);

        String[] colors = {"Red", "Blue", "Green", "Yellow", "Orange", "Purple", "Pink"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, colors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(adapter);

        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedColor = getColorFromName(colors[position]);
                colorPreview.setBackgroundColor(selectedColor);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        int categoryId = getIntent().getIntExtra("categoryId", -1);
        if (categoryId != -1) loadExistingCategory(categoryId);

        saveCategoryButton.setOnClickListener(v -> saveCategory());
    }

    private void loadExistingCategory(int id) {
        executor.execute(() -> {
            Category cat = db.categoryDao().getCategoryById(id);
            if (cat == null) return;

            existingCategory = cat;

            runOnUiThread(() -> {
                categoryNameInput.setText(cat.name);

                String colorName = getColorNameFromValue(cat.color);
                if (colorName != null) {
                    for (int i = 0; i < colorSpinner.getCount(); i++) {
                        if (colorSpinner.getItemAtPosition(i).toString().equals(colorName)) {
                            colorSpinner.setSelection(i);
                            break;
                        }
                    }
                }

                saveCategoryButton.setText("Update");
                findViewById(R.id.tvCategoryTitle).setTag("Update Category");
            });
        });
    }

    private void saveCategory() {
        String name = categoryNameInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter category name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingCategory != null) {
            existingCategory.name = name;
            existingCategory.color = selectedColor;

            executor.execute(() -> {
                db.categoryDao().update(existingCategory);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Category updated!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            Category category = new Category(name, selectedColor);
            executor.execute(() -> {
                db.categoryDao().insert(category);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }

    private int getColorFromName(String colorName) {
        switch (colorName) {
            case "Red": return Color.RED;
            case "Blue": return Color.BLUE;
            case "Green": return Color.GREEN;
            case "Yellow": return Color.YELLOW;
            case "Orange": return 0xFFFFA500;
            case "Purple": return 0xFF800080;
            case "Pink": return 0xFFFFC0CB;
            default: return Color.BLACK;
        }
    }

    private String getColorNameFromValue(int color) {
        if (color == Color.RED) return "Red";
        if (color == Color.BLUE) return "Blue";
        if (color == Color.GREEN) return "Green";
        if (color == Color.YELLOW) return "Yellow";
        if (color == 0xFFFFA500) return "Orange";
        if (color == 0xFF800080) return "Purple";
        if (color == 0xFFFFC0CB) return "Pink";
        return null;
    }
}
