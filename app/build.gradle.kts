plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt) // Sử dụng plugin Hilt cho Java, thay vì kotlin-kapt
}

android {
    namespace = "com.finmate"
    compileSdk = 34 // Sửa lại phiên bản SDK cho đúng

    defaultConfig {
        applicationId = "com.finmate"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
}

dependencies {
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler) // Dùng annotationProcessor cho Java

    // Hilt
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler) // Dùng annotationProcessor cho Java

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
