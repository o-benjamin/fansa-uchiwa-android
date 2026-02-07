# 使用数ランキング機能 Issue テンプレート / Usage Ranking Feature Issue Templates

## 概要 / Overview

このディレクトリには、スタンプとフォントの使用数ランキング機能を実装するための GitHub Issue テンプレートが含まれています。

This directory contains GitHub Issue templates for implementing the stamp and font usage ranking feature.

## テンプレート一覧 / Template List

### 1. Investigation Ticket (調査チケット)
**ファイル名**: `ranking-feature-investigation.md`

**目的**: 技術的な実装方針の検討と設計を行うためのチケット

**内容**:
- データ設計（Room DB スキーマ、データモデル）
- 既存コードベースの調査
- UI/UX 設計
- パフォーマンスとストレージの考慮事項
- プライバシーとデータ保護

### 2. Implementation Ticket (実装チケット)
**ファイル名**: `ranking-feature-implementation.md`

**目的**: 実際の機能実装を行うためのチケット

**内容**:
- Phase 1: データ層の実装 (Room DB, Repository)
- Phase 2: ドメイン層の実装 (データモデル、集計ロジック)
- Phase 3: UI 層の実装 (ViewModel, Composable)
- Phase 4: リソースとスタイリング
- Phase 5: テストとバリデーション
- Phase 6: ドキュメント

## 使用方法 / How to Use

### GitHub UI から Issue を作成する場合

1. GitHub リポジトリの **Issues** タブを開く
2. **New issue** ボタンをクリック
3. テンプレート選択画面で以下のいずれかを選択:
   - **Stamp and Font Usage Ranking - Investigation** (調査チケット)
   - **Stamp and Font Usage Ranking - Implementation** (実装チケット)
4. テンプレートに従って詳細を記入
5. **Submit new issue** で作成

### 推奨フロー / Recommended Workflow

1. **まず Investigation Ticket を作成**
   - データ設計と技術方針を決定
   - 実装の前提条件を明確化
   
2. **Investigation 完了後に Implementation Ticket を作成**
   - 調査結果に基づいて実装タスクを実行
   - 各 Phase ごとに進捗を管理

## 技術的背景 / Technical Context

### 現在のコードベース
- **アーキテクチャ**: MVVM + Repository Pattern
- **UI**: Jetpack Compose Material 3
- **データベース**: Room (SQLite)
- **DI**: Hilt
- **スタンプ種類**: 8 種類 (`StickerAsset.kt`)
- **フォント種類**: 31 種類 (`FontFamilies.kt`)

### 実装対象
- スタンプ（ステッカー）とフォントの使用回数トラッキング
- 使用回数に基づくランキング表示
- Home 画面またはランキング専用画面での表示

## ラベル / Labels

### Investigation Ticket
- `enhancement`: 新機能
- `investigation`: 調査・検討
- `analytics`: 分析・統計関連

### Implementation Ticket
- `enhancement`: 新機能
- `feature`: 機能実装
- `HomeScreen`: Home 画面関連

## 参考資料 / References

- [既存の Issue テンプレート](./)
- [アーキテクチャガイド](../../.github/copilot-instructions.md)
- [プライバシーポリシー](../../docs/privacy-policy.html)

---

**作成日 / Created**: 2026-02-07
