package com.sanjana.oneforall.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.adapters.ItemAdapter;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.Category;
import com.sanjana.oneforall.database.Item;

import java.util.ArrayList;
import java.util.Collections;
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
    private ImageButton btnSort;
    private TextView tvTotalEntries;
    private Button btnAll, btnWatching, btnCompleted, btnOnHold, btnDropped, btnPlan;

    private String selectedStatus = "All";
    private int selectedCategoryId = 0;
    private List<Category> categories = new ArrayList<>();

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private enum SortOption {
        STATUS, ALPHABET_ASC, ALPHABET_DESC, LAST_UPDATED_NEW, LAST_UPDATED_OLD
    }

    private SortOption currentSort = SortOption.LAST_UPDATED_NEW;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = AppDatabase.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.recyclerHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(getContext(), filteredItems);
        recyclerView.setAdapter(adapter);

        categorySpinner = view.findViewById(R.id.spinnerCategory);
        btnSort = view.findViewById(R.id.btnSort);
        tvTotalEntries = view.findViewById(R.id.tvTotalEntries);

        btnAll = view.findViewById(R.id.btnAll);
        btnWatching = view.findViewById(R.id.btnWatching);
        btnCompleted = view.findViewById(R.id.btnCompleted);
        btnOnHold = view.findViewById(R.id.btnOnHold);
        btnDropped = view.findViewById(R.id.btnDropped);
        btnPlan = view.findViewById(R.id.btnPlan);

        setupStatusButtons();
        setupSortButton();
        setupDragAndDrop();
        loadAllItems();

        return view;
    }

    private void setupCategorySpinner() {
        List<String> names = new ArrayList<>();
        names.add("All");
        for (Category c : categories) names.add(c.name);

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
            btnAll.setSelected(false);
            btnWatching.setSelected(false);
            btnCompleted.setSelected(false);
            btnOnHold.setSelected(false);
            btnDropped.setSelected(false);
            btnPlan.setSelected(false);

            v.setSelected(true);

            if (v == btnAll) selectedStatus = "All";
            else if (v == btnWatching) selectedStatus = "Watching";
            else if (v == btnCompleted) selectedStatus = "Completed";
            else if (v == btnOnHold) selectedStatus = "On-Hold";
            else if (v == btnDropped) selectedStatus = "Dropped";
            else if (v == btnPlan) selectedStatus = "Plan to Watch";

            applyFilter();
        };

        btnAll.setOnClickListener(statusClick);
        btnWatching.setOnClickListener(statusClick);
        btnCompleted.setOnClickListener(statusClick);
        btnOnHold.setOnClickListener(statusClick);
        btnDropped.setOnClickListener(statusClick);
        btnPlan.setOnClickListener(statusClick);
    }

    private void setupSortButton() {
        btnSort.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(requireContext(), btnSort);
            popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.sort_status) currentSort = SortOption.STATUS;
                else if (id == R.id.sort_alpha_asc) currentSort = SortOption.ALPHABET_ASC;
                else if (id == R.id.sort_alpha_desc) currentSort = SortOption.ALPHABET_DESC;
                else if (id == R.id.sort_last_new) currentSort = SortOption.LAST_UPDATED_NEW;
                else if (id == R.id.sort_last_old) currentSort = SortOption.LAST_UPDATED_OLD;

                applyFilter();
                return true;
            });
            popup.show();
        });
    }

    private void loadAllItems() {
        executor.execute(() -> {
            categories = db.categoryDao().getAllCategories();
            allItems = db.itemDao().getAllItemsOrdered();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setupCategorySpinner();
                    applyFilter();
                });
            }
        });
    }

    private void applyFilter() {
        filteredItems.clear();
        for (Item item : allItems) {
            boolean matchCategory = (selectedCategoryId == 0 || item.categoryId == selectedCategoryId);
            boolean matchStatus = (selectedStatus.equals("All") || item.status.equals(selectedStatus));
            if (matchCategory && matchStatus) filteredItems.add(item);
        }

        switch (currentSort) {
            case STATUS: break;
            case ALPHABET_ASC: Collections.sort(filteredItems, (a,b)->a.title.compareToIgnoreCase(b.title)); break;
            case ALPHABET_DESC: Collections.sort(filteredItems, (a,b)->b.title.compareToIgnoreCase(a.title)); break;
            case LAST_UPDATED_NEW: Collections.sort(filteredItems, (a,b)->Long.compare(b.lastUpdated,a.lastUpdated)); break;
            case LAST_UPDATED_OLD: Collections.sort(filteredItems, (a,b)->Long.compare(a.lastUpdated,b.lastUpdated)); break;
        }

        tvTotalEntries.setText("Total Entries: " + filteredItems.size());
        adapter.notifyDataSetChanged();
    }

    private void setupDragAndDrop() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();

                Collections.swap(filteredItems, from, to);
                adapter.notifyItemMoved(from, to);
                updateAllItemsOrder();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private void updateAllItemsOrder() {
        executor.execute(() -> {
            for (int i = 0; i < filteredItems.size(); i++) {
                Item filteredItem = filteredItems.get(i);
                for (Item item : allItems) {
                    if (item.id == filteredItem.id) {
                        item.orderIndex = i;
                        db.itemDao().update(item);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllItems();
    }
}
