# 📤 GitHubにアップロードする手順

このガイドに従って、プロジェクトをGitHubにアップロードしてください。

## 🎯 目標

プロジェクトをGitHubにアップロードして、誰でもアクセスできるようにする

---

## 📋 準備（完了済み）

以下のファイルが作成されています：

- ✅ `.gitignore` - Gitで管理しないファイルのリスト
- ✅ `system.properties` - Javaバージョンの指定
- ✅ `application.properties` - 環境変数対応版
- ✅ `README.md` - プロジェクトの説明
- ✅ `DEPLOY.md` - デプロイ手順

これらのファイルをプロジェクトのルートディレクトリに配置してください。

---

## 🚀 Step 1: GitHubアカウントの準備

### 1-1. GitHubアカウントの確認

既にアカウントがある場合はスキップ。ない場合：

1. [GitHub](https://github.com) にアクセス
2. 「Sign up」をクリック
3. メールアドレス、パスワードを入力
4. アカウントを作成

---

## 📦 Step 2: GitHubリポジトリの作成

### 2-1. 新しいリポジトリを作成

1. GitHubにログイン
2. 右上の「+」→「New repository」をクリック

### 2-2. リポジトリ情報を入力

| 項目 | 入力値 |
|------|--------|
| **Repository name** | `nhk-quiz-app`（または任意の名前） |
| **Description** | `NHK for School クイズアプリケーション` |
| **Public/Private** | **Public**（誰でもアクセス可能） |
| **Initialize this repository with:** | **何もチェックしない** |

### 2-3. リポジトリを作成

「Create repository」をクリック

---

## 💻 Step 3: ローカルでGitの設定

### 3-1. プロジェクトディレクトリに移動

コマンドプロンプト（Windows）またはターミナル（Mac/Linux）を開いて：

```bash
cd C:\path\to\your\chatgptquiz
```

**重要**: プロジェクトのルートディレクトリ（`pom.xml`があるディレクトリ）に移動してください。

### 3-2. 配置したファイルを確認

以下のファイルがプロジェクトルートにあることを確認：

```
chatgptquiz/
├── .gitignore              ← 追加
├── system.properties       ← 追加
├── README.md              ← 追加
├── DEPLOY.md              ← 追加
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       └── resources/
│           └── application.properties  ← 更新済み
└── target/
```

### 3-3. Gitの初期化

```bash
git init
```

出力例：
```
Initialized empty Git repository in C:/path/to/chatgptquiz/.git/
```

### 3-4. Gitユーザー情報の設定（初回のみ）

```bash
git config --global user.name "あなたの名前"
git config --global user.email "your-email@example.com"
```

**注意**: GitHubで使用しているメールアドレスを使用してください。

---

## 📤 Step 4: ファイルをコミット

### 4-1. ファイルをステージング

```bash
git add .
```

このコマンドで、すべてのファイル（`.gitignore`で除外されたファイル以外）がGitの管理下に追加されます。

### 4-2. ステージングの確認

```bash
git status
```

出力例：
```
On branch main

No commits yet

Changes to be committed:
  (use "git rm --cached <file>..." to unstage)
        new file:   .gitignore
        new file:   README.md
        new file:   DEPLOY.md
        new file:   pom.xml
        new file:   src/main/java/...
        ...
```

**重要確認ポイント**:
- ❌ `target/` ディレクトリが含まれていないこと
- ❌ `.idea/` ディレクトリが含まれていないこと
- ❌ `*.log` ファイルが含まれていないこと

もし含まれている場合は、`.gitignore` が正しく配置されているか確認してください。

### 4-3. コミット

```bash
git commit -m "Initial commit: NHK Quiz App"
```

出力例：
```
[main (root-commit) abc1234] Initial commit: NHK Quiz App
 XX files changed, XXX insertions(+)
 create mode 100644 .gitignore
 ...
```

---

## 🔗 Step 5: GitHubにプッシュ

### 5-1. ブランチ名を確認・変更

```bash
git branch -M main
```

### 5-2. リモートリポジトリを追加

GitHubで作成したリポジトリのURLを使用します。

```bash
git remote add origin https://github.com/あなたのユーザー名/nhk-quiz-app.git
```

**例**:
```bash
git remote add origin https://github.com/taro-yamada/nhk-quiz-app.git
```

### 5-3. プッシュ

```bash
git push -u origin main
```

初回プッシュ時、GitHubのログイン情報を求められる場合があります：

- **Username**: GitHubのユーザー名
- **Password**: 
  - ⚠️ **注意**: GitHubパスワードではなく、**Personal Access Token**が必要です

#### Personal Access Tokenの作成（必要な場合）

1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. 「Generate new token」→「Generate new token (classic)」
3. Note: `nhk-quiz-app-deploy`
4. Expiration: `90 days`（またはお好みの期間）
5. Select scopes: `repo`にチェック
6. 「Generate token」をクリック
7. 表示されたトークンをコピー（**一度しか表示されません！**）
8. パスワード欄にトークンを貼り付け

### 5-4. プッシュ成功の確認

```
Enumerating objects: XX, done.
Counting objects: 100% (XX/XX), done.
...
To https://github.com/あなたのユーザー名/nhk-quiz-app.git
 * [new branch]      main -> main
Branch 'main' set up to track remote branch 'main' from 'origin'.
```

---

## ✅ Step 6: GitHubで確認

### 6-1. ブラウザでリポジトリを開く

```
https://github.com/あなたのユーザー名/nhk-quiz-app
```

### 6-2. 確認項目

- ✅ ファイルが正しくアップロードされている
- ✅ README.mdが表示されている
- ✅ `target/`フォルダが含まれていない
- ✅ `.idea/`フォルダが含まれていない

---

## 🎉 完了！

GitHubへのアップロードが完了しました！

### 次のステップ

次は **DEPLOY.md** の手順に従って、Renderにデプロイしてください。

---

## 🔧 トラブルシューティング

### エラー: `remote origin already exists`

**解決方法**:
```bash
git remote remove origin
git remote add origin https://github.com/あなたのユーザー名/nhk-quiz-app.git
```

### エラー: `Permission denied`

**原因**: 認証エラー

**解決方法**:
1. Personal Access Tokenを作成
2. パスワードの代わりにトークンを使用

### エラー: `src refspec main does not match any`

**原因**: コミットがない

**解決方法**:
```bash
git commit -m "Initial commit"
git push -u origin main
```

### ファイルが大きすぎる（100MB超）

**原因**: Gitは100MB以上のファイルをプッシュできません

**解決方法**:
1. `.gitignore`に大きなファイルを追加
2. 既にコミットしている場合：
   ```bash
   git rm --cached large-file.jar
   git commit -m "Remove large file"
   ```

---

## 📝 今後の更新方法

コードを修正した後、以下のコマンドでGitHubに反映：

```bash
# 変更を確認
git status

# 変更をステージング
git add .

# コミット
git commit -m "Fix: バグ修正の説明"

# プッシュ
git push origin main
```

これでGitHubが自動的に更新され、Renderも自動的に再デプロイされます！

---

## 🆘 ヘルプが必要な場合

- [GitHub公式ドキュメント](https://docs.github.com)
- [Git基本コマンド](https://git-scm.com/docs)

何か問題があれば、エラーメッセージを教えてください！
