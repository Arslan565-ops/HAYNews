package com.example.haynews.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface NewsDao {
    @Insert
    void insert(NewsEntity news);

    @Update
    void update(NewsEntity news);

    @Query("SELECT * FROM news_table WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    List<NewsEntity> getAllBookmarked();

    @Query("SELECT * FROM news_table WHERE isDownloaded = 1 ORDER BY timestamp DESC")
    List<NewsEntity> getAllDownloaded();

    @Query("SELECT * FROM news_table WHERE id = :newsId")
    NewsEntity getNewsById(long newsId);

    @Query("SELECT * FROM news_table WHERE url = :url LIMIT 1")
    NewsEntity getNewsByUrl(String url);

    @Query("UPDATE news_table SET isBookmarked = :isBookmarked WHERE url = :url")
    void updateBookmarkStatus(String url, boolean isBookmarked);

    @Query("UPDATE news_table SET isDownloaded = :isDownloaded WHERE url = :url")
    void updateDownloadStatus(String url, boolean isDownloaded);

    @Query("DELETE FROM news_table WHERE url = :url")
    void deleteByUrl(String url);

    @Query("DELETE FROM news_table")
    void deleteAll();
}

