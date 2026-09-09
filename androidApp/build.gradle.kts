import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    // Shared domain layer. The UI deliberately is not shared — iOS gets SwiftUI.
    implementation(project(":sharedLogic"))

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.toolingPreview)
    implementation(libs.androidx.activity.compose)
    // androidx.core (FileProvider, used by the share export) arrives transitively
    // with Compose. It is not declared explicitly because the catalogue's
    // core-ktx 1.19 needs AGP 9.1 / compileSdk 37 — see the toolchain note.
    // No lifecycle-compose yet: state lives in a plain holder, not a ViewModel.
    // Add lifecycle-viewmodel-compose when the repository layer lands.

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
}

/**
 * Upload-key credentials, kept out of the repository.
 *
 * Create `keystore.properties` beside the root `settings.gradle.kts` with
 * `storeFile`, `storePassword`, `keyAlias` and `keyPassword`. When the file is
 * absent — a fresh clone, or CI without the secret — the release build still
 * assembles, just unsigned, so the toolchain stays verifiable without the key.
 */
val keystoreProperties = rootProject.file("keystore.properties").takeIf { it.exists() }?.let {
    Properties().apply { it.inputStream().use(::load) }
}

android {
    namespace = "dev.lildua.oddly"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.lildua.oddly"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        if (keystoreProperties != null) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            // R8 on: a habit app has no reflection-driven code to trip over, and
            // the shrink is worth it on a download users decide about in a second.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}
