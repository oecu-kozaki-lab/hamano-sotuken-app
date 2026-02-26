2025年　濱野凌吾　作成プログラム

注意
ローカルでしか動きません。
各自ダウンロードして、ChatGPTのAPIキーを環境変数に入力し実行してください。
このアプリケーションはOpenAI APIを使用するため、API使用料が発生します。

# 📚 NHK for School クイズアプリ

NHK for Schoolの動画に基づいた教育クイズアプリケーションです。OpenAI APIを使用してクイズを自動生成します。

## 🌟 機能

- 学年別（小3〜中3）の動画一覧表示
- キーワードベースの自動クイズ生成
- Wikidata連携による追加情報表示
- クイズの正解率トラッキング

## 🚀 デモ

デプロイされたアプリ: [こちらにURLを記入]

## 🛠️ 技術スタック

- **バックエンド**: Spring Boot (Java 17)
- **フロントエンド**: Thymeleaf, HTML/CSS/JavaScript
- **AI**: OpenAI API (GPT-4o)
- **データソース**: Wikidata API, Wikipedia API

## 📦 ローカル環境での実行

### 前提条件

- Java 17以上
- Maven 3.6以上
- OpenAI APIキー

### 1. リポジトリをクローン

```bash
git clone https://github.com/あなたのユーザー名/nhk-quiz-app.git
cd nhk-quiz-app
```

### 2. 環境変数を設定

```bash
# Windows
set OPENAI_API_KEY=sk-your-api-key-here

# Mac/Linux
export OPENAI_API_KEY=sk-your-api-key-here
```

### 3. アプリケーションを起動

```bash
mvn spring-boot:run
```

### 4. ブラウザでアクセス

```
http://localhost:8080
```

## ⚙️ 設定

`src/main/resources/application.properties` で以下の設定が可能です：

| 環境変数 | 説明 | デフォルト値 |
|---------|------|-------------|
| `OPENAI_API_KEY` | OpenAI APIキー（必須） | なし |
| `OPENAI_MODEL` | 使用するGPTモデル | `gpt-4o` |
| `OPENAI_MAX_TOKENS` | 最大トークン数 | `5000` |
| `ENABLE_WIKIDATA` | Wikidata機能の有効化 | `false` |
| `PORT` | サーバーポート | `8080` |

## 🌐 デプロイ方法

### Renderでのデプロイ

1. [Render](https://render.com)にサインアップ
2. 「New Web Service」を選択
3. このリポジトリを接続
4. 以下の設定を入力：

| 項目 | 設定値 |
|------|--------|
| **Build Command** | `mvn clean package -DskipTests` |
| **Start Command** | `java -jar target/chatgptquiz-0.0.1-SNAPSHOT.jar` |
| **Instance Type** | `Free` |

5. 環境変数を設定：
   - `OPENAI_API_KEY`: あなたのAPIキー
   - `ENABLE_WIKIDATA`: `false`
   - `OPENAI_MODEL`: `gpt-4o`

6. 「Create Web Service」をクリック

## 📝 使い方

1. トップページで学年を選択
2. 動画一覧から興味のある動画を選択
3. 「クイズを解く」ボタンをクリック
4. クイズに回答して学習

## 🔒 セキュリティ

- APIキーは環境変数で管理し、Gitにコミットしない
- `.gitignore`に`.env`ファイルを追加済み

## 📄 ライセンス

このプロジェクトはMITライセンスの下で公開されています。

## 👥 貢献

プルリクエストを歓迎します！

## 📧 お問い合わせ

問題や質問がある場合は、Issueを作成してください。

---


