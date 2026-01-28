package com.example.chatgptquiz.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.chatgptquiz.model.Video;
import com.example.chatgptquiz.service.VideoService;

@Controller
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/videos")
    public String videoList(
            @RequestParam(value = "grade", required = false) String grade,
            Model model
    ) {
        List<Video> videos;

        if (grade != null && !grade.isEmpty()) {
            videos = videoService.getVideosByGrade(grade);
        } else {
            videos = videoService.getAllVideos();
        }

        model.addAttribute("videos", videos);
        model.addAttribute("selectedGrade", grade);

        return "videos";
    }

    /**
     * 動画からクイズを開始（URLエンコード対応）
     */
    @GetMapping("/video/quiz")
    public String videoQuiz(
            @RequestParam("keyword") String keyword,
            @RequestParam("grade") String grade
    ) {
        try {
            // 日本語をURLエンコード
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            String encodedGrade = URLEncoder.encode(grade, "UTF-8");
            
            return "redirect:/quiz?keyword=" + encodedKeyword + 
                   "&grade=" + encodedGrade + "&index=0";
                   
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "error";
        }
    }
}