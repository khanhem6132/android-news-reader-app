package com.example.newsapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewsDetailActivity extends AppCompatActivity {

    TextView textTitle;
    ImageView imageNews;
    WebView webView;
    Button btnSave, btnShare, btnLike;
    ImageButton btnSpeak, btnStop;
    TextToSpeech tts;
    String content;

    private EditText editComment;
    private Button btnSendComment;
    private RecyclerView recyclerComments;

    private List<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private FirebaseFirestore firestore;
    private String newsId;

    String url, title, imageUrl;
    DatabaseHelper databaseHelper;
    String articleHtml = ""; // ✅ biến lưu nội dung bài viết để lưu offline

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        textTitle = findViewById(R.id.textTitle);
        imageNews = findViewById(R.id.imageNews);
        webView = findViewById(R.id.webView);
        btnSave = findViewById(R.id.btnSave);
        btnShare = findViewById(R.id.btnShare);
        btnLike = findViewById(R.id.btnLike);
        btnSpeak = findViewById(R.id.btnSpeak);
        btnStop = findViewById(R.id.btnStop);

        databaseHelper = new DatabaseHelper(this);

        // ✅ Nhận dữ liệu từ Intent
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        imageUrl = getIntent().getStringExtra("image");


        textTitle.setText(title);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(imageNews);
        }

        // Khởi tạo Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("vi", "VN"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Ngôn ngữ không được hỗ trợ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không thể khởi tạo Text-to-Speech", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút đọc bài viết
        btnSpeak.setOnClickListener(v -> speakText());

        // Nút dừng đọc
        btnStop.setOnClickListener(v -> {
            if (tts.isSpeaking()) {
                tts.stop();
                Toast.makeText(this, "Đã dừng đọc", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Nút lưu bài viết
        btnSave.setOnClickListener(v -> {
            String desc = "Xem trong bài viết";
            String pubDate = "Offline";

            // ⚠️ Đưa nội dung thật vào đây
            if (articleHtml.isEmpty()) {
                Toast.makeText(this, "Chờ tải nội dung xong rồi hãy lưu!", Toast.LENGTH_SHORT).show();
                return;
            }

            News news = new News(title, desc, url, pubDate, imageUrl, articleHtml);
            if (!databaseHelper.isNewsSaved(url)) {
                databaseHelper.addNews(news);
                Toast.makeText(this, "Đã lưu bài viết offline!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bài viết này đã lưu rồi", Toast.LENGTH_SHORT).show();
            }
        });

        //nút chia sẻ
        btnShare.setOnClickListener(v -> {
            if (url != null && !url.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\nĐọc tại: " + url);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết qua..."));
            } else {
                Toast.makeText(this, "Không thể chia sẻ: bài viết không có link hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        //nút thích
        btnLike.setOnClickListener(v -> {
            String desc = "Xem trong bài viết";  // mô tả ngắn mặc định
            String pubDate = "Online";           // vì bài này không lưu offline

            // Nếu bài chưa lưu, thêm bản tóm tắt để đánh dấu yêu thích
            if (!databaseHelper.isNewsSaved(url)) {
                News news = new News(title, desc, url, pubDate, imageUrl);
                databaseHelper.addNews(news);
            }

            boolean currentlyLiked = databaseHelper.isLiked(url);
            databaseHelper.setLiked(url, !currentlyLiked);

            Toast.makeText(this,
                    !currentlyLiked ? "Đã thêm vào danh sách yêu thích ❤️" : "Đã bỏ thích 💔",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // ✅ Kiểm tra URL hợp lệ
        if (url == null || !url.startsWith("http")) {
            Toast.makeText(this, "URL bài viết không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Cấu hình WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(false);
        webSettings.setDefaultFontSize(16);
        webSettings.setLoadsImagesAutomatically(true);
        webView.setBackgroundColor(0xFFFFFFFF);

        // ✅ Tải nội dung bài viết
        new LoadArticleTask().execute(url);

        // 🔹 Khởi tạo bình luận
        editComment = findViewById(R.id.editComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        recyclerComments = findViewById(R.id.recyclerComments);

        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList);
        recyclerComments.setAdapter(commentAdapter);

        firestore = FirebaseFirestore.getInstance();
        newsId = title.replaceAll("\\s+", "_"); // dùng tiêu đề làm ID

        // Gửi bình luận
        btnSendComment.setOnClickListener(v -> {
            String text = editComment.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung bình luận", Toast.LENGTH_SHORT).show();
                return;
            }

            Comment comment = new Comment("Người dùng", text, System.currentTimeMillis());
            firestore.collection("comments")
                    .document(newsId)
                    .collection("list")
                    .add(comment)
                    .addOnSuccessListener(docRef -> {
                        editComment.setText("");
                        Toast.makeText(this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi gửi bình luận", Toast.LENGTH_SHORT).show()
                    );
        });

// 🔹 Tải bình luận realtime (không cần thoát app)
        loadCommentsRealtime();

    }

    private void loadCommentsRealtime() {
        firestore.collection("comments")
                .document(newsId)
                .collection("list")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    commentList.clear();
                    if (value != null) {
                        for (var doc : value.getDocuments()) {
                            Comment c = doc.toObject(Comment.class);
                            if (c != null) commentList.add(c);
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                });
    }


    private void speakText() {
        if (tts == null) return;

        String textToRead;

        // Nếu bài viết đã tải xong
        if (articleHtml != null && !articleHtml.isEmpty()) {
            Document doc = Jsoup.parse(articleHtml);
            String plainText = doc.text().trim();

            if (plainText.isEmpty()) {
                Toast.makeText(this, "Không có nội dung để đọc.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gộp tiêu đề + nội dung
            textToRead = title + ". " + plainText;
        } else {
            Toast.makeText(this, "Đang tải nội dung bài viết...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Giới hạn độ dài mỗi lần đọc (TTS không đọc quá dài)
        int chunkSize = 3000;
        if (textToRead.length() > chunkSize) {
            int start = 0;
            while (start < textToRead.length()) {
                int end = Math.min(start + chunkSize, textToRead.length());
                String chunk = textToRead.substring(start, end);
                tts.speak(chunk, TextToSpeech.QUEUE_ADD, null, null);
                start = end;
            }
        } else {
            tts.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }



    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private class LoadArticleTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                Document doc = Jsoup.connect(strings[0]).get();

                Element content = doc.selectFirst(
                        "div#main-detail-body, div.detail-content, article, div.main-content-body"
                );

                if (content != null) {
                    content.select("script, style, iframe, figure, .banner, .adsbygoogle, .related-news").remove();

                    String cssStyle = "<style>" +
                            "body { font-family: sans-serif; line-height: 1.6; padding: 10px; }" +
                            "img { max-width: 100%; height: auto; border-radius: 8px; margin: 8px 0; }" +
                            "p { margin: 10px 0; }" +
                            "strong { color: #222; }" +
                            "</style>";

                    return cssStyle + content.html();
                } else {
                    return "<p>Không tìm thấy nội dung bài viết.</p>";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "<p>Lỗi khi tải nội dung bài viết.</p>";
            }
        }

        @Override
        protected void onPostExecute(String htmlContent) {
            articleHtml = htmlContent; // ✅ lưu lại HTML thực tế để dùng khi bấm “Lưu”
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
        }
    }
}
