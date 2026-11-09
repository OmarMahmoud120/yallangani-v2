import org.jetbrains.kotlin.gradle.dsl.JvmTarget

private val TEST_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
private val TEST_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
private val TEST_REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.osmartapps.yallanghanee"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.osmartapps.yallanghanee"
        minSdk = 24
        targetSdk = 36
        versionCode = 13
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Google sample ad units by default; override in ~/.gradle/gradle.properties for release.
        manifestPlaceholders["admobAppId"] =
            providers.gradleProperty("admob.appId").getOrElse(TEST_ADMOB_APP_ID)
        buildConfigField(
            "String",
            "ADMOB_INTERSTITIAL_UNIT_ID",
            "\"${providers.gradleProperty("admob.interstitialUnitId").getOrElse(TEST_INTERSTITIAL_UNIT_ID)}\"",
        )
        buildConfigField(
            "String",
            "ADMOB_REWARDED_UNIT_ID",
            "\"${providers.gradleProperty("admob.rewardedUnitId").getOrElse(TEST_REWARDED_UNIT_ID)}\"",
        )
    }

    buildTypes {
        debug {
            // Installs alongside the legacy Play build without replacing it.
            applicationIdSuffix = ".dev"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.play.services.ads)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
