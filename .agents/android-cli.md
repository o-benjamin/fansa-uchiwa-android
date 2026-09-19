# Android CLI で動作を確かめる

Android CLI（`android` コマンド）を使い、実装中に自分でアプリを動かして確かめる。オーナーの手元で初めて画面の崩れやクラッシュに気づく、という状態をなくすため。

## 準備（初回のみ）

- `android --version` で入っているか確かめる。なければ https://developer.android.com/tools/agents から入れる
- 公式のスキルを入れる：`android skills add --skill=android-cli --agent=claude-code --project=.`（`.claude/skills/android-cli/` に入る。Google の配布物のためコミットしない）
- 使うエミュレータ（`android emulator list` で確認。なければ `android emulator create` で作る）
  - `Pixel_10`（API 36.1 = targetSdk）：ふだんの確認
  - `Pixel_4`（API 29 = minSdk）：API 33 以上でしか動かない機能（ぷくぷく効果など）の分岐を確かめるとき

## 実装の前

- Android の API・ライブラリを新しく使うとき、書き方に自信がないときは、記憶で書かずに `android docs search <キーワード>` で調べ、`android docs fetch <URL>` で本文を読む（非推奨になった API や移行ガイドを見落とさないため）

## 実装したあと

画面・操作・広告・保存など、動きが変わる変更をしたら、PR を出す前に必ず確かめる。

1. `android emulator start Pixel_10`（起動が終わるまで待って戻る）
2. `./gradlew :app:assembleDebug` でビルドし、`android run --device=<serial> --apks=app/build/outputs/apk/debug/app-debug.apk --activity=com.fansauchiwa.MainActivity` で起動する（serial は `adb devices` で確認）
3. 変更した画面まで操作し、確かめる
   - まず `android layout --device=<serial>`（画面の要素と座標の JSON）。操作後の差分だけ見たいときは `--diff`
   - 見た目（色・重なり・画像）は `adb -s <serial> exec-out screencap -p > <ファイル>.png` で撮り、画像を必ず見る
   - 操作は `adb -s <serial> shell input tap <x> <y>` / `input text` / `input swipe`
4. `adb -s <serial> logcat -d | grep -E "FATAL|AndroidRuntime: FATAL"` でクラッシュがないか確かめる
5. 関係する journey（`journeys/*.xml`）があれば、`.claude/skills/android-cli/references/journeys.md` の手順で実行する
6. PR 本文の「確認したこと」に、端末（エミュレータ名と API レベル）・確かめた操作・結果を書く

## 注意

- 実機がつながっていると、`android screen capture` は端末を選べずに失敗する。スクリーンショットは上の `adb -s <serial> exec-out screencap -p` を使う
- ツールチップやポップアップは `android layout` に出ないことがある。画面の状態が layout と合わないときはスクリーンショットで確かめる
- アイコンだけのボタンや色の選択肢には `contentDescription` がなく、layout に名前が出ない。座標とスクリーンショットで特定する（新しく作る UI では `contentDescription` を付ける。`.agents/ui.md` の Semantics の規約に従う）
- debug ビルドはテスト広告を使う。本番の広告を自分でタップしない

## journey の追加

主要な操作の流れは `journeys/` に XML で置く（形式は `.claude/skills/android-cli/references/journeys.md`）。新しい画面や、収益・保存に関わる流れを追加したら journey も追加する。書いたら一度実行して、成功することを確かめてからコミットする。
