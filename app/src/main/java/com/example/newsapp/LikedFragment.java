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

public class LikedFragment extends Fragment {

    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private List<News> likedList;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liked, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewLiked);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(getContext());
        likedList = dbHelper.getLikedNews();

        adapter = new NewsAdapter(getContext(), likedList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLikedNews();
    }

    private void refreshLikedNews() {
        likedList.clear();
        likedList.addAll(dbHelper.getLikedNews());
        adapter.notifyDataSetChanged();
    }
}
