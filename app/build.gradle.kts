import com.android.build.api.dsl.ApplicationBuildType
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

// AdMob のテスト用 ID を設定する。
fun ApplicationBuildType.useTestAdUnits() {
    manifestPlaceholders["ADMOB_APPLICATION_ID"] = "ca-app-pub-3940256099942544~3347511713"
    buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
    buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
    buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
}

android {
    namespace = "com.fansauchiwa"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fansauchiwa"
        minSdk = 29
        targetSdk = 36
        versionCode = 31
        versionName = "2.11.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            useTestAdUnits()
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = true
            }

            val applicationId = localProperties.getProperty("ADMOB_APPLICATION_ID_RELEASE")
                ?: throw GradleException(
                    "ADMOB_APPLICATION_ID_RELEASE not found in local.properties. " +
                            "Please add it to continue building the release version."
                )
            manifestPlaceholders["ADMOB_APPLICATION_ID"] = applicationId

            val rewardedAdId = localProperties.getProperty("ADMOB_REWARDED_ID_RELEASE")
                ?: throw GradleException(
                    "ADMOB_REWARDED_ID_RELEASE not found in local.properties. " +
                            "Please add it to continue building the release version."
                )
            buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"$rewardedAdId\"")

            val bannerAdId = localProperties.getProperty("ADMOB_BANNER_ID_RELEASE")
                ?: throw GradleException(
                    "ADMOB_BANNER_ID_RELEASE not found in local.properties. " +
                            "Please add it to continue building the release version."
                )
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"$bannerAdId\"")

            val interstitialAdId = localProperties.getProperty("ADMOB_INTERSTITIAL_ID_RELEASE")
                ?: throw GradleException(
                    "ADMOB_INTERSTITIAL_ID_RELEASE not found in local.properties. " +
                            "Please add it to continue building the release version."
                )
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"$interstitialAdId\"")
        }

        // R8 の難読化・シュリンクを手元で検証するためのビルドタイプ。
        // リリースと同じ R8 設定のままデバッグ署名・テスト広告で動かす。
        create("beta") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "release"
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = true
            }
            useTestAdUnits()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    sourceSets {
        // debug と beta は同じデバッグ用の App Check provider を使う（#337）
        getByName("debug").java.srcDir("src/appCheckDebug/java")
        getByName("beta").java.srcDir("src/appCheckDebug/java")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.work.testing)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coil
    implementation(libs.coil3.compose)
    implementation(libs.coil3.okhttp)

    // Log
    implementation(libs.footprint)

    // Ads
    implementation(libs.play.service.ads)
    // Unity Ads（AdMob メディエーション、issue #213）
    implementation(libs.unity.ads.mediation)
    implementation(libs.unity.ads)
    // Liftoff Monetize（Vungle。AdMob メディエーション、issue #213）
    implementation(libs.liftoff.mediation)
    implementation(libs.liftoff.ads)

    // ML Kit Subject Segmentation
    implementation(libs.play.services.mlkit.subject.segmentation)
    // 背景透過の ML Kit モジュールのダウンロード（ModuleInstallClient）に使う
    implementation(libs.play.services.base)

    // アプリ内レビュー依頼（In-App Review API）
    implementation(libs.play.review.ktx)

    // ColorPicker
    implementation(libs.compose.colorpicker)

    // Browser (Chrome Custom Tabs)
    implementation(libs.androidx.browser)

    // Reorderable
    implementation(libs.reorderable)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)
    // 匿名ログインと App Check（#337）。AI 素材のサーバーを、本物のアプリの利用者からだけ呼べるようにする
    implementation(libs.firebase.auth)
    implementation(libs.firebase.appcheck)
    // App Check の provider はビルドタイプで分ける（di/AppCheckProviderModule.kt）。
    // ストアを通らない debug と beta は Play Integrity を通らないため、デバッグ用の provider を使う。
    // Play Integrity は beta の R8 確認に入らないので、更新したら :app:minifyReleaseWithR8 も通す（.agents/r8.md）
    releaseImplementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
    "betaImplementation"(libs.firebase.appcheck.debug)

}
