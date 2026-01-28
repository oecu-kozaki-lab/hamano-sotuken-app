package com.example.chatgptquiz.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.chatgptquiz.model.Video;

@Service
public class VideoService {

    private final List<Video> videos;

    // コンストラクタで VideoCsvLoader を注入
    public VideoService(VideoCsvLoader videoCsvLoader) {
        this.videos = videoCsvLoader.load();
    }

    public List<Video> getVideosByGrade(String grade) {

        List<Video> result = videos.stream()
            .filter(v -> v.getGrades().contains(grade)) // ★学年フィルタ
            .collect(Collectors.toList());

        System.out.println("🎯 選択学年: " + grade);
        System.out.println("📺 ヒット動画数: " + result.size());

        return result;
    }

    // 全動画を取得
    public List<Video> getAllVideos() {
        return videos;
    }

    // タイトルで検索
    public List<Video> searchByTitle(String title) {
        return videos.stream()
            .filter(v -> v.getTitle().contains(title))
            .collect(Collectors.toList());
    }
}