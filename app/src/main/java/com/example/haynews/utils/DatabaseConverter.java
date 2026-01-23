package com.example.haynews.utils;

import com.example.haynews.NewsItem;
import com.example.haynews.database.NewsEntity;

public class DatabaseConverter {
    public static NewsEntity newsItemToEntity(NewsItem item) {
        NewsEntity entity = new NewsEntity();
        entity.title = item.title;
        entity.subtitle = item.subtitle;
        entity.imageUrl = item.imageUrl;
        entity.content = item.content;
        entity.source = item.source;
        entity.author = item.author;
        entity.url = item.url;
        entity.category = item.category;
        entity.publishedAt = item.publishedAt;
        entity.credibilityScore = item.credibilityScore;
        entity.isBookmarked = item.isBookmarked;
        entity.isDownloaded = item.isDownloaded;
        entity.description = item.description;
        return entity;
    }

    public static NewsItem entityToNewsItem(NewsEntity entity) {
        NewsItem item = new NewsItem();
        item.title = entity.title;
        item.subtitle = entity.subtitle;
        item.imageUrl = entity.imageUrl;
        item.content = entity.content;
        item.source = entity.source;
        item.author = entity.author;
        item.url = entity.url;
        item.category = entity.category;
        item.publishedAt = entity.publishedAt;
        item.credibilityScore = entity.credibilityScore;
        item.isBookmarked = entity.isBookmarked;
        item.isDownloaded = entity.isDownloaded;
        item.description = entity.description;
        return item;
    }
}

