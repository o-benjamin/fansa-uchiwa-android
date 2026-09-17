import os

# 使い方
# python3 export_code.py

# ==========================================
# 設定項目
# ==========================================
# 解析対象にする拡張子
TARGET_EXTENSIONS = {
    '.kt', '.java', '.xml', '.gradle', '.kts', '.toml', '.pro', '.properties'
}

# 完全に無視するファイル名
IGNORE_FILES = {
    'local.properties', 'gradlew', 'gradlew.bat'
}

# 無視するディレクトリ名（部分一致ではなく完全一致）
IGNORE_DIRS = {
    '.git', '.gradle', '.idea', 'build', 'captures', '.settings', 'out'
}

# 出力ファイル名
OUTPUT_FILE = 'android_source_summary.txt'

def is_target_file(file_path):
    """対象ファイルかどうかを判定する"""
    filename = os.path.basename(file_path)
    if filename in IGNORE_FILES:
        return False

    _, ext = os.path.splitext(filename)
    return ext in TARGET_EXTENSIONS

def export_repository_code(root_dir):
    print(f"スキャンを開始します: {root_dir}")

    with open(OUTPUT_FILE, 'w', encoding='utf-8') as outfile:
        file_count = 0

        for dirpath, dirnames, filenames in os.walk(root_dir):
            # 無視するディレクトリを探索対象から外す (破壊的変更でos.walkを制御)
            dirnames[:] = [d for d in dirnames if d not in IGNORE_DIRS]

            for filename in filenames:
                full_path = os.path.join(dirpath, filename)

                if is_target_file(full_path):
                    # 出力ファイル内での相対パスを計算
                    relative_path = os.path.relpath(full_path, root_dir)

                    # AIがファイル構造を認識しやすいように区切り文字を入れる
                    outfile.write("\n" + "="*80 + "\n")
                    outfile.write(f"FILE: {relative_path}\n")
                    outfile.write("="*80 + "\n\n")

                    try:
                        with open(full_path, 'r', encoding='utf-8', errors='replace') as infile:
                            outfile.write(infile.read())
                        file_count += 1
                        print(f"追加: {relative_path}")
                    except Exception as e:
                        print(f"エラー (スキップ): {relative_path} - {e}")

        print("\n" + "-"*40)
        print(f"完了! {file_count} 個のファイルを {OUTPUT_FILE} に出力しました。")

if __name__ == '__main__':
    # スクリプトを置いた場所（のカレントディレクトリ）を対象にする場合
    # Androidプロジェクトのルートディレクトリにこのスクリプトを置いて実行してください
    current_directory = os.getcwd()
    export_repository_code(current_directory)