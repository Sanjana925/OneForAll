package com.sanjana.oneforall.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.adapters.ItemAdapter;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.Item;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    private AppDatabase db;
    private EditText searchInput;

    public SearchFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        db = AppDatabase.getInstance(requireContext());

        searchInput = view.findViewById(R.id.searchEditText);
        recyclerView = view.findViewById(R.id.recyclerSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ItemAdapter(getContext(), filteredItems);
        recyclerView.setAdapter(adapter);

        loadItems();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadItems() {
        new Thread(() -> {
            List<Item> dbItems = db.itemDao().getAllItemsOrdered();

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                allItems.clear();
                allItems.addAll(dbItems);
                filterItems(searchInput.getText().toString());
            });
        }).start();
    }

    private void filterItems(String query) {
        filteredItems.clear();
        for (Item item : allItems) {
            if (item.title.toLowerCase().contains(query.toLowerCase())) {
                filteredItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadItems();
    }
}
