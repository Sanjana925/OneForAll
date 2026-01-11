package com.sanjana.oneforall.ui.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.adapters.ListFolderAdapter;
import com.sanjana.oneforall.adapters.ListItemAdapter;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.ListFolder;
import com.sanjana.oneforall.database.ListItem;
import com.sanjana.oneforall.ui.list.AddEditListActivity;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {

    private RecyclerView rvFolders, rvItems;
    private ListFolderAdapter folderAdapter;
    private ListItemAdapter itemAdapter;
    private List<ListFolder> folders = new ArrayList<>();
    private List<ListItem> items = new ArrayList<>();
    private AppDatabase db;

    private ListFolder selectedFolder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        db = AppDatabase.getInstance(requireContext());

        rvFolders = view.findViewById(R.id.rvFolders);
        rvItems = view.findViewById(R.id.rvListItems);

        // --- FOLDERS ADAPTER ---
        folderAdapter = new ListFolderAdapter(getContext(), folders, folder -> {
            selectedFolder = folder;
            loadItems();
        });
        rvFolders.setAdapter(folderAdapter);
        rvFolders.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // --- ITEMS ADAPTER ---
        itemAdapter = new ListItemAdapter(getContext(), items, new ListItemAdapter.OnItemClickListener() {
            @Override
            public void onEdit(ListItem item) {
                Intent i = new Intent(getContext(), AddEditListActivity.class);
                i.putExtra("itemId", item.id);
                startActivity(i);
            }

            @Override
            public void onDelete(ListItem item) {
                new Thread(() -> {
                    db.listItemDao().delete(item);
                    loadItems(); // reload after deletion
                }).start();
            }
        });
        rvItems.setAdapter(itemAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));

        loadFolders(); // initial load

        return view;
    }

    private void loadFolders() {
        new Thread(() -> {
            List<ListFolder> dbFolders = db.listFolderDao().getAll(); // background thread
            getActivity().runOnUiThread(() -> { // update UI
                folders.clear();
                folders.addAll(dbFolders);
                folderAdapter.notifyDataSetChanged();

                if (!folders.isEmpty()) {
                    selectedFolder = folders.get(0);
                    loadItems(); // load items for the first folder
                }
            });
        }).start();
    }

    private void loadItems() {
        if (selectedFolder == null) return;

        new Thread(() -> {
            List<ListItem> dbItems = db.listItemDao().getByFolder(selectedFolder.id);
            // optional: filter visible/favorites here if needed
            getActivity().runOnUiThread(() -> {
                items.clear();
                items.addAll(dbItems);
                itemAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFolders(); // refresh when returning to fragment
    }
}
