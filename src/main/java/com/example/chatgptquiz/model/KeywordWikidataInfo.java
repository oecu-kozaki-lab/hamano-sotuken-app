package com.example.chatgptquiz.model;

import com.example.chatgptquiz.service.WikidataService.WikidataInfo;

public class KeywordWikidataInfo {
    private String keyword;
    private WikidataInfo wikidataInfo;
    
    public KeywordWikidataInfo(String keyword, WikidataInfo wikidataInfo) {
        this.keyword = keyword;
        this.wikidataInfo = wikidataInfo;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public WikidataInfo getWikidataInfo() {
        return wikidataInfo;
    }
    
    public void setWikidataInfo(WikidataInfo wikidataInfo) {
        this.wikidataInfo = wikidataInfo;
    }
}