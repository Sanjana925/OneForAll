package com.sanjana.oneforall.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sanjana.oneforall.R;
import com.sanjana.oneforall.adapters.FolderPillAdapter;
import com.sanjana.oneforall.adapters.ListItemAdapter;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.ListFolder;
import com.sanjana.oneforall.database.ListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFragment extends Fragment {

    private RecyclerView rvFolders, rvItems;
    private FolderPillAdapter folderAdapter;
    private ListItemAdapter itemAdapter;
    private List<ListFolder> folders = new ArrayList<>();
    private List<ListItem> items = new ArrayList<>();
    private AppDatabase db;
    private ListFolder selectedFolder; // -1 = All

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);

        db = AppDatabase.getInstance(requireContext());
        rvFolders = view.findViewById(R.id.rvFolders);
        rvItems = view.findViewById(R.id.rvListItems);

        rvFolders.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false)
        );
        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));

        folderAdapter = new FolderPillAdapter(getContext(), folders, folder -> {
            selectedFolder = folder;
            loadItems();
        });
        rvFolders.setAdapter(folderAdapter);

        itemAdapter = new ListItemAdapter(getContext(), items, item -> {
            // Delete confirmation
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes", (d, w) -> {
                        new Thread(() -> {
                            db.listItemDao().delete(item);
                            requireActivity().runOnUiThread(() -> itemAdapter.removeItem(item));
                        }).start();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        rvItems.setAdapter(itemAdapter);

        // Folder menu icon
        ImageButton btnMenu = view.findViewById(R.id.btnFolderMenu);
        btnMenu.setOnClickListener(v -> showFolderMenu());

        loadFolders();

        return view;
    }

    private void loadFolders() {
        new Thread(() -> {
            List<ListFolder> dbFolders = db.listFolderDao().getAll();

            // Add "All" at position 0
            ListFolder allFolder = new ListFolder("All");
            allFolder.id = -1;

            List<ListFolder> allFolders = new ArrayList<>();
            allFolders.add(allFolder);
            allFolders.addAll(dbFolders);

            requireActivity().runOnUiThread(() -> {
                folders.clear();
                folders.addAll(allFolders);
                folderAdapter.notifyDataSetChanged();

                // Select "All" by default
                if (selectedFolder == null) {
                    selectedFolder = allFolder;
                    folderAdapter.notifyItemChanged(0);
                }

                loadItems();
            });
        }).start();
    }

    private void loadItems() {
        new Thread(() -> {
            List<ListItem> dbItems;
            if (selectedFolder != null && selectedFolder.id != -1) {
                dbItems = db.listItemDao().getByFolder(selectedFolder.id);
            } else {
                dbItems = db.listItemDao().getAll();
            }

            Collections.sort(dbItems, (a, b) -> Long.compare(b.timestamp, a.timestamp));

            requireActivity().runOnUiThread(() -> {
                items.clear();
                items.addAll(dbItems);
                itemAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showFolderMenu() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Folders")
                .setItems(new String[]{"All", "Add New Folder"}, (dialog, which) -> {
                    if (which == 0) {
                        // Select All
                        selectedFolder = folders.get(0);
                        folderAdapter.notifyDataSetChanged();
                        loadItems();
                    } else {
                        // Add Folder
                        showAddFolderDialog();
                    }
                }).show();
    }

    private void showAddFolderDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Folder Name");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Folder")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Enter folder name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new Thread(() -> {
                        db.listFolderDao().insert(new ListFolder(name));
                        loadFolders();
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
