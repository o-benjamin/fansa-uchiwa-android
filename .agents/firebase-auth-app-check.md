# 匿名ログインと App Check（AI 素材のサーバー）

AI 素材のサーバー（Cloud Functions）を、本物のアプリの利用者からだけ呼べるようにする仕組み（#337）。

- **呼ぶ場所**:
    - サーバーを呼ぶ直前に `AuthRepository.prepareForServerCall()` を呼び、返った利用者の ID を使ってください。App Check を有効にし、まだなら匿名でログインします。
    - アプリの起動時には呼ばないでください。AI 素材を使わない人のために Play Integrity を呼んだり、匿名の利用者を作ったりしないためです。

- **provider はビルドタイプで分ける**:
    - `release`：Play Integrity（`app/src/release/.../di/AppCheckProviderModule.kt`）。ストアから入れたアプリでしか通りません。
    - `debug` と `beta`：デバッグ用の provider（`app/src/appCheckDebug/.../di/AppCheckProviderModule.kt`）。ストアを通らないため Play Integrity は通りません。
    - Play Integrity は `releaseImplementation` なので、`beta` の R8 確認には入りません。この依存を足す・更新したときは `./gradlew :app:minifyReleaseWithR8` も通してください（`.agents/r8.md`）。

- **動かすための設定（コードの外）**:
    1. Firebase コンソールの Authentication で、ログイン方法の「匿名」を有効にする。無効のままだと `prepareForServerCall()` が例外を投げます。
    2. Play Console の「アプリの完全性」で、Play Integrity API を Firebase のプロジェクト（Google Cloud プロジェクト）につなぐ。
    3. Firebase コンソールの App Check で、Android アプリに Play Integrity を登録する。SHA-256 は Play Console の「アプリの署名」にあるアプリ署名鍵のものです（Google Play が署名し直すため、アップロード鍵のものでは通りません）。
    4. `debug` / `beta` で試すときは、初回の呼び出しで logcat に出るデバッグトークンを、Firebase コンソールの App Check の「デバッグトークンを管理」に登録する。

- **持ち主が変わるとき**:
    - App Check のデバッグトークンをすべて消してください。残すと、前の持ち主の端末から本番のサーバーを呼べてしまいます。
    - アプリを別の Play Console アカウントへ移す、または別の Firebase プロジェクトへ移すときは、上の 2・3 をやり直してください。アプリ署名鍵が変わらなければ SHA-256 は同じです。
    - 匿名の利用者の ID は Firebase のプロジェクトに属します。プロジェクトを替えると利用者の ID が変わり、ID にひもづくサーバー側の記録（無料の回数など）は引き継がれません。
