package com.sanjana.oneforall.ui.list;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_list);

        spinnerFolder = findViewById(R.id.spinnerFolder);
        editTitle = findViewById(R.id.editListTitle);
        editContent = findViewById(R.id.editListContent);
        btnSave = findViewById(R.id.btnSave);
        btnAddFolder = findViewById(R.id.btnAddFolder);

        db = AppDatabase.getInstance(this);

        // Spinner
        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFolder.setAdapter(spinnerAdapter);

        loadFolders();

        int itemId = getIntent().getIntExtra("itemId", -1);
        if (itemId != -1) loadExistingItem(itemId);

        btnSave.setOnClickListener(v -> saveListItem());
        btnAddFolder.setOnClickListener(v -> showAddFolderDialog());
    }

    // ================= FOLDERS =================

    private void loadFolders() {
        new Thread(() -> {
            List<ListFolder> dbFolders = db.listFolderDao().getAll();
            runOnUiThread(() -> {
                folders.clear();
                folders.addAll(dbFolders);

                spinnerAdapter.clear();
                for (ListFolder f : folders) spinnerAdapter.add(f.name);
                spinnerAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showAddFolderDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Add Folder");

        EditText input = new EditText(this);
        input.setHint("Folder Name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter folder name", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                db.listFolderDao().insert(new ListFolder(name));
                loadFolders();
            }).start();
        });

        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        builder.show();
    }

    // ================= LOAD / SAVE =================

    private void loadExistingItem(int itemId) {
        new Thread(() -> {
            ListItem item = db.listItemDao().getById(itemId);
            if (item != null) {
                existingItem = item;
                runOnUiThread(() -> {
                    editTitle.setText(item.title);
                    editContent.setText(item.content);

                    for (int i = 0; i < folders.size(); i++) {
                        if (folders.get(i).id == item.folderId) {
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
        String content = editContent.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (folders.isEmpty()) {
            Toast.makeText(this, "Add a folder first", Toast.LENGTH_SHORT).show();
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
                db.listItemDao().insert(
                        new ListItem(folderId, title, content, System.currentTimeMillis())
                );
            }
            runOnUiThread(this::finish);
        }).start();
    }
}
