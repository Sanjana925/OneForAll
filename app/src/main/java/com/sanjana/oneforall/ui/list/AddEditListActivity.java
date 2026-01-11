package com.sanjana.oneforall.ui.list;

import android.app.AlertDialog;
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
import com.sanjana.oneforall.database.ListFolder;
import com.sanjana.oneforall.database.ListItem;

import java.util.ArrayList;
import java.util.List;

public class AddEditListActivity extends AppCompatActivity {

    private Spinner spinnerFolder;
    private EditText editTitle, editContent;
    private Button btnSave, btnAddFolder;

    private AppDatabase db;
    private List<ListFolder> folders = new ArrayList<>();
    private ListItem existingItem;

    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_list);

        spinnerFolder = findViewById(R.id.spinnerFolder);
        editTitle = findViewById(R.id.editListTitle);
        editContent = findViewById(R.id.editListContent);
        btnSave = findViewById(R.id.btnSave);
        btnAddFolder = findViewById(R.id.btnAddFolder);

        db = AppDatabase.getInstance(this);

        // Initialize spinner adapter
        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFolder.setAdapter(spinnerAdapter);

        // Load folders from DB
        loadFolders();

        // Check if editing existing item
        int itemId = getIntent().getIntExtra("itemId", -1);
        if (itemId != -1) {
            loadExistingItem(itemId);
        }

        // Save button
        btnSave.setOnClickListener(v -> saveListItem());

        // Add Folder button
        btnAddFolder.setOnClickListener(v -> showAddFolderDialog());
    }

    private void loadFolders() {
        new Thread(() -> {
            List<ListFolder> dbFolders = db.listFolderDao().getAll();
            runOnUiThread(() -> {
                folders.clear();
                folders.addAll(dbFolders);

                spinnerAdapter.clear();
                for (ListFolder f : folders) {
                    spinnerAdapter.add(f.name);
                }
                spinnerAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Folder");

        EditText input = new EditText(this);
        input.setHint("Folder Name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String folderName = input.getText().toString().trim();
            if (folderName.isEmpty()) {
                Toast.makeText(this, "Enter folder name", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                ListFolder folder = new ListFolder(folderName);
                db.listFolderDao().insert(folder);

                // Reload folders and select the new one
                List<ListFolder> dbFolders = db.listFolderDao().getAll();
                runOnUiThread(() -> {
                    folders.clear();
                    folders.addAll(dbFolders);

                    spinnerAdapter.clear();
                    for (ListFolder f : folders) {
                        spinnerAdapter.add(f.name);
                    }
                    spinnerAdapter.notifyDataSetChanged();

                    // Select the newly added folder
                    spinnerFolder.setSelection(folders.size() - 1);

                    Toast.makeText(this, "Folder added!", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void loadExistingItem(int itemId) {
        new Thread(() -> {
            ListItem item = db.listItemDao().getById(itemId); // make sure DAO has getById()
            if (item != null) {
                existingItem = item;
                runOnUiThread(() -> {
                    editTitle.setText(existingItem.title);
                    editContent.setText(existingItem.content);

                    // Set spinner to item's folder
                    for (int i = 0; i < folders.size(); i++) {
                        if (folders.get(i).id == existingItem.folderId) {
                            spinnerFolder.setSelection(i);
                            break;
                        }
                    }
                });
            }
        }).start();
    }

    private void saveListItem() {
        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (folders.isEmpty()) {
            Toast.makeText(this, "Add at least one folder", Toast.LENGTH_SHORT).show();
            return;
        }

        int folderId = folders.get(spinnerFolder.getSelectedItemPosition()).id;

        new Thread(() -> {
            if (existingItem != null) {
                existingItem.title = title;
                existingItem.content = content;
                existingItem.folderId = folderId;
                db.listItemDao().update(existingItem);
            } else {
                ListItem item = new ListItem(folderId, title, content, System.currentTimeMillis());
                db.listItemDao().insert(item);
            }

            runOnUiThread(this::finish);
        }).start();
    }
}
