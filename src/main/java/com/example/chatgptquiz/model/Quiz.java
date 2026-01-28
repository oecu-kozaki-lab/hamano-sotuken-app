package com.example.chatgptquiz.model;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Quiz {

    private String question;
    private List<String> choices;
    private int answerIndex;

    // デフォルトコンストラクタ（Jacksonが必要とする）
    public Quiz() {
    }

    // getter / setter
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }

    public void setAnswerIndex(int answerIndex) {
        this.answerIndex = answerIndex;
    }

    /**
     * JSON文字列からQuizオブジェクトを生成
     * OpenAI APIが返すJSON（時々 ```json ``` で囲まれている）をパース
     */
    public static Quiz fromJson(String json) {
        try {
            // JSONの前後にある余計な文字列を削除（マークダウン対応）
            String cleanJson = json.trim();
            
            // ```json で始まる場合は削除
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            // ``` で始まる場合は削除
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            // ``` で終わる場合は削除
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            
            cleanJson = cleanJson.trim();

            // Jackson を使って JSON → Quiz オブジェクトに変換
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(cleanJson, Quiz.class);
            
        } catch (Exception e) {
            // パースに失敗したら詳細なエラーを投げる
            throw new RuntimeException("JSONのパースに失敗しました: " + json, e);
        }
    }
}