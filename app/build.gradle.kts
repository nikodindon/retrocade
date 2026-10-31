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

fun secret(name: String, fallback: String = ""): String =
    System.getenv(name)?.trim()?.takeUnless { it.isEmpty() }
        ?: localProperty(name, fallback)

val testAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val testInterstitialId = "ca-app-pub-3940256099942544/1033173712"
val releaseAdmobAppId = secret("ADMOB_APP_ID")
val releaseInterstitialId = secret("ADMOB_INTERSTITIAL_GAME_START")
val lifetimeId = secret("BILLING_LIFETIME_ID", "retrocade_premium_lifetime")

val releaseStoreFile = secret("RELEASE_STORE_FILE")
val releaseStorePassword = secret("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = secret("RELEASE_KEY_ALIAS")
val releaseKeyPassword = secret("RELEASE_KEY_PASSWORD")
val releaseSigningConfigured = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { it.isNotBlank() }

val expectedApplicationId = "com.nikodindon.retrocade"
val expectedVersionName = "1.0.0"
val appVersionCode = 3

android {
    namespace = expectedApplicationId
    compileSdk = 36

    defaultConfig {
        applicationId = expectedApplicationId
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = expectedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "BILLING_LIFETIME_ID", "\"$lifetimeId\"")
        manifestPlaceholders["admobAppId"] = testAdmobAppId
        manifestPlaceholders["billingVersion"] = libs.versions.billing.get()
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
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
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
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
 * Release guard: production credentials, identity, version and monetization scope
 * are checked before any signed APK/AAB is produced. Debug builds and lint never
 * need production credentials.
 */
val verifyReleaseAdIds by tasks.registering {
    // Capture values eagerly: reading top-level script properties from doLast would
    // break Gradle's configuration cache.
    val appId = releaseAdmobAppId
    val interstitialId = releaseInterstitialId
    val signingConfigured = releaseSigningConfigured
    val storePath = releaseStoreFile
    val applicationId = expectedApplicationId
    val versionName = expectedVersionName
    val versionCode = appVersionCode
    val root = rootProject.layout.projectDirectory.asFile
    val scannedRoots = listOf("app", "core", "games").map { File(root, it) }
    // Split the literals so this guard does not match its own build script.
    val forbiddenMonthly = Regex(
        listOf(
            "BILLING" + "_SUB",
            "premium" + "_monthly",
            "BillingProductType" + ".MONTHLY",
            "ProductType" + ".SUBS",
            "abonnement" + " mensuel",
            "premium" + " mensuel",
        ).joinToString("|"),
        RegexOption.IGNORE_CASE,
    )

    doLast {
        val ids = mapOf(
            "ADMOB_APP_ID" to appId,
            "ADMOB_INTERSTITIAL_GAME_START" to interstitialId,
        )
        ids.forEach { (key, value) ->
            if (value.isBlank() || value.contains("XXXX")) {
                throw GradleException(
                    "Release build blocked: $key is missing. Set it in local.properties " +
                        "or as an environment/CI secret. See local.properties.example.",
                )
            }
            if (value.contains("3940256099942544")) {
                throw GradleException(
                    "Release build blocked: $key uses a Google test ad identifier.",
                )
            }
        }

        if (!signingConfigured) {
            throw GradleException(
                "Release build blocked: release signing is not configured. Set " +
                    "RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS and " +
                    "RELEASE_KEY_PASSWORD in local.properties or the environment.",
            )
        }
        val storeFile = File(storePath).let { if (it.isAbsolute) it else File(root, storePath) }
        if (!storeFile.isFile) {
            throw GradleException(
                "Release build blocked: RELEASE_STORE_FILE does not exist (${storeFile.path}).",
            )
        }
        if (applicationId != "com.nikodindon.retrocade") {
            throw GradleException("Release build blocked: unexpected applicationId $applicationId.")
        }
        if (versionName != "1.0.0") {
            throw GradleException("Release build blocked: versionName must be 1.0.0, was $versionName.")
        }
        if (versionCode <= 0) {
            throw GradleException("Release build blocked: versionCode must be positive.")
        }

        scannedRoots.forEach { scannedRoot ->
            if (!scannedRoot.exists()) return@forEach
            scannedRoot.walkTopDown()
                .filter { it.isFile && it.extension in setOf("kt", "kts", "xml", "pro", "properties") }
                .filterNot { it.path.contains("${File.separator}build${File.separator}") }
                .forEach { file ->
                    val match = forbiddenMonthly.find(file.readText())
                    if (match != null) {
                        throw GradleException(
                            "Release build blocked: monthly subscription reference " +
                                "'${match.value}' found in ${file.relativeTo(root)}.",
                        )
                    }
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
