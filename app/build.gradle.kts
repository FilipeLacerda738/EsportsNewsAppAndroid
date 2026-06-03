import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}


fun getAppVersionName(): String {
    return try {
        val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
            .directory(rootProject.rootDir)
            .start()

        process.waitFor()

        val version = process.inputStream.bufferedReader().readText().trim()
        if (version.isNotEmpty()) version.replace("v", "") else "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
    }
}

fun getAppVersionCode(): Int {
    return try {
        val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .directory(rootProject.rootDir)
            .start()

        process.waitFor()

        val count = process.inputStream.bufferedReader().readText().trim()
        if (count.isNotEmpty()) count.toInt() else 1
    } catch (e: Exception) {
        1
    }
}


android {
    namespace = "com.example.esportsnews"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.esportsnews"
        minSdk = 26
        targetSdk = 35

        versionCode = getAppVersionCode()
        versionName = getAppVersionName()

        val apiKey = localProperties.getProperty("API_KEY") ?: ""
        buildConfigField("String", "API_KEY", "\"$apiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)

    implementation("androidx.compose.material3:material3:1.3.0")

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("androidx.compose.material:material-icons-extended")
}