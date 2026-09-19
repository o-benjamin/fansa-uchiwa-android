# Android CI の使い方

`.github/workflows/android-ci.yml` は、ブランチへの push のたびにユニットテストと Android Lint を実行する。
CI はレビューの観点ではなく、**実装中に使う確認手段**として扱う。ローカルでフルビルドする代わりに、push して CI の結果で確かめる。

## 実装の流れ

1. ブランチを切ったら、最初のまとまったコミットで push し、**Draft PR** を作る（`gh pr create --draft`）。
2. 以降はコミットを push するたびに CI の結果を待つ。
   - `gh pr checks --watch` （PR がある場合）
   - `gh run watch $(gh run list --branch <ブランチ名> --limit 1 --json databaseId --jq '.[0].databaseId')` （PR がない場合）
3. 失敗したら、原因を読んで直してから次の実装に進む。
   - `gh run view --log-failed` で失敗したステップのログを見る
   - テストの詳細や Lint のレポートは、失敗時にアップロードされる `reports` Artifact にある（`gh run download <run-id> -n reports`）
4. CI が緑になってから Draft を外し、レビューを依頼する（`gh pr ready`）。

## 守ること

- CI が赤のまま次の作業を積み上げない。赤のブランチはレビューに回さない。
- テストや Lint を通すために、テストの削除・`@Ignore`・`@Suppress`・Lint baseline の追加をしない。仕様が変わってテストが古くなった場合は、どのコミットで仕様が変わったかを PR に書いたうえでテストを直す。
- Lint が SDK バージョンの判定を理解できない場合（`NewApi` の誤検知）は、判定用の関数に `@ChecksSdkIntAtLeast` を付ける。
- CI では秘密情報を使わない（リポジトリが公開のため）。`google-services.json` と release 用の広告 ID は、ワークフローの中でダミーを生成している。新しく `local.properties` のキーを必須にした場合は、ワークフローのダミー値も追加する。
- 手元で素早く確かめたいときは、変更に関係するテストだけを実行してよい（`./gradlew :app:testDebugUnitTest --tests '<クラス名>'`）。
