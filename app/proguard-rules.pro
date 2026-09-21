# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ---------------------------------------------------------------------------
# Crashlytics
# スタックトレースを mapping ファイルで復元できるよう、行番号情報を保持する。
# ---------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep public class * extends java.lang.Exception

# 保存データやリフレクションで名前が参照されるクラスは、ここにクラス名を書かず
# クラス側に androidx.annotation.Keep を付与して保持すること。

# ---------------------------------------------------------------------------
# Play In-App Review（review-ktx）
# review-ktx がコンパイル時だけの注釈 NoNullnessRewrite を参照しており、
# クラスパスにないため R8 が Missing class で止まる。実行時には参照されないので警告を抑止する。
# AGP が生成した build/outputs/mapping/release/missing_rules.txt の提案どおり（review-ktx 2.0.2 時点、
# gradle/libs.versions.toml の playReview）。上げたらこの行を外し、:app:bundleRelease が通るか試す。
# ---------------------------------------------------------------------------
-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite
