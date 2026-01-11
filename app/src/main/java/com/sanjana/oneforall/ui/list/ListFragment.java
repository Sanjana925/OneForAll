package com.sanjana.oneforall.ui.list;

import android.app.AlertDialog;
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
import com.sanjana.oneforall.adapters.ListItemAdapter;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.ListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFragment extends Fragment {

    private RecyclerView rvItems;
    private ListItemAdapter itemAdapter;
    private List<ListItem> items = new ArrayList<>();
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        db = AppDatabase.getInstance(requireContext());

        rvItems = view.findViewById(R.id.rvListItems);
        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));

        itemAdapter = new ListItemAdapter(getContext(), items, item -> {
            // Delete confirmation
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        new Thread(() -> {
                            db.listItemDao().delete(item);
                            requireActivity().runOnUiThread(() -> itemAdapter.removeItem(item));
                        }).start();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        rvItems.setAdapter(itemAdapter);

        loadItems();

        return view;
    }

    private void loadItems() {
        new Thread(() -> {
            List<ListItem> dbItems = db.listItemDao().getAll();
            // Sort by latest first
            Collections.sort(dbItems, (a, b) -> Long.compare(b.timestamp, a.timestamp));

            requireActivity().runOnUiThread(() -> {
                items.clear();
                items.addAll(dbItems);
                itemAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadItems(); // refresh when returning from edit/add
    }
}
