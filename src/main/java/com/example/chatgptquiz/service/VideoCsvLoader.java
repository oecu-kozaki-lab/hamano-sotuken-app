package com.example.chatgptquiz.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.chatgptquiz.model.KeywordWikidataLink;
import com.example.chatgptquiz.model.Video;

@Service
public class VideoCsvLoader {

    public List<Video> load() {

        List<Video> list = new ArrayList<>();

        try {
            ClassPathResource resource =
                    new ClassPathResource("nhkforschool_history_banngumi.tsv.csv");

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), "UTF-8")
            );

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {

                if (first) {
                    first = false;
                    continue;
                }

                String[] cols = line.split(",", -1);

                // デバッグ出力：カラム数を確認
                if (list.size() < 3) {
                    System.out.println("📋 カラム数: " + cols.length);
                    System.out.println("📋 11番目の値: [" + (cols.length > 11 ? cols[11] : "存在しない") + "]");
                }

                String title = cols[2];
                String url = cols[5];
                String thumbnailUrl = cols[6];
                String grades = cols[7];
                String keyword = cols.length > 11 ? cols[11] : "";
                
                // Wikidataリンクを安全に取得（空欄チェック付き）
                String link1 = (cols.length > 16 && !cols[16].isEmpty()) ? cols[16] : null;
                String link2 = (cols.length > 17 && !cols[17].isEmpty()) ? cols[17] : null;
                String link3 = (cols.length > 18 && !cols[18].isEmpty()) ? cols[18] : null;
                String link4 = (cols.length > 19 && !cols[19].isEmpty()) ? cols[19] : null;
                String link5 = (cols.length > 20 && !cols[20].isEmpty()) ? cols[20] : null;

                if (title.isEmpty() || url.isEmpty() || grades.isEmpty()) {
                    continue;
                }

                // キーワードが空の場合もデバッグ出力
                if (keyword.isEmpty()) {
                    System.out.println("⚠️ キーワードが空: " + title);
                }

                // ★ キーワードを分割（スペース区切り）
                String[] keywords = keyword.split("\\s+");
                
                // ★ Wikidataリンクの配列
                String[] wikidataLinks = {link1, link2, link3, link4, link5};
                
                // ★ キーワードとWikidataリンクのペアリストを作成（情報は取得しない）
                List<KeywordWikidataLink> keywordWikidataLinkList = new ArrayList<>();
                
                for (int i = 0; i < Math.min(keywords.length, wikidataLinks.length); i++) {
                    String kw = keywords[i];
                    String link = wikidataLinks[i];
                    
                    if (kw != null && !kw.isEmpty() && link != null && !link.isEmpty()) {
                        // ★ リンク情報だけ保存（Wikidata APIは呼ばない）
                        keywordWikidataLinkList.add(new KeywordWikidataLink(kw, link));
                        System.out.println("🔗 キーワード「" + kw + "」のリンクを保存: " + link);
                    }
                }

                list.add(new Video(
                        title,
                        url,
                        thumbnailUrl,
                        grades,
                        keyword,
                        keywordWikidataLinkList
                ));
            }

            br.close();

            System.out.println("✅ 動画データ読み込み完了: " + list.size() + "件");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("CSVファイルの読み込みに失敗しました", e);
        }

        return list;
    }

    public List<Video> searchByTitle(String searchTitle) {
        List<Video> allVideos = load();
        List<Video> result = new ArrayList<>();

        for (Video video : allVideos) {
            if (video.getTitle().contains(searchTitle)) {
                result.add(video);
            }
        }

        return result;
    }

    public List<Video> filterByGrade(String grade) {
        List<Video> allVideos = load();
        List<Video> result = new ArrayList<>();

        for (Video video : allVideos) {
            if (video.getGrades().equals(grade)) {
                result.add(video);
            }
        }

        return result;
    }
}