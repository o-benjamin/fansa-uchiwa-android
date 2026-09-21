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
