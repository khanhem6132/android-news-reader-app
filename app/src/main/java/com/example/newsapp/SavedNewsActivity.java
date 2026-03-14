package com.example.newsapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedNewsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSavedNews;
    private SavedNewsAdapter adapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_news);

        // Khởi tạo RecyclerView và DatabaseHelper
        recyclerViewSavedNews = findViewById(R.id.recyclerViewSavedNews);
        recyclerViewSavedNews.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);

        // Lấy danh sách bài viết đã lưu
        List<News> savedNewsList = databaseHelper.getAllSavedNews();

        if (savedNewsList.isEmpty()) {
            Toast.makeText(this, "Chưa có bài viết nào được lưu", Toast.LENGTH_SHORT).show();
        }

        // Tạo adapter và gắn vào RecyclerView
        adapter = new SavedNewsAdapter(this, savedNewsList, databaseHelper);
        recyclerViewSavedNews.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại danh sách khi quay lại Activity
        List<News> updatedList = databaseHelper.getAllSavedNews();
        adapter = new SavedNewsAdapter(this, updatedList, databaseHelper);
        recyclerViewSavedNews.setAdapter(adapter);
    }
}
