package com.example.chatgptquiz.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WikidataService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // ★ 設定値を追加
    @Value("${app.enable-wikidata:false}")
    private boolean enableWikidata;
    
    public WikidataService() {
        // ★ タイムアウト設定を追加
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 接続タイムアウト10秒
        factory.setReadTimeout(15000);     // 読み取りタイムアウト15秒
        
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
        
        System.out.println("✅ WikidataService初期化完了");
    }
    
    /**
     * WikidataのエンティティIDから情報を取得
     * @param entityId 例: "Q23718"
     * @return Wikidataの情報（ラベル、説明、主要プロパティ、Wikipedia抜粋）
     */
    public WikidataInfo fetchWikidataInfo(String entityId) {
        if (entityId == null || entityId.isEmpty()) {
            return null;
        }
        
        // ★ 機能が無効な場合は即座にnullを返す
        if (!enableWikidata) {
            System.out.println("⏭️ Wikidata取得はスキップされました（設定で無効）");
            return null;
        }
        
        try {
            // Wikidata APIのURL
            String apiUrl = String.format(
                "https://www.wikidata.org/wiki/Special:EntityData/%s.json",
                entityId
            );
            
            // User-Agentヘッダーを追加
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ChatGptQuizApp/1.0 (Educational Quiz Generator; contact@example.com)");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            System.out.println("🌐 Wikidata APIリクエスト: " + apiUrl);
            
            // RestTemplateでGETリクエスト（ヘッダー付き）
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            String response = responseEntity.getBody();
            JsonNode root = objectMapper.readTree(response);
            JsonNode entityNode = root.path("entities").path(entityId);
            
            WikidataInfo info = new WikidataInfo();
            info.setEntityId(entityId);
            
            // ★ 日本語ラベルを取得（日本語必須）
            JsonNode labels = entityNode.path("labels");
            if (labels.has("ja")) {
                info.setLabel(labels.path("ja").path("value").asText());
            } else {
                // ★ 日本語ラベルがなければnullを返す
                System.out.println("⚠️ 日本語ラベルなし: " + entityId + " (スキップ)");
                return null;
            }
            
            // ★ 日本語説明を取得（例：「1582年に日本の京都で発生した謀反・襲撃事件」）
            JsonNode descriptions = entityNode.path("descriptions");
            if (descriptions.has("ja")) {
                String description = descriptions.path("ja").path("value").asText();
                info.setDescription(description);
                System.out.println("📝 Description取得: " + description);
            } else {
                info.setDescription(null);
                System.out.println("⚠️ 日本語Descriptionなし");
            }
            
            // ★ 日本語の別名（エイリアス）を取得
            JsonNode aliases = entityNode.path("aliases");
            if (aliases.has("ja")) {
                List<String> aliasesList = new ArrayList<>();
                JsonNode jaAliases = aliases.path("ja");
                if (jaAliases.isArray()) {
                    for (JsonNode alias : jaAliases) {
                        aliasesList.add(alias.path("value").asText());
                    }
                }
                info.setAliases(aliasesList);
            }
            
            // ★ 日本語Wikipediaから抜粋を取得
            String wikipediaExtract = fetchWikipediaExtract(entityNode);
            if (wikipediaExtract != null && !wikipediaExtract.isEmpty()) {
                info.setWikipediaExtract(wikipediaExtract);
            }
            
            // 主要なプロパティを取得
            JsonNode claims = entityNode.path("claims");
            info.setProperties(extractProperties(claims, root));
            
            // デバッグ出力：取得したプロパティを表示
            System.out.println("=".repeat(60));
            System.out.println("🔍 Wikidata情報取得: " + entityId);
            System.out.println("名称: " + info.getLabel());
            System.out.println("説明: " + (info.getDescription() != null ? info.getDescription() : "(なし)"));
            
            if (info.getAliases() != null && !info.getAliases().isEmpty()) {
                System.out.println("別名: " + String.join(", ", info.getAliases()));
            }
            
            if (info.getWikipediaExtract() != null) {
                String extract = info.getWikipediaExtract();
                String preview = extract.length() > 100 ? extract.substring(0, 100) + "..." : extract;
                System.out.println("Wikipedia抜粋: " + preview);
            }
            
            if (!info.getProperties().isEmpty()) {
                System.out.println("取得したプロパティ (" + info.getProperties().size() + "件):");
                for (WikidataProperty prop : info.getProperties()) {
                    System.out.println("  ✓ " + prop.getName() + ": " + prop.getValue());
                }
            } else {
                System.out.println("  (プロパティなし)");
            }
            System.out.println("=".repeat(60));
            
            return info;
            
        } catch (RestClientException e) {
            // ★ ネットワークエラーの詳細を出力
            System.err.println("⚠️ Wikidata取得エラー (ID: " + entityId + ")");
            System.err.println("   原因: " + e.getClass().getSimpleName());
            System.err.println("   メッセージ: " + e.getMessage());
            System.err.println("   → 接続できない環境の可能性があります（スキップして続行）");
            return null;
            
        } catch (Exception e) {
            System.err.println("⚠️ Wikidata取得エラー (ID: " + entityId + "): " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 日本語Wikipediaから記事の抜粋を取得
     */
    private String fetchWikipediaExtract(JsonNode entityNode) {
        try {
            // 日本語Wikipediaのリンクを取得
            JsonNode sitelinks = entityNode.path("sitelinks");
            if (!sitelinks.has("jawiki")) {
                return null;
            }
            
            String jaTitle = sitelinks.path("jawiki").path("title").asText();
            
            // Wikipedia APIで抜粋を取得
            String apiUrl = String.format(
                "https://ja.wikipedia.org/w/api.php?action=query&prop=extracts&exintro=true&explaintext=true&titles=%s&format=json",
                jaTitle.replace(" ", "_")
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ChatGptQuizApp/1.0 (Educational Quiz Generator; contact@example.com)");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            String response = responseEntity.getBody();
            JsonNode root = objectMapper.readTree(response);
            JsonNode pages = root.path("query").path("pages");
            
            // 最初のページの抜粋を取得
            if (pages.isObject() && pages.size() > 0) {
                JsonNode firstPage = pages.elements().next();
                String extract = firstPage.path("extract").asText();
                
                // 最初の段落のみを取得（改行で分割して最初の部分）
                if (extract != null && !extract.isEmpty()) {
                    String[] paragraphs = extract.split("\n");
                    if (paragraphs.length > 0) {
                        return paragraphs[0].trim();
                    }
                }
            }
            
        } catch (RestClientException e) {
            System.err.println("⚠️ Wikipedia抜粋取得エラー（ネットワーク）: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("⚠️ Wikipedia抜粋取得エラー: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * WikidataのURLからエンティティIDを抽出
     * @param url 例: "http://www.wikidata.org/entity/Q23718"
     * @return エンティティID 例: "Q23718"
     */
    public String extractEntityId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // URLの最後の部分（Q〜）を抽出
        String[] parts = url.split("/");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            if (lastPart.matches("^Q\\d+$")) {
                return lastPart;
            }
        }
        
        return null;
    }
    
    /**
     * 主要なプロパティを抽出（日本語のみ）
     */
    private List<WikidataProperty> extractProperties(JsonNode claims, JsonNode root) {
        List<WikidataProperty> properties = new ArrayList<>();
        
        // よく使われる重要なプロパティのみを取得
        String[] importantProperties = {
            "P31",   // 分類（instance of）
            "P279",  // 上位クラス（subclass of）
            "P361",  // 一部（part of）
            "P17",   // 国（country）
            "P276",  // 場所（location）
            "P580",  // 開始時点（start time）
            "P582",  // 終了時点（end time）
            "P585",  // 時点（point in time）
            "P571",  // 設立（inception）
            "P569",  // 生年月日（date of birth）
            "P570"   // 没年月日（date of death）
        };
        
        for (String propertyId : importantProperties) {
            if (claims.has(propertyId)) {
                JsonNode propertyClaims = claims.get(propertyId);
                
                // 最初のクレームのみを取得
                if (propertyClaims.isArray() && propertyClaims.size() > 0) {
                    JsonNode firstClaim = propertyClaims.get(0);
                    
                    String propertyName = getPropertyLabel(propertyId);
                    String value = extractValue(firstClaim.path("mainsnak"), root);
                    
                    // ★ 日本語の値のみ追加
                    if (propertyName != null && value != null) {
                        properties.add(new WikidataProperty(propertyName, value));
                    }
                }
            }
        }
        
        return properties;
    }
    
    /**
     * プロパティIDからラベル（名称）を取得（日本語のみ）
     */
    private String getPropertyLabel(String propertyId) {
        // よく使われるプロパティのマッピング（日本語）
        switch (propertyId) {
            case "P31": return "分類";
            case "P279": return "上位クラス";
            case "P361": return "一部";
            case "P17": return "国";
            case "P276": return "場所";
            case "P580": return "開始時点";
            case "P582": return "終了時点";
            case "P585": return "時点";
            case "P571": return "設立";
            case "P569": return "生年月日";
            case "P570": return "没年月日";
            default: return null;
        }
    }
    
    /**
     * クレームから値を抽出
     */
    private String extractValue(JsonNode mainsnak, JsonNode root) {
        JsonNode datavalue = mainsnak.path("datavalue");
        
        if (datavalue.isMissingNode()) {
            return null;
        }
        
        String type = datavalue.path("type").asText();
        
        switch (type) {
            case "string":
            case "url":
            case "external-id":
                return datavalue.path("value").asText();
                
            case "wikibase-item":
                // エンティティIDからラベル（名称）を取得
                String entityId = datavalue.path("value").path("id").asText();
                return getEntityLabel(entityId, root);
                
            case "time":
                String time = datavalue.path("value").path("time").asText();
                // ★ 日付フォーマットを改善
                return formatDate(time);
                
            case "quantity":
                return datavalue.path("value").path("amount").asText();
                
            case "globe-coordinate":
                double lat = datavalue.path("value").path("latitude").asDouble();
                double lon = datavalue.path("value").path("longitude").asDouble();
                return String.format("緯度%.4f, 経度%.4f", lat, lon);
                
            case "monolingualtext":
                // 単言語テキスト（言語コード付きテキスト）
                String lang = datavalue.path("value").path("language").asText();
                String text = datavalue.path("value").path("text").asText();
                if ("ja".equals(lang)) {
                    return text;
                }
                return null;
                
            default:
                return null;
        }
    }
    
    /**
     * ★ 日付を読みやすい形式にフォーマット
     * 例：+1582-06-21T00:00:00Z → 1582年6月21日
     */
    private String formatDate(String time) {
        try {
            // +1582-06-21T00:00:00Z のような形式から日付部分を抽出
            String dateOnly = time.replace("+", "").substring(0, 10);
            String[] parts = dateOnly.split("-");
            
            if (parts.length >= 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                
                // 月日が00の場合は年のみ、日が00の場合は年月のみ表示
                if (month == 0) {
                    return String.format("%d年", year);
                } else if (day == 0) {
                    return String.format("%d年%d月", year, month);
                } else {
                    return String.format("%d年%d月%d日", year, month, day);
                }
            }
            
            return dateOnly;
            
        } catch (Exception e) {
            // エラー時は元の形式から+を削除して返す
            return time.replace("+", "").substring(0, 10);
        }
    }
    
    /**
     * エンティティIDからラベル（名称）を取得
     * ★ 日本語がない場合はnullを返す
     */
    private String getEntityLabel(String entityId, JsonNode root) {
        try {
            // 同じレスポンス内にある場合は取得
            JsonNode entity = root.path("entities").path(entityId);
            if (!entity.isMissingNode()) {
                JsonNode labels = entity.path("labels");
                // ★ 日本語のみ取得、なければnull
                if (labels.has("ja")) {
                    return labels.path("ja").path("value").asText();
                } else {
                    return null;  // ★ 日本語がなければnull
                }
            }
            
            // ★ ネットワークエラーが起きやすい環境では追加リクエストをスキップ
            if (!enableWikidata) {
                return null;
            }
            
            // なければ追加でAPIリクエスト
            String apiUrl = String.format(
                "https://www.wikidata.org/wiki/Special:EntityData/%s.json",
                entityId
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ChatGptQuizApp/1.0 (Educational Quiz Generator; contact@example.com)");
            HttpEntity<String> entity2 = new HttpEntity<>(headers);
            
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity2,
                String.class
            );
            
            String response = responseEntity.getBody();
            JsonNode newRoot = objectMapper.readTree(response);
            JsonNode newEntity = newRoot.path("entities").path(entityId);
            JsonNode labels = newEntity.path("labels");
            
            // ★ 日本語のみ取得、なければnull
            if (labels.has("ja")) {
                return labels.path("ja").path("value").asText();
            } else {
                return null;  // ★ 日本語がなければnull
            }
            
        } catch (RestClientException e) {
            System.err.println("⚠️ エンティティラベル取得エラー（ネットワーク） (" + entityId + "): " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("⚠️ エンティティラベル取得エラー (" + entityId + "): " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Wikidata情報を文字列にフォーマット（ChatGPTプロンプト用）
     */
    public String formatWikidataInfoForPrompt(List<WikidataInfo> infoList) {
        if (infoList == null || infoList.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < infoList.size(); i++) {
            WikidataInfo info = infoList.get(i);
            sb.append(String.format("[関連キーワード%d]\n", i + 1));
            sb.append(String.format("名称: %s\n", info.getLabel()));
            
            if (info.getDescription() != null && !info.getDescription().isEmpty()) {
                sb.append(String.format("説明: %s\n", info.getDescription()));
            }
            
            if (info.getAliases() != null && !info.getAliases().isEmpty()) {
                sb.append(String.format("別名: %s\n", String.join(", ", info.getAliases())));
            }
            
            // ★ Wikipedia抜粋を追加（詳細な定義文）
            if (info.getWikipediaExtract() != null && !info.getWikipediaExtract().isEmpty()) {
                sb.append(String.format("詳細定義: %s\n", info.getWikipediaExtract()));
            }
            
            if (info.getProperties() != null && !info.getProperties().isEmpty()) {
                sb.append("詳細情報:\n");
                for (WikidataProperty prop : info.getProperties()) {
                    sb.append(String.format("  - %s: %s\n", prop.getName(), prop.getValue()));
                }
            }
            
            if (i < infoList.size() - 1) {
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }
    
    // 内部クラス
    public static class WikidataInfo {
        private String entityId;
        private String label;
        private String description;
        private List<String> aliases;
        private String wikipediaExtract;  // ★ Wikipedia抜粋を追加
        private List<WikidataProperty> properties = new ArrayList<>();
        
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<String> getAliases() { return aliases; }
        public void setAliases(List<String> aliases) { this.aliases = aliases; }
        
        public String getWikipediaExtract() { return wikipediaExtract; }
        public void setWikipediaExtract(String wikipediaExtract) { this.wikipediaExtract = wikipediaExtract; }
        
        public List<WikidataProperty> getProperties() { return properties; }
        public void setProperties(List<WikidataProperty> properties) { this.properties = properties; }
    }
    
    public static class WikidataProperty {
        private String name;
        private String value;
        
        public WikidataProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public String getValue() { return value; }
        
        @Override
        public String toString() {
            return name + ": " + value;
        }
    }
}