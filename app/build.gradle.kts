plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.safehaven"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.safehaven"
        minSdk = 24
        targetSdk = 35
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Existing Jetpack Compose and Material3 dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Google Play Services for Maps and Location
    implementation("com.google.android.gms:play-services-maps:17.0.1")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Firebase (For future real-time database, notifications, or authentication)
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-database")

    // Sensor library for Shake Detection (For "Shake to SOS" feature)
    implementation("androidx.core:core-ktx:1.12.0")

    // RecyclerView (If needed for displaying lists like self-defense tips, legal info)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Retrofit (For fetching safer places dynamically via an API)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.android.volley:volley:1.2.1")

    // Material Components (for Material 3 design)
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ShakeDetector library (unchanged, as it's a third-party library)
    implementation("com.github.tbouron.shakedetector:library:1.0.0@aar")

    // Google Maps and Location services
    implementation("com.google.android.gms:play-services-maps:18.2.0") // Updated to latest stable
    implementation("com.google.android.gms:play-services-location:21.3.0") // Already up-to-date

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.tbouron.shakedetector:library:1.0.0@aar")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // Remove Places SDK dependency (no longer needed)
    // implementation("com.google.android.libraries.places:places:3.3.0")
    implementation("com.google.maps.android:android-maps-utils:3.0.0")
    implementation("com.google.maps:google-maps-services:0.18.2") // Still needed for Routes API
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("com.google.android.libraries.places:places:4.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("com.google.maps.android:android-maps-utils:3.0.0")
    androidTestImplementation("com.google.android.libraries.places:places:3.3.0")

    // OpenStreetMap JSON parsing
    implementation("org.json:json:20210307")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
