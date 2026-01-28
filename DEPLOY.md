# 🚀 デプロイ手順ガイド

このアプリケーションをRenderにデプロイする詳細な手順です。

## 📋 事前準備

### 1. GitHubリポジトリの作成（完了）

✅ このリポジトリが既に作成されています

### 2. 必要なもの

- GitHubアカウント
- OpenAI APIキー（https://platform.openai.com/api-keys）
- メールアドレス（Render登録用）

## 🌐 Renderでのデプロイ手順

### Step 1: Renderアカウント作成

1. [Render](https://render.com)にアクセス
2. 右上の「Get Started」をクリック
3. 「Sign up with GitHub」を選択
4. GitHubでログイン・認証

### Step 2: 新しいWebサービスを作成

1. Renderダッシュボードで「New +」ボタンをクリック
2. 「Web Service」を選択
3. 「Connect a repository」で以下を実行：
   - 「Configure account」をクリック
   - GitHubで「Install」を承認
   - このリポジトリ（nhk-quiz-app）にチェック
   - 「Install」をクリック

### Step 3: リポジトリを選択

1. リポジトリリストから `nhk-quiz-app` を選択
2. 「Connect」をクリック

### Step 4: サービス設定

以下の情報を入力：

| 項目 | 入力値 |
|------|--------|
| **Name** | `nhk-quiz-app`（任意の名前） |
| **Region** | `Singapore` 🇸🇬（日本に最も近い） |
| **Branch** | `main` |
| **Runtime** | `Java` |
| **Build Command** | `mvn clean package -DskipTests` |
| **Start Command** | `java -jar target/chatgptquiz-0.0.1-SNAPSHOT.jar` |
| **Instance Type** | `Free`（無料プラン） |

### Step 5: 環境変数の設定（重要！）

「Environment」セクションで「Add Environment Variable」をクリックし、以下を追加：

#### 必須の環境変数

```
Key: OPENAI_API_KEY
Value: sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```
⚠️ **重要**: 実際のAPIキーに置き換えてください

#### オプション設定（推奨値）

```
Key: ENABLE_WIKIDATA
Value: false
```

```
Key: OPENAI_MODEL
Value: gpt-4o
```

```
Key: OPENAI_MAX_TOKENS
Value: 5000
```

### Step 6: デプロイ開始

1. 全ての設定を確認
2. 「Create Web Service」をクリック
3. デプロイが自動的に開始されます

## ⏱️ デプロイ時間

- 初回デプロイ: 約5〜10分
- ビルド時間: 約3〜5分
- 起動時間: 約30秒〜1分

## 📊 デプロイ状況の確認

### ログの確認方法

1. Renderダッシュボードで作成したサービスをクリック
2. 「Logs」タブを選択
3. リアルタイムでログが表示されます

### 成功の確認

以下のログが表示されれば成功：

```
✅ WikidataService初期化完了
✅ ChatGptQuizService初期化完了
Started ChatGptQuizApplication in X.XXX seconds
```

## 🌍 アクセスURL

デプロイが完了すると、以下の形式のURLが発行されます：

```
https://nhk-quiz-app.onrender.com
```

または

```
https://nhk-quiz-app-xxxx.onrender.com
```

このURLをブラウザで開いて動作を確認してください！

## 🔧 トラブルシューティング

### ビルドが失敗する場合

#### エラー: `Failed to execute goal`

**原因**: Mavenのビルドエラー

**解決方法**:
1. ローカルで `mvn clean package` を実行
2. エラーがないか確認
3. 修正してGitHubにプッシュ

#### エラー: `Connection refused`

**原因**: 環境変数PORTが設定されていない

**解決方法**:
- `application.properties`に `server.port=${PORT:8080}` があることを確認

### アプリケーションが起動しない場合

#### エラー: `OPENAI_API_KEY not found`

**原因**: 環境変数が設定されていない

**解決方法**:
1. Renderダッシュボード → サービス → Environment
2. `OPENAI_API_KEY` を追加
3. 「Save Changes」をクリック
4. 自動的に再デプロイされます

#### エラー: `OutOfMemoryError`

**原因**: 無料プランのメモリ不足

**解決方法**:
- Start Commandを以下に変更：
  ```
  java -Xmx450m -jar target/chatgptquiz-0.0.1-SNAPSHOT.jar
  ```

## 📈 無料プランの制限

### Renderの無料プラン仕様

- **メモリ**: 512MB
- **CPU**: 共有
- **稼働時間**: 月750時間まで
- **スリープ**: 15分間アクセスがないとスリープ
- **起動時間**: スリープからの復帰に30秒〜1分

### スリープ対策

無料プランではアクセスがないとスリープします。以下の方法で対策可能：

1. **定期的なアクセス**（推奨）
   - UptimeRobotなどの監視サービスで5分ごとにアクセス

2. **有料プランへアップグレード**
   - $7/月 でスリープなし

## 🔄 更新方法

コードを修正してGitHubにプッシュすると、自動的に再デプロイされます：

```bash
git add .
git commit -m "Update feature"
git push origin main
```

Renderが自動的に：
1. 変更を検知
2. ビルド開始
3. デプロイ実行

## 🎯 確認チェックリスト

デプロイ後、以下を確認してください：

- [ ] URLにアクセスできる
- [ ] トップページが表示される
- [ ] 学年選択ができる
- [ ] 動画一覧が表示される
- [ ] クイズが正常に生成される
- [ ] 回答が正しく判定される

## 💰 料金について

### OpenAI API使用料

- GPT-4o: $2.50 / 1M入力トークン
- GPT-4o: $10.00 / 1M出力トークン

**目安**:
- 1クイズあたり約500トークン
- 100クイズで約$0.50〜$1.00

### Render料金

- 無料プラン: $0
- Starter: $7/月（スリープなし）

## 🆘 サポート

問題が発生した場合：

1. **Renderのログを確認**
2. **GitHubのIssueを確認**
3. **Render公式ドキュメント**: https://render.com/docs
4. **このリポジトリのIssueを作成**

## ✅ デプロイ成功！

おめでとうございます！🎉

あなたのアプリケーションが世界中からアクセスできるようになりました。

URLを友達や同僚と共有して、フィードバックをもらいましょう！
