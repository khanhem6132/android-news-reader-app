package com.example.newsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<News> newsList;
    private List<News> fullList;

    public NewsAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = newsList;
        this.fullList = new ArrayList<>(newsList);
    }

    public void updateData(List<News> newList) {
        newsList.clear();
        newsList.addAll(newList);
        fullList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        List<News> filteredList = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String search = text.toLowerCase();
            for (News news : fullList) {
                if (news.getTitle().toLowerCase().contains(search) ||
                        news.getDescription().toLowerCase().contains(search)) {
                    filteredList.add(news);
                }
            }
        }
        newsList.clear();
        newsList.addAll(filteredList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.textTitle.setText(news.getTitle());
        holder.textDescription.setText(news.getDescription());
        holder.textTime.setText(news.getPubDate());

        if (!news.getImageUrl().isEmpty()) {
            Picasso.get().load(news.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.imageNews);
        } else {
            holder.imageNews.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // ✅ Đọc cài đặt người dùng
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String listSize = prefs.getString("list_size", "large");
        int textSize = prefs.getInt("text_size", 14);
        boolean showImage = prefs.getBoolean("show_image", true);

        // ✅ Thay đổi kích thước item
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params != null) {
            params.height = listSize.equals("large") ? 420 : 260;
            holder.itemView.setLayoutParams(params);
        }

        // ✅ Cỡ chữ bài viết
        holder.textTitle.setTextSize(textSize + 2);
        holder.textDescription.setTextSize(textSize);

        // ✅ Hiển thị hoặc ẩn hình ảnh
        if (showImage && !news.getImageUrl().isEmpty()) {
            holder.imageNews.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(news.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.imageNews);
        } else {
            holder.imageNews.setVisibility(View.GONE);
        }

        // ✅ Bắt sự kiện click mở WebView
        holder.itemView.setOnClickListener(v -> {
            int positionClicked = holder.getAdapterPosition();
            if (positionClicked != RecyclerView.NO_POSITION) {
                News clickedNews = newsList.get(positionClicked);

                // Kiểm tra null trước khi mở bài viết
                if (clickedNews.getLink() != null && !clickedNews.getLink().isEmpty()) {
                    Intent intent = new Intent(context, NewsDetailActivity.class);
                    intent.putExtra("url", clickedNews.getLink());
                    intent.putExtra("title", clickedNews.getTitle());
                    intent.putExtra("image", clickedNews.getImageUrl());
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Bài viết này không có link hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDescription, textTime;
        ImageView imageNews;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textTime = itemView.findViewById(R.id.textTime);
            imageNews = itemView.findViewById(R.id.imageNews);
        }
    }
}

