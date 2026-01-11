package com.sanjana.oneforall.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.adapters.ItemAdapter;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.Category;
import com.sanjana.oneforall.database.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    private AppDatabase db;

    private Spinner categorySpinner;
    private Button btnWatching, btnCompleted, btnOnHold, btnDropped, btnPlan;

    private String selectedStatus = "All";
    private int selectedCategoryId = 0; // 0 = All
    private List<Category> categories = new ArrayList<>();

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = AppDatabase.getInstance(requireContext());

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(getContext(), filteredItems);
        recyclerView.setAdapter(adapter);

        // Spinner
        categorySpinner = view.findViewById(R.id.filterSpinner);

        // Status buttons
        btnWatching = view.findViewById(R.id.btnWatching);
        btnCompleted = view.findViewById(R.id.btnCompleted);
        btnOnHold = view.findViewById(R.id.btnOnHold);
        btnDropped = view.findViewById(R.id.btnDropped);
        btnPlan = view.findViewById(R.id.btnPlan);

        setupStatusButtons();
        loadCategoriesAndItems(); // load both in background

        return view;
    }

    private void setupCategorySpinner() {
        List<String> names = new ArrayList<>();
        names.add("All");
        for (Category c : categories) {
            names.add(c.name);
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                names
        );
        categorySpinner.setAdapter(spinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = (position == 0) ? 0 : categories.get(position - 1).id;
                applyFilter();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupStatusButtons() {
        View.OnClickListener statusClick = v -> {
            btnWatching.setSelected(false);
            btnCompleted.setSelected(false);
            btnOnHold.setSelected(false);
            btnDropped.setSelected(false);
            btnPlan.setSelected(false);

            v.setSelected(true);

            if (v == btnWatching) selectedStatus = "Watching";
            else if (v == btnCompleted) selectedStatus = "Completed";
            else if (v == btnOnHold) selectedStatus = "On-Hold";
            else if (v == btnDropped) selectedStatus = "Dropped";
            else if (v == btnPlan) selectedStatus = "Plan to Watch";
            else selectedStatus = "All";

            applyFilter();
        };

        btnWatching.setOnClickListener(statusClick);
        btnCompleted.setOnClickListener(statusClick);
        btnOnHold.setOnClickListener(statusClick);
        btnDropped.setOnClickListener(statusClick);
        btnPlan.setOnClickListener(statusClick);
    }

    private void loadCategoriesAndItems() {
        executor.execute(() -> {
            categories = db.categoryDao().getAllCategories();
            allItems = db.itemDao().getAllItems();

            getActivity().runOnUiThread(() -> {
                setupCategorySpinner();
                applyFilter();
            });
        });
    }

    private void applyFilter() {
        filteredItems.clear();
        for (Item item : allItems) {
            boolean matchCategory = (selectedCategoryId == 0 || item.categoryId == selectedCategoryId);
            boolean matchStatus = (selectedStatus.equals("All") || item.status.equals(selectedStatus));
            if (matchCategory && matchStatus) filteredItems.add(item);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategoriesAndItems(); // refresh safely in background
    }
}
