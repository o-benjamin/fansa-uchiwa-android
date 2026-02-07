---
name: Stamp and Font Usage Ranking - Investigation
about: スタンプとフォントの使用数ランキング機能の技術検討
title: '[Investigation] スタンプとフォントの使用数ランキング機能の技術検討'
labels: enhancement, investigation, analytics
assignees: o-benjamin

---

## 概要 / Overview
スタンプ（ステッカー）とフォントの使用回数をトラッキングし、ランキング形式で表示する機能の実装に向けた技術検討を行う。

Track stamp (sticker) and font usage counts and display them as rankings. This ticket is for technical investigation and design.

## 検討事項 / Investigation Items

### 1. データ設計 / Data Design
- [ ] 使用回数のトラッキング方法の決定
  - どのタイミングで使用回数をカウントするか（デコレーション追加時、保存時など）
  - データの永続化方法（Room DBに新しいテーブル追加 or 既存テーブル拡張）
- [ ] データモデルの設計
  - スタンプ使用履歴のデータ構造
  - フォント使用履歴のデータ構造
  - 集計結果のキャッシュ方法

### 2. 既存コードベースの調査 / Codebase Analysis
- [ ] 現在のデコレーション管理フロー確認
  - `EditViewModel` でのデコレーション追加ロジック
  - `LocalDatabaseRepository` でのデータ永続化ロジック
- [ ] スタンプとフォントの識別方法確認
  - `StickerAsset` enum（8種類）の使用状況
  - `FontFamilies` enum（31種類）の使用状況
  - `Decoration.Sticker` と `Decoration.Text` のデータ構造

### 3. UI/UX設計 / UI/UX Design
- [ ] ランキング表示画面の配置検討
  - Home画面に統合 or 新しい画面として追加
  - ランキング表示のデザイン（リスト形式、グリッド形式など）
- [ ] ランキング画面への導線設計
  - ナビゲーション構造の変更の有無
  - タブやメニューの追加検討

### 4. パフォーマンスとストレージ / Performance & Storage
- [ ] データ量の見積もり
  - 長期間使用時のデータ増加量予測
  - ストレージへの影響評価
- [ ] 集計処理のパフォーマンス検討
  - リアルタイム集計 vs 事前集計（バッチ処理）
  - バックグラウンドスレッドでの処理方式

### 5. プライバシーとデータ保護 / Privacy
- [ ] ローカルのみでのデータ保存確認
  - 現在のアプリはローカルのみでデータ保存（要確認）
  - クラウド同期の必要性検討
- [ ] プライバシーポリシーの更新必要性確認

## 技術スタック / Technical Stack
- **Database**: Room (SQLite)
- **Architecture**: MVVM + Repository Pattern
- **DI**: Hilt
- **Serialization**: kotlinx.serialization

## 参考情報 / References
- Current sticker types: `StickerAsset.kt` (8 types)
- Current font types: `FontFamilies.kt` (31 types)
- Decoration data model: `Decoration.kt` (sealed interface)
- Current DB schema: `FansaUchiwaDao.kt`, `FansaUchiwaDataEntity.kt`

## 成果物 / Deliverables
- [ ] データスキーマ設計書（コメントまたはドキュメント）
- [ ] UI/UXモックアップまたはワイヤーフレーム（必要に応じて）
- [ ] 技術的な実装方針の決定
- [ ] 実装チケットへの技術仕様の引継ぎ

## 備考 / Notes
このチケットは技術検討のみを目的としており、実装は別チケットで行います。
