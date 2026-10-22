import java.util.Properties
import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
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
        versionName = "1.6.1"

        val apiKey = localProperties.getProperty("API_KEY")
            ?: localProperties.getProperty("API_ACCESS_KEY")
            ?: ""
        buildConfigField("String", "API_KEY", "\"$apiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://esports-pro-api.onrender.com/\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug{
            val debugApiBaseUrl = localProperties.getProperty("DEBUG_API_BASE_URL")
                ?: "http://127.0.0.1:8000/"
            val debugApiUri = try {
                URI(debugApiBaseUrl)
            } catch (_: java.net.URISyntaxException) {
                throw GradleException("DEBUG_API_BASE_URL inválida em local.properties.")
            }
            require(
                debugApiUri.scheme in listOf("http", "https") &&
                    !debugApiUri.host.isNullOrBlank() && debugApiUri.rawUserInfo == null &&
                    debugApiUri.rawQuery == null && debugApiUri.rawFragment == null &&
                    debugApiUri.port in -1..65535 && debugApiBaseUrl.endsWith("/")
            ) { "DEBUG_API_BASE_URL deve ser uma URL HTTP(S) absoluta, sem credenciais, query ou fragmento e terminar em /." }
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
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



    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.lifecycle.runtime.compose)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.appcompat)
    implementation(libs.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.compose.material.icons)
}
