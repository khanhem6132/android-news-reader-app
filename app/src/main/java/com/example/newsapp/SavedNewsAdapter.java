package com.example.newsapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SavedNewsAdapter extends RecyclerView.Adapter<SavedNewsAdapter.NewsViewHolder> {

    private Context context;
    private List<News> newsList;
    private DatabaseHelper databaseHelper;

    public SavedNewsAdapter(Context context, List<News> newsList, DatabaseHelper databaseHelper) {
        this.context = context;
        this.newsList = newsList;
        this.databaseHelper = databaseHelper;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);

        holder.textTitle.setText(news.getTitle());
        holder.textDescription.setText(news.getDescription());

        Glide.with(context)
                .load(news.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageNews);

        // 👉 Xử lý nhấn để đọc bài viết chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SavedNewsDetailActivity.class);
            intent.putExtra("title", news.getTitle());
            intent.putExtra("imageUrl", news.getImageUrl());
            intent.putExtra("content", news.getContent()); // ✅ truyền nội dung HTML thật
            context.startActivity(intent);
        });


        // 👉 Nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            databaseHelper.deleteNews(news.getLink());
            newsList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDescription;
        ImageView imageNews;
        Button btnDelete;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            imageNews = itemView.findViewById(R.id.imageNews); // ✅ trùng với XML
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

}
