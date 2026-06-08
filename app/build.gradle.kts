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
        val gitFolder = rootProject.rootDir
        val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
            .directory(gitFolder)
            .redirectErrorStream(true)
            .start()

        process.waitFor()
        val result = process.inputStream.bufferedReader().readText().trim()

        println("GIT TAG RESULT: '$result'")

        if (result.contains("fatal") || result.contains("error") || result.isEmpty()) {
            println("GIT TAG FAILED: Retornando 1.0.0 como fallback.")
            "1.0.0"
        } else {
            val finalVersion = result.replace("v", "")
            println("GIT TAG SUCESSO: Retornando $finalVersion")
            finalVersion
        }
    } catch (e: Exception) {
        println("GIT COMMAND CRASH: ${e.message}")
        "1.0.0"
    }
}

fun getAppVersionCode(): Int {
    return try {
        val gitFolder = rootProject.rootDir
        val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .directory(gitFolder)
            .redirectErrorStream(true)
            .start()

        process.waitFor()
        val result = process.inputStream.bufferedReader().readText().trim()

        if (result.contains("fatal") || result.isEmpty()) 1 else result.toInt()
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug{
            isMinifyEnabled = false
            isShrinkResources = false
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