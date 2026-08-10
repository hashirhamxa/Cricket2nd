import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.perf)
}

android {
    namespace = "livecricket.livecrickettv.cricketstreaming"
    compileSdk = 36

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }
    val apiToken = localProperties.getProperty("API_TOKEN") ?: ""

    defaultConfig {
        applicationId = "livecricket.livecrickettv.cricketstreaming"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "1.6"

        buildConfigField("String", "API_TOKEN", "\"$apiToken\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.incremental"] = "true"
            }
        }
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // Core Android
    implementation(libs.activity)
    implementation(libs.fragment.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    implementation(libs.viewpager2)

    // Lifecycle + ViewModel (for MVVM)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Networking (Retrofit)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Dependency Injection (Hilt)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // JSON Parsing (Gson)
    implementation(libs.gson)

    // Local Database (Room)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.common)

    // Background Tasks (WorkManager)
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    kapt(libs.hilt.work.compiler)

    // Image Loading (Glide)
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Shimmer
    implementation(libs.shimmer)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //ads
    implementation(libs.play.services.ads)
    implementation(libs.unity.ads)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)


    implementation(files(*fileTree("libs").matching { include("*.jar") }.files.toTypedArray()))
    implementation(project(":newjustplayer"))


}
