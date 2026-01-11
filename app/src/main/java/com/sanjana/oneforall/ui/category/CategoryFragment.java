package com.sanjana.oneforall.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sanjana.oneforall.adapters.CategoryAdapter;
import com.sanjana.oneforall.database.AppDatabase;
import com.sanjana.oneforall.database.Category;
import com.sanjana.oneforall.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categories = new ArrayList<>();
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public CategoryFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        db = AppDatabase.getInstance(requireContext());
        recyclerView = view.findViewById(R.id.recyclerCategory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CategoryAdapter(getContext(), categories);
        recyclerView.setAdapter(adapter);

        loadCategories(); // load in background
        return view;
    }

    private void loadCategories() {
        executor.execute(() -> {
            List<Category> dbCategories = db.categoryDao().getAllCategories();
            getActivity().runOnUiThread(() -> {
                categories.clear();
                categories.addAll(dbCategories);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategories(); // refresh whenever fragment resumes
    }
}
