package com.example.newsapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import java.util.List;

public class LikedNewsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NewsAdapter adapter;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_news); // ✅ layout riêng

        Toolbar toolbar = findViewById(R.id.toolbarLiked);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerViewLikedNews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        List<News> likedList = databaseHelper.getLikedNews();

        adapter = new NewsAdapter(this, likedList);
        recyclerView.setAdapter(adapter);
    }
}
