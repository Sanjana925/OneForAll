package com.sanjana.oneforall.ui.list;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.database.*;

import java.util.ArrayList;
import java.util.List;

public class AddEditListActivity extends AppCompatActivity {

    private Spinner spinnerFolder;
    private EditText editTitle, editContent;
    private Button btnSave;

    private ImageButton btnBold, btnItalic, btnUnderline, btnBullet, btnTask, btnNumbered;

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

        btnBold = findViewById(R.id.btnBold);
        btnItalic = findViewById(R.id.btnItalic);
        btnUnderline = findViewById(R.id.btnUnderline);
        btnBullet = findViewById(R.id.btnBullet);
        btnTask = findViewById(R.id.btnTask);
        btnNumbered = findViewById(R.id.btnNumbered);

        db = AppDatabase.getInstance(this);

        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFolder.setAdapter(spinnerAdapter);

        loadFolders();

        int itemId = getIntent().getIntExtra("itemId", -1);
        if (itemId != -1) loadExistingItem(itemId);

        btnSave.setOnClickListener(v -> saveItem());

        // Formatting buttons
        btnBold.setOnClickListener(v -> applyStyle(Typeface.BOLD));
        btnItalic.setOnClickListener(v -> applyStyle(Typeface.ITALIC));
        btnUnderline.setOnClickListener(v -> applyUnderline());
        btnBullet.setOnClickListener(v -> insertAtCursor("\n• "));
        btnTask.setOnClickListener(v -> insertAtCursor("\n☐ "));
        btnNumbered.setOnClickListener(v -> insertNumberedList());

        // Auto continuation for bullets, numbers, tasks
        editContent.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                int cursor = editContent.getSelectionStart();
                if (cursor > 0 && cursor <= s.length()) {
                    if (s.charAt(cursor - 1) == '\n') {
                        String textBefore = s.subSequence(0, cursor - 1).toString();
                        String[] lines = textBefore.split("\n");
                        String lastLine = lines.length > 0 ? lines[lines.length - 1] : "";

                        if (lastLine.matches("•\\s.*")) {
                            s.insert(cursor, "• ");
                        } else if (lastLine.matches("☐\\s.*")) {
                            s.insert(cursor, "☐ ");
                        } else if (lastLine.matches("\\d+\\.\\s.*")) {
                            int lastNumber = 1;
                            try {
                                lastNumber = Integer.parseInt(lastLine.split("\\.")[0].trim());
                            } catch (Exception ignored) {}
                            s.insert(cursor, (lastNumber + 1) + ". ");
                        }
                    }
                }

                isEditing = false;
            }
        });
    }

    // ---------- Formatting ----------
    private void applyStyle(int style) {
        int start = editContent.getSelectionStart();
        int end = editContent.getSelectionEnd();
        if (start >= end) return;
        editContent.getText().setSpan(new StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void applyUnderline() {
        int start = editContent.getSelectionStart();
        int end = editContent.getSelectionEnd();
        if (start >= end) return;
        editContent.getText().setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void insertAtCursor(String text) {
        int pos = editContent.getSelectionStart();
        if (pos < 0) pos = 0;
        editContent.getText().insert(pos, text);
    }

    private void insertNumberedList() {
        int pos = editContent.getSelectionStart();
        if (pos < 0) pos = 0;

        String content = editContent.getText().toString();
        int lineNumber = 1;
        if (pos > 0) {
            String before = content.substring(0, pos);
            String[] lines = before.split("\n");
            for (String l : lines) {
                if (l.matches("\\d+\\.\\s.*")) lineNumber++;
            }
        }
        editContent.getText().insert(pos, "\n" + lineNumber + ". ");
    }

    // ---------- Database ----------
    private void loadFolders() {
        new Thread(() -> {
            List<ListFolder> data = db.listFolderDao().getAll();
            runOnUiThread(() -> {
                folders.clear();
                folders.addAll(data);
                spinnerAdapter.clear();
                for (ListFolder f : folders) spinnerAdapter.add(f.name);
                spinnerAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void loadExistingItem(int id) {
        new Thread(() -> {
            ListItem item = db.listItemDao().getById(id);
            if (item == null) return;

            existingItem = item;
            runOnUiThread(() -> {
                editTitle.setText(item.title);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    editContent.setText(Html.fromHtml(item.content, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    editContent.setText(Html.fromHtml(item.content));
                }

                for (int i = 0; i < folders.size(); i++) {
                    if (folders.get(i).id == item.folderId) {
                        spinnerFolder.setSelection(i);
                        break;
                    }
                }
            });
        }).start();
    }

    private void saveItem() {
        String title = editTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
            return;
        }

        String html = Html.toHtml(editContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        int folderId = folders.get(spinnerFolder.getSelectedItemPosition()).id;

        new Thread(() -> {
            if (existingItem != null) {
                existingItem.title = title;
                existingItem.content = html;
                existingItem.folderId = folderId;
                db.listItemDao().update(existingItem);
            } else {
                db.listItemDao().insert(new ListItem(folderId, title, html, System.currentTimeMillis()));
            }
            runOnUiThread(this::finish);
        }).start();
    }
}
