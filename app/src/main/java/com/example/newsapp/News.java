package com.example.newsapp;

public class News {
    private String title;
    private String description;
    private String link;
    private String pubDate;
    private String imageUrl;
    private String content; // ✅ thêm trường content

    public News(String title, String description, String link, String pubDate, String imageUrl, String content) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.pubDate = pubDate;
        this.imageUrl = imageUrl;
        this.content = content;
    }

    // Giữ nguyên constructor cũ để không lỗi nếu nơi khác chưa truyền content
    public News(String title, String description, String link, String pubDate, String imageUrl) {
        this(title, description, link, pubDate, imageUrl, "");
    }

    public String getTitle(){

        return title;
    }

public String getDescription() {
    return description;
}

public String getLink() {
    return link;
}

public String getPubDate() {
    return pubDate;
}

public String getImageUrl() {
    return imageUrl;
}

public String getContent() {
    return content;
}
}
