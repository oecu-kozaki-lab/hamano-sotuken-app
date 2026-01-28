package com.example.chatgptquiz.model;

import java.util.ArrayList;
import java.util.List;

public class Video {
    
    private String title;
    private String url;
    private String thumbnailUrl;
    private String grades;
    private String keyword;
    private List<KeywordWikidataLink> keywordWikidataLinks;  // ★ 変更：リンク情報だけ保持

    // 既存のコンストラクタ（後方互換性のため残す）
    public Video(String title, String url, String thumbnailUrl, String grades, String keyword) {
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.grades = grades;
        this.keyword = keyword;
        this.keywordWikidataLinks = new ArrayList<>();
    }

    // ★ 新しいコンストラクタ（Wikidataリンク情報を保持）
    public Video(String title, String url, String thumbnailUrl, String grades, String keyword, 
                 List<KeywordWikidataLink> keywordWikidataLinks) {
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.grades = grades;
        this.keyword = keyword;
        this.keywordWikidataLinks = keywordWikidataLinks != null ? keywordWikidataLinks : new ArrayList<>();
    }

    // デフォルトコンストラクタ
    public Video() {
        this.keywordWikidataLinks = new ArrayList<>();
    }

    // Getter / Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getGrades() {
        return grades;
    }

    public void setGrades(String grades) {
        this.grades = grades;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<KeywordWikidataLink> getKeywordWikidataLinks() {
        return keywordWikidataLinks;
    }

    public void setKeywordWikidataLinks(List<KeywordWikidataLink> keywordWikidataLinks) {
        this.keywordWikidataLinks = keywordWikidataLinks;
    }

    @Override
    public String toString() {
        return "Video{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", grades='" + grades + '\'' +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}