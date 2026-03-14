package com.example.newsapp;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedFragment extends Fragment {
    RecyclerView recyclerView;
    SavedNewsAdapter adapter;
    List<News> savedList;
    DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewSaved);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(getContext());
        savedList = dbHelper.getAllSavedNews(); // <-- lấy dữ liệu từ SQLite
        adapter = new SavedNewsAdapter(getContext(), savedList, dbHelper);

        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        savedList.clear();
        savedList.addAll(dbHelper.getAllSavedNews());
        adapter.notifyDataSetChanged();
    }
}

