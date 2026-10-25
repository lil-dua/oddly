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
    // with Compose. It is not declared explicitly: core-ktx 1.19 needs
    // AGP 9.1 / compileSdk 37, which the pinned toolchain does not have.
    // One splash on every API level (installSplashScreen). Version is checked
    // against the toolchain pin; it must not drag androidx.core up to 1.19.
    implementation(libs.androidx.core.splashscreen)
    // LifecycleEventEffect / LifecycleStartEffect let the app notice a new day on
    // resume. State stays a plain holder, not a ViewModel.
    implementation(libs.androidx.lifecycle.runtime.compose)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.test.runner)
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

/**
 * Version comes from the iOS xcconfig so both platforms share one number. `providers.fileContents`
 * makes the file a configuration-cache input, so editing it invalidates the cache.
 */
val versionFilePath = "iosApp/Configuration/Version.xcconfig"
val versionValues: Map<String, String> = providers
    .fileContents(rootProject.layout.projectDirectory.file(versionFilePath))
    .asText.get()
    .lineSequence()
    .map { it.substringBefore("//").trim() }
    .filter { it.isNotEmpty() && !it.startsWith("#") && '=' in it }
    .associate { it.substringBefore('=').trim() to it.substringAfter('=').trim() }

fun requireVersionKey(key: String): String =
    versionValues[key] ?: error("$versionFilePath: missing key $key")

val appVersionName: String = requireVersionKey("MARKETING_VERSION").also {
    require(Regex("""\d+(\.\d+){1,2}""").matches(it)) {
        "$versionFilePath: MARKETING_VERSION='$it' must look like 1.0 or 1.0.1"
    }
}
val appVersionCode: Int = requireVersionKey("CURRENT_PROJECT_VERSION").let {
    it.toIntOrNull()?.takeIf { n -> n in 1..2_100_000_000 }
        ?: error("$versionFilePath: CURRENT_PROJECT_VERSION='$it' must be an integer in 1..2100000000")
}

android {
    namespace = "dev.lildua.oddly"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.lildua.oddly"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        // Two errors predate CI (T-25); the baseline keeps them from turning the
        // first run red while still failing the build on any new error.
        baseline = file("lint-baseline.xml")
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
        // BuildConfig.VERSION_NAME feeds the Settings footer / About text.
        buildConfig = true
    }
}
