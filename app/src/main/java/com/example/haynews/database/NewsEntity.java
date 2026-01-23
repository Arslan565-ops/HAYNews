package com.example.haynews.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "news_table")
public class NewsEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String subtitle;
    public String imageUrl;
    public String content;
    public String source;
    public String author;
    public String url;
    public String category;
    public String publishedAt;
    public int credibilityScore;
    public boolean isBookmarked;
    public boolean isDownloaded;
    public String description;
    public long timestamp;

    public NewsEntity() {
        this.timestamp = System.currentTimeMillis();
        this.isBookmarked = false;
        this.isDownloaded = false;
        this.credibilityScore = 0;
    }
}

