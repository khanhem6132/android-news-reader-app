package com.example.newsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import java.util.Locale;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {


    RecyclerView recyclerView;
    NewsAdapter newsAdapter;
    List<News> newsList;
    TabLayout tabLayout;
    ProgressBar progressBar;
    HashMap<String, String> rssMap;

    private static final int VOICE_SEARCH_REQUEST_CODE = 1001;

    public List<News> getNewsList() {
        return newsList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewNews);
        tabLayout = view.findViewById(R.id.tabLayout);
        progressBar = view.findViewById(R.id.progressBar);

        SearchView searchView = view.findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNews(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNews(newText);
                return true;
            }
        });

        ImageButton btnVoice = view.findViewById(R.id.btnVoiceSearch);
        btnVoice.setOnClickListener(v -> startVoiceSearch());

        newsList = new ArrayList<>();

        if (getContext() != null) {
            newsAdapter = new NewsAdapter(getContext(), newsList);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(newsAdapter);
        }

        setupRSSMapping();
        setupTabs();
        setupTabListener();

        new FetchRSS().execute(rssMap.get("Tin mới"));

        return view;
    }

    private void setupRSSMapping() {
        rssMap = new HashMap<>();
        rssMap.put("Tin mới", "https://tuoitre.vn/rss/tin-moi-nhat.rss");
        rssMap.put("Thế giới", "https://tuoitre.vn/rss/the-gioi.rss");
        rssMap.put("Thể thao", "https://tuoitre.vn/rss/the-thao.rss");
        rssMap.put("Giải trí", "https://tuoitre.vn/rss/giai-tri.rss");
        rssMap.put("Kinh doanh", "https://tuoitre.vn/rss/kinh-doanh.rss");
        rssMap.put("Giáo dục", "https://tuoitre.vn/rss/giao-duc.rss");
        rssMap.put("Sức khỏe", "https://tuoitre.vn/rss/suc-khoe.rss");
        rssMap.put("Du lịch", "https://tuoitre.vn/rss/du-lich.rss");
    }

    private void setupTabs() {
        for (String tabName : rssMap.keySet()) {
            tabLayout.addTab(tabLayout.newTab().setText(tabName));
        }
    }

    private void setupTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                String tabName = tab.getText().toString();
                String rssUrl = rssMap.get(tabName);

                if (rssUrl != null) {
                    new FetchRSS().execute(rssUrl);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void startVoiceSearch() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Nói từ khóa bạn muốn tìm...");

        try {
            startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(),
                        "Thiết bị không hỗ trợ tìm kiếm bằng giọng nói",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_SEARCH_REQUEST_CODE
                && resultCode == Activity.RESULT_OK
                && data != null) {

            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {

                String spokenText = results.get(0);

                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Bạn nói: " + spokenText,
                            Toast.LENGTH_SHORT).show();
                }

                filterNews(spokenText);
            }
        }
    }

    private void filterNews(String query) {

        if (query == null || query.trim().isEmpty()) {
            newsAdapter.filter("");
        } else {
            newsAdapter.filter(query);
        }
    }

    private class FetchRSS extends AsyncTask<String, Void, List<News>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<News> doInBackground(String... strings) {

            List<News> tempList = new ArrayList<>();

            String rssUrl = strings[0];

            try {

                Document doc = Jsoup.connect(rssUrl)
                        .timeout(15000)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0")
                        .get();

                Elements items = doc.select("item");

                for (Element item : items) {

                    String title = item.selectFirst("title") != null ?
                            item.selectFirst("title").text() : "";

                    String description = item.selectFirst("description") != null ?
                            item.selectFirst("description").text() : "";

                    String pubDate = item.selectFirst("pubDate") != null ?
                            item.selectFirst("pubDate").text() : "";

                    String link = item.selectFirst("link") != null ?
                            item.selectFirst("link").text() : "";

                    String imageUrl = "";

                    if (description.contains("<img")) {

                        Document descDoc = Jsoup.parse(description);

                        Element img = descDoc.selectFirst("img");

                        if (img != null) {
                            imageUrl = img.attr("src");
                        }
                    }

                    tempList.add(new News(title, description, link, pubDate, imageUrl));
                }

            } catch (Exception e) {

                Log.e("FETCH_RSS", "Lỗi khi tải RSS: " + e.getMessage());
            }

            return tempList;
        }

        @Override
        protected void onPostExecute(List<News> news) {

            super.onPostExecute(news);

            if (!isAdded()) return;   // 🔴 FIX CRASH Ở ĐÂY

            progressBar.setVisibility(View.GONE);

            if (news.isEmpty()) {

                Toast.makeText(getContext(),
                        "Không tải được tin tức",
                        Toast.LENGTH_SHORT).show();

            } else {

                newsList.clear();
                newsList.addAll(news);

                newsAdapter.updateData(news);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

                    if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                            != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                    }
                }

                SharedPreferences prefs =
                        requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

                boolean hasShownNotification =
                        prefs.getBoolean("hasShownNotification", false);

                if (!hasShownNotification && !newsList.isEmpty()) {

                    int randomIndex =
                            new java.util.Random().nextInt(newsList.size());

                    News randomNews = newsList.get(randomIndex);

                    NotificationHelper.showRandomNewsNotification(requireContext(), randomNews);

                    prefs.edit().putBoolean("hasShownNotification", true).apply();
                }
            }
        }
    }

    public void refreshNewsSettings() {

        if (newsAdapter != null) {

            newsAdapter.notifyDataSetChanged();
        }
    }


}
