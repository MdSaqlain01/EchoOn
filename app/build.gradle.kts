import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val env = Properties().apply {
    val file = rootProject.file(".env")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val supabaseUrl: String = env.getProperty("SUPABASE_URL", "")
val supabaseAnonKey: String = env.getProperty("SUPABASE_ANON_KEY", "")
val googleTranslateApiKey: String = env.getProperty("GOOGLE_TRANSLATE_API_KEY", "")

android {
    namespace = "com.echoon.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.echoon.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // Expose config to the app without hardcoding keys in source
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "GOOGLE_TRANSLATE_API_KEY", "\"$googleTranslateApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Networking + coroutines for calling translation API
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    // On-device text recognition for camera translation (bundled ML Kit model)
    implementation("com.google.mlkit:text-recognition:16.0.0")
}