package com.example.newsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "news.db";
    private static final int DATABASE_VERSION = 3; // ⚠️ tăng version để kích hoạt onUpgrade


    private static final String TABLE_NAME = "saved_news";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_LINK = "link";
    private static final String COL_PUBDATE = "pubDate";
    private static final String COL_IMAGE = "imageUrl";
    private static final String COL_CONTENT = "content"; // ✅ thêm cột nội dung
    private static final String COL_IS_LIKED = "isLiked";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_LINK + " TEXT, " +
                COL_PUBDATE + " TEXT, " +
                COL_IMAGE + " TEXT, " +
                COL_CONTENT + " TEXT, " +
                COL_IS_LIKED + " INTEGER DEFAULT 0)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // ✅ Thêm bài viết
    public boolean addNews(News news) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, news.getTitle());
        values.put(COL_DESCRIPTION, news.getDescription());
        values.put(COL_LINK, news.getLink());
        values.put(COL_PUBDATE, news.getPubDate());
        values.put(COL_IMAGE, news.getImageUrl());
        values.put(COL_CONTENT, news.getContent());

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }

    // ✅ Xóa bài viết theo link
    public void deleteNews(String link) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_LINK + "=?", new String[]{link});
        db.close();
    }

    // ✅ Lấy danh sách bài viết đã lưu
    public List<News> getAllSavedNews() {
        List<News> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                News news = new News(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LINK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PUBDATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT))
                );
                list.add(news);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    // ✅ Kiểm tra xem bài viết đã được lưu chưa
    public boolean isNewsSaved(String link) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_LINK + "=?", new String[]{link});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // ✅ Cập nhật trạng thái thích / bỏ thích
    public void setLiked(String link, boolean liked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_LIKED, liked ? 1 : 0);
        db.update(TABLE_NAME, values, COL_LINK + "=?", new String[]{link});
        db.close();
    }

    // ✅ Kiểm tra xem bài viết có được thích không
    public boolean isLiked(String link) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_IS_LIKED + " FROM " + TABLE_NAME + " WHERE " + COL_LINK + "=?", new String[]{link});
        boolean liked = false;
        if (cursor.moveToFirst()) {
            liked = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_LIKED)) == 1;
        }
        cursor.close();
        db.close();
        return liked;
    }

    // ✅ Lấy danh sách bài viết đã thích
    public List<News> getLikedNews() {
        List<News> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_IS_LIKED + "=1", null);
        if (cursor.moveToFirst()) {
            do {
                News news = new News(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LINK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PUBDATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT))
                );
                list.add(news);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

}
