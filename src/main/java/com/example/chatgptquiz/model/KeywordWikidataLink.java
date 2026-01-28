package com.example.chatgptquiz.model;

public class KeywordWikidataLink {
    private String keyword;
    private String wikidataUrl;
    
    public KeywordWikidataLink(String keyword, String wikidataUrl) {
        this.keyword = keyword;
        this.wikidataUrl = wikidataUrl;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public String getWikidataUrl() {
        return wikidataUrl;
    }
    
    public void setWikidataUrl(String wikidataUrl) {
        this.wikidataUrl = wikidataUrl;
    }
}
