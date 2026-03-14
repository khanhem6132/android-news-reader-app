package com.example.newsapp;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SavedNewsDetailActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textTitle;
    private WebView webView;
    private EditText editComment;
    private Button btnSend;
    private RecyclerView recyclerComments;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String newsId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_news_detail);

        imageView = findViewById(R.id.imageView);
        textTitle = findViewById(R.id.textTitle);
        webView = findViewById(R.id.webView);
        editComment = findViewById(R.id.editComment);
        btnSend = findViewById(R.id.btnSendComment);
        recyclerComments = findViewById(R.id.recyclerComments);

        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList);
        recyclerComments.setAdapter(commentAdapter);

        firestore = FirebaseFirestore.getInstance();

        String title = getIntent().getStringExtra("title");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String content = getIntent().getStringExtra("content");
        textTitle.setText(title);
        newsId = title.replaceAll("\\s+", "_"); // dùng tiêu đề làm ID

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(imageView);
        }

        webView.getSettings().setJavaScriptEnabled(false);
        webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);

        btnSend.setOnClickListener(v -> {
            String text = editComment.getText().toString().trim();
            if (!text.isEmpty()) {
                Comment comment = new Comment("User", text, System.currentTimeMillis());
                firestore.collection("comments")
                        .document(newsId)
                        .collection("list")
                        .add(comment)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
                            editComment.setText("");
                            loadComments();
                        });
            }
        });

        loadComments();
    }

    private void loadComments() {
        CollectionReference ref = firestore.collection("comments")
                .document(newsId)
                .collection("list");
        ref.orderBy("timestamp").get().addOnSuccessListener(query -> {
            commentList.clear();
            for (var doc : query.getDocuments()) {
                Comment c = doc.toObject(Comment.class);
                if (c != null) commentList.add(c);
            }
            commentAdapter.notifyDataSetChanged();
        });
    }
}
