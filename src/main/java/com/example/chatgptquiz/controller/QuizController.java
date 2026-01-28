package com.example.chatgptquiz.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.example.chatgptquiz.model.Quiz;
import com.example.chatgptquiz.service.ChatGptQuizService;
import com.example.chatgptquiz.service.WikidataService.WikidataInfo;

@Controller
@SessionAttributes({"correctCount", "totalCount"})
public class QuizController {

    private final ChatGptQuizService chatGptQuizService;

    public QuizController(ChatGptQuizService chatGptQuizService) {
        this.chatGptQuizService = chatGptQuizService;
    }

    @ModelAttribute("correctCount")
    public Integer correctCount() {
        return 0;
    }

    @ModelAttribute("totalCount")
    public Integer totalCount() {
        return 0;
    }

    @GetMapping("/quiz")
    public String quiz(
            @RequestParam("keyword") String keyword,
            @RequestParam("grade") String grade,
            @RequestParam(name = "index", defaultValue = "0") int index,
            @RequestParam(name = "isCorrect", required = false) Boolean isCorrect,
            @ModelAttribute("correctCount") Integer correctCount,
            @ModelAttribute("totalCount") Integer totalCount,
            Model model
    ) {

        System.out.println("🔍 受け取ったキーワード: [" + keyword + "]");
        System.out.println("🔍 学年: [" + grade + "]");
        System.out.println("🔍 インデックス: " + index);
        System.out.println("🔍 正解フラグ: " + isCorrect);

        // 最初の問題の場合、正解数をリセット
        if (index == 0) {
            correctCount = 0;
            totalCount = 0;
        } else if (isCorrect != null) {
            // 2問目以降で、前の問題の結果を記録
            if (isCorrect) {
                correctCount++;
            }
            totalCount++;
            System.out.println("📊 正解数更新: " + correctCount + " / " + totalCount);
        }

        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalCount", totalCount);

        if (keyword == null || keyword.isBlank()) {
            model.addAttribute("error", "キーワードが指定されていません");
            return "error";
        }

        List<String> keywordList = Arrays.asList(keyword.split("\\s+"));
        System.out.println("📝 キーワードリスト: " + keywordList);

        // 全てのキーワードを処理し終えたら完了ページへ
        if (index >= keywordList.size()) {
            model.addAttribute("grade", grade);
            return "quiz-finish";
        }

        String currentKeyword = keywordList.get(index);
        System.out.println("📌 現在のキーワード: [" + currentKeyword + "]");

        try {
            // ★ WikidataInfoも一緒に取得
            Map<String, Object> result = chatGptQuizService.generateSingleQuizWithWikidata(currentKeyword, grade);
            
            Quiz quiz = (Quiz) result.get("quiz");
            WikidataInfo wikidataInfo = (WikidataInfo) result.get("wikidataInfo");

            model.addAttribute("quiz", quiz);
            model.addAttribute("wikidataInfo", wikidataInfo); // ★ Wikidata情報を追加
            model.addAttribute("grade", grade);
            model.addAttribute("keyword", keyword);
            model.addAttribute("index", index + 1);
            model.addAttribute("currentIndex", index);
            model.addAttribute("totalQuestions", keywordList.size());

            System.out.println("✅ クイズ生成成功: " + quiz.getQuestion());
            
            // ★ Wikidata情報の取得状況をログ出力
            if (wikidataInfo != null) {
                System.out.println("✅ Wikidata情報取得成功: " + wikidataInfo.getLabel());
            } else {
                System.out.println("⚠️ Wikidata情報が取得できませんでした");
            }

        } catch (Exception e) {
            System.err.println("❌ クイズ生成エラー: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("quiz", null);
            model.addAttribute("wikidataInfo", null); // ★ エラー時はnull
            model.addAttribute("error", "クイズの生成に失敗しました: " + e.getMessage());
        }

        return "quiz";
    }

}