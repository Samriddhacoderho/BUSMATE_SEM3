plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.busmate"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.busmate"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging) //added by keshab for material icons
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.airbnb.android:lottie-compose:6.6.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.google.maps.android:maps-compose:4.3.0")

    // Google Play Services Location (For GPS)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Core Maps SDK (Needed by Compose library)
    implementation("com.google.android.gms:play-services-maps:19.1.0")

    implementation("com.google.code.gson:gson:2.10.1")
//    implementation("com.baato:baato-android:1.1.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.6")
    implementation("com.google.zxing:core:3.5.1")
//    implementation("com.cloudinary:cloudinary-android:2.1.0")
//    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.cloudinary:cloudinary-android:2.1.0")

    // Coil for Loading Cloudinary URLs into Compose
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-rc01")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-rc01")


    // CameraX
    implementation("androidx.camera:camera-camera2:1.5.2")
    implementation("androidx.camera:camera-lifecycle:1.5.2")
    implementation("androidx.camera:camera-view:1.5.2")

    // REQUIRED for CameraX's ListenableFuture
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.guava:guava:32.1.2-android")

// ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

}

