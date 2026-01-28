package com.example.chatgptquiz.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.example.chatgptquiz.model.KeywordWikidataLink;
import com.example.chatgptquiz.model.Quiz;
import com.example.chatgptquiz.model.Video;
import com.example.chatgptquiz.service.WikidataService.WikidataInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatGptQuizService {

    private static final String OPENAI_API_URL =
            "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey = System.getenv("OPENAI_API_KEY");
    
    // ★ 設定値をapplication.propertiesから取得
    @Value("${app.enable-wikidata:false}")
    private boolean enableWikidata;
    
    @Value("${openai.model:gpt-4o}")
    private String openaiModel;
    
    @Value("${openai.max-tokens:5000}")
    private int maxTokens;
    
    @Autowired
    private VideoService videoService;
    
    @Autowired
    private WikidataService wikidataService;

    // ★ コンストラクタでタイムアウト設定
    public ChatGptQuizService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);  // 接続タイムアウト30秒
        factory.setReadTimeout(60000);     // 読み取りタイムアウト60秒
        
        this.restTemplate = new RestTemplate(factory);
        
        System.out.println("✅ ChatGptQuizService初期化完了");
    }

    /**
     * 1キーワード = 1問 クイズ生成（GPT-4o + Wikidata情報活用）
     * ★ WikidataInfoも一緒に返す
     */
    public Map<String, Object> generateSingleQuizWithWikidata(String keyword, String grade) {
        
        WikidataInfo wikidataInfo = null;
        
        // ★ Wikidata機能が有効な場合のみ取得を試みる
        if (enableWikidata) {
            System.out.println("🔍 Wikidata取得を試行中...");
            try {
                wikidataInfo = getWikidataInfoForKeyword(keyword, grade);
            } catch (Exception e) {
                System.err.println("⚠️ Wikidata取得をスキップ: " + e.getMessage());
            }
        } else {
            System.out.println("⏭️ Wikidata取得はスキップされました（設定で無効）");
        }
        
        String wikidataContext = formatWikidataForPrompt(wikidataInfo);

        String prompt = String.format(
            "「%s」を答えにした4択問題を1問作成してください。\n" +
            "対象学年：%s\n" +
            (!wikidataContext.isEmpty() 
                ? "\n=== 参考情報（問題作成のヒント） ===\n" + wikidataContext + "====================================\n\n"
                : "\n") +
            "必ず次のJSON形式のみで出力してください。\n" +
            "短い文章にしてください。\n" +
            "説明文・前置き・コードブロックは禁止です。\n\n" +
            "{\n" +
            "  \"question\": \"問題文\",\n" +
            "  \"choices\": [\"選択肢1\", \"選択肢2\", \"選択肢3\", \"選択肢4\"],\n" +
            "  \"answerIndex\": 0\n" +
            "}",
            keyword,
            grade
        );

        try {
            Map<String, Object> requestBody = Map.of(
                "model", openaiModel,  // ★ 設定ファイルから取得
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                ),
                "max_completion_tokens", maxTokens,  // ★ 設定ファイルから取得
                "response_format", Map.of(
                    "type", "json_schema",
                    "json_schema", Map.of(
                        "name", "quiz",
                        "schema", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "question", Map.of("type", "string"),
                                "choices", Map.of(
                                    "type", "array",
                                    "items", Map.of("type", "string"),
                                    "minItems", 4,
                                    "maxItems", 4
                                ),
                                "answerIndex", Map.of("type", "integer")
                            ),
                            "required", List.of("question", "choices", "answerIndex")
                        )
                    )
                )
            );

            String json = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            System.out.println("🚀 OpenAI APIリクエスト送信中...");
            System.out.println("   モデル: " + openaiModel);
            System.out.println("   最大トークン: " + maxTokens);
            
            String response = restTemplate.postForObject(
                OPENAI_API_URL,
                entity,
                String.class
            );
            
            // ★ レスポンスのnullチェック
            if (response == null || response.isEmpty()) {
                throw new RuntimeException("OpenAI APIからレスポンスがありませんでした");
            }

            System.out.println("🤖 OpenAI 生レスポンス:\n" + response);

            JsonNode root = objectMapper.readTree(response);
            String content = root
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

            if (content == null || content.isBlank()) {
                throw new RuntimeException("GPTが出力を返しませんでした（token不足）");
            }

            System.out.println("📄 生成クイズJSON:\n" + content);

            Quiz quiz = Quiz.fromJson(content);
            
            // ★ QuizとWikidataInfoを両方返す
            Map<String, Object> result = new HashMap<>();
            result.put("quiz", quiz);
            result.put("wikidataInfo", wikidataInfo);
            
            System.out.println("✅ クイズ生成成功");
            
            return result;

        } catch (ResourceAccessException e) {
            // ★ ネットワークエラー用の詳細メッセージ
            System.err.println("❌ OpenAI APIへの接続に失敗しました");
            System.err.println("原因: " + e.getMessage());
            System.err.println("\n【対処方法】");
            System.err.println("1. インターネット接続を確認してください");
            System.err.println("2. ファイアウォール/プロキシ設定を確認してください");
            System.err.println("3. 無線接続に切り替えてみてください");
            throw new RuntimeException("OpenAI APIへの接続エラー。ネットワーク設定を確認してください", e);
            
        } catch (HttpClientErrorException e) {
            // ★ APIエラー用の詳細メッセージ
            System.err.println("❌ OpenAI APIエラー: " + e.getStatusCode());
            System.err.println("レスポンス: " + e.getResponseBodyAsString());
            
            if (e.getStatusCode().value() == 401) {
                throw new RuntimeException("OpenAI APIキーが無効です。環境変数OPENAI_API_KEYを確認してください", e);
            } else if (e.getStatusCode().value() == 404) {
                throw new RuntimeException("指定されたモデル「" + openaiModel + "」が見つかりません。application.propertiesを確認してください", e);
            }
            
            throw new RuntimeException("OpenAI APIエラー: " + e.getResponseBodyAsString(), e);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("クイズ生成に失敗しました: " + e.getMessage(), e);
        }
    }
    
    /**
     * ★ 後方互換性のための従来メソッド
     */
    public Quiz generateSingleQuiz(String keyword, String grade) {
        Map<String, Object> result = generateSingleQuizWithWikidata(keyword, grade);
        return (Quiz) result.get("quiz");
    }
    
    /**
     * ★ キーワードに対応するWikidata情報を取得（クイズ生成直前に実行）
     * ★ WikidataInfoオブジェクトとして返す
     */
    private WikidataInfo getWikidataInfoForKeyword(String keyword, String grade) {
        try {
            System.out.println("\n" + "═".repeat(80));
            System.out.println("🔍 キーワード「" + keyword + "」のWikidata情報を取得中...");
            System.out.println("═".repeat(80));
            
            // 該当する学年の動画を取得
            List<Video> videos = videoService.getVideosByGrade(grade);
            
            // キーワードが含まれる動画を探す
            for (Video video : videos) {
                if (video.getKeyword() != null && video.getKeyword().contains(keyword)) {
                    
                    // ★ Wikidataリンクリストを取得
                    List<KeywordWikidataLink> linkList = video.getKeywordWikidataLinks();
                    
                    if (linkList != null && !linkList.isEmpty()) {
                        for (KeywordWikidataLink linkInfo : linkList) {
                            
                            // 該当キーワードのリンクを見つける
                            if (linkInfo.getKeyword().equals(keyword)) {
                                
                                // ★ ここでWikidata APIを呼び出す（クイズ生成直前）
                                String entityId = wikidataService.extractEntityId(linkInfo.getWikidataUrl());
                                
                                if (entityId != null) {
                                    WikidataInfo wikidataInfo = wikidataService.fetchWikidataInfo(entityId);
                                    
                                    if (wikidataInfo != null) {
                                        System.out.println("✅ Wikidata情報取得完了");
                                        System.out.println("═".repeat(80) + "\n");
                                        return wikidataInfo;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("⚠️ 該当するWikidata情報が見つかりませんでした");
            System.out.println("═".repeat(80) + "\n");
            
        } catch (Exception e) {
            System.err.println("⚠️ Wikidata情報取得エラー: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * ★ WikidataInfoをプロンプト用にフォーマット
     */
    private String formatWikidataForPrompt(WikidataInfo wikidataInfo) {
        if (wikidataInfo == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("名称: %s\n", wikidataInfo.getLabel()));
        
        // ★ description（短い説明）を追加
        if (wikidataInfo.getDescription() != null && !wikidataInfo.getDescription().isEmpty()) {
            sb.append(String.format("簡易説明: %s\n", wikidataInfo.getDescription()));
        }
        
        // ★ 別名（エイリアス）を追加
        if (wikidataInfo.getAliases() != null && !wikidataInfo.getAliases().isEmpty()) {
            sb.append(String.format("別名: %s\n", String.join(", ", wikidataInfo.getAliases())));
        }
        
        // ★ Wikipedia抜粋（詳細な定義文）を追加
        if (wikidataInfo.getWikipediaExtract() != null && !wikidataInfo.getWikipediaExtract().isEmpty()) {
            sb.append(String.format("詳細定義: %s\n", wikidataInfo.getWikipediaExtract()));
        }
        
        // プロパティ情報を追加
        if (wikidataInfo.getProperties() != null && !wikidataInfo.getProperties().isEmpty()) {
            sb.append("詳細情報:\n");
            for (WikidataService.WikidataProperty prop : wikidataInfo.getProperties()) {
                sb.append(String.format("  - %s: %s\n", prop.getName(), prop.getValue()));
            }
        }
        
        System.out.println("📋 参考情報の内容:");
        System.out.println(sb.toString());
        
        return sb.toString();
    }
}