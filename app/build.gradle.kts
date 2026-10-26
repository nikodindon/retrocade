import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun localProperty(key: String, fallback: String = ""): String =
    localProperties.getProperty(key)?.trim().takeUnless { it.isNullOrEmpty() } ?: fallback

val testAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val testInterstitialId = "ca-app-pub-3940256099942544/1033173712"
val releaseAdmobAppId = localProperty("ADMOB_APP_ID")
val releaseInterstitialId = localProperty("ADMOB_INTERSTITIAL_GAME_START")
val monthlyId = localProperty("BILLING_SUB_MONTHLY_ID", "retrocade_premium_monthly")
val lifetimeId = localProperty("BILLING_LIFETIME_ID", "retrocade_premium_lifetime")

android {
    namespace = "com.nikodindon.retrocade"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nikodindon.retrocade"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "BILLING_MONTHLY_ID", "\"$monthlyId\"")
        buildConfigField("String", "BILLING_LIFETIME_ID", "\"$lifetimeId\"")
        manifestPlaceholders["admobAppId"] = testAdmobAppId
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "ADMOB_APP_ID", "\"$testAdmobAppId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$testInterstitialId\"")
            buildConfigField("boolean", "USE_TEST_ADS", "true")
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "ADMOB_APP_ID", "\"$releaseAdmobAppId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$releaseInterstitialId\"")
            buildConfigField("boolean", "USE_TEST_ADS", "false")
            manifestPlaceholders["admobAppId"] = releaseAdmobAppId
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

kotlin {
    jvmToolchain(17)
}

/**
 * Release guard: Google's sample ad identifiers must never reach a production build.
 * The task is wired into the release packaging tasks so debug builds and lint do not
 * require production credentials, while any shipped APK/AAB is still blocked.
 */
val verifyReleaseAdIds by tasks.registering {
    // Capture the configuration values eagerly: reading top-level script properties from
    // doLast would break Gradle's configuration cache.
    val appId = releaseAdmobAppId
    val interstitialId = releaseInterstitialId
    doLast {
        val ids = mapOf(
            "ADMOB_APP_ID" to appId,
            "ADMOB_INTERSTITIAL_GAME_START" to interstitialId,
        )
        ids.forEach { (key, value) ->
            if (value.isBlank() || value.contains("XXXX")) {
                throw GradleException(
                    "Release build blocked: $key is missing from local.properties. " +
                        "See local.properties.example.",
                )
            }
            if (value.contains("3940256099942544")) {
                throw GradleException(
                    "Release build blocked: $key uses a Google test ad identifier.",
                )
            }
        }
    }
}

tasks.matching { it.name == "packageRelease" || it.name == "packageReleaseBundle" }.configureEach {
    dependsOn(verifyReleaseAdIds)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:engine"))
    implementation(project(":core:audio"))
    implementation(project(":core:ui"))
    implementation(project(":core:ads"))
    implementation(project(":core:billing"))
    implementation(project(":core:storage"))
    implementation(project(":games:api"))
    implementation(project(":games:tetris"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
