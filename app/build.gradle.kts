import org.jetbrains.kotlin.gradle.dsl.JvmTarget

private val TEST_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
private val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
private val TEST_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
private val TEST_REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
private val TEST_REWARDED_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/7781731905"
private val TEST_APP_OPEN_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

private val RELEASE_AD_ID_PROPERTIES = listOf(
    "admob.appId",
    "admob.bannerUnitId",
    "admob.interstitialUnitId",
    "admob.rewardedUnitId",
    "admob.rewardedInterstitialUnitId",
    "admob.appOpenUnitId",
)

private fun Project.adId(property: String, testId: String): String =
    providers.gradleProperty(property).getOrElse(testId)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
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

        // Sample ad units by default; release overrides with properties from ~/.gradle/gradle.properties.
        manifestPlaceholders["admobAppId"] = TEST_ADMOB_APP_ID
        buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$TEST_BANNER_UNIT_ID\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"$TEST_INTERSTITIAL_UNIT_ID\"")
        buildConfigField("String", "ADMOB_REWARDED_UNIT_ID", "\"$TEST_REWARDED_UNIT_ID\"")
        buildConfigField(
            "String",
            "ADMOB_REWARDED_INTERSTITIAL_UNIT_ID",
            "\"$TEST_REWARDED_INTERSTITIAL_UNIT_ID\"",
        )
        buildConfigField("String", "ADMOB_APP_OPEN_UNIT_ID", "\"$TEST_APP_OPEN_UNIT_ID\"")
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
            // Real IDs only in release builds; debug keeps Google's sample units.
            manifestPlaceholders["admobAppId"] = adId("admob.appId", TEST_ADMOB_APP_ID)
            buildConfigField(
                "String",
                "ADMOB_BANNER_UNIT_ID",
                "\"${adId("admob.bannerUnitId", TEST_BANNER_UNIT_ID)}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_INTERSTITIAL_UNIT_ID",
                "\"${adId("admob.interstitialUnitId", TEST_INTERSTITIAL_UNIT_ID)}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_REWARDED_UNIT_ID",
                "\"${adId("admob.rewardedUnitId", TEST_REWARDED_UNIT_ID)}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_REWARDED_INTERSTITIAL_UNIT_ID",
                "\"${adId("admob.rewardedInterstitialUnitId", TEST_REWARDED_INTERSTITIAL_UNIT_ID)}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_APP_OPEN_UNIT_ID",
                "\"${adId("admob.appOpenUnitId", TEST_APP_OPEN_UNIT_ID)}\"",
            )
        }
    }

    val checkReleaseAdIds by tasks.registering {
        group = "verification"
        description = "Fails a release build when real AdMob IDs are missing."
        doLast {
            val missing = RELEASE_AD_ID_PROPERTIES.filter {
                !providers.gradleProperty(it).isPresent
            }
            check(missing.isEmpty()) {
                "Set real AdMob IDs in ~/.gradle/gradle.properties: ${missing.joinToString()}"
            }
        }
    }
    tasks.matching { it.name == "preReleaseBuild" }.configureEach {
        dependsOn(checkReleaseAdIds)
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

    bundle {
        language {
            // The in-app language switch must work offline, so keep both locales in the base module.
            enableSplit = false
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
    implementation(libs.user.messaging.platform)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
