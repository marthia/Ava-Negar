plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "me.marthia.avanegar"
    compileSdk = 35

    defaultConfig {
        applicationId = "me.marthia.avanegar"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.window.size.android)
    testImplementation(libs.junit)

    implementation(libs.kotlinx.serialization.json) // Check for latest version

    // vosk
    implementation(libs.vosk.android)

    implementation(libs.androidx.core.splashscreen)

    // navigation
    implementation(libs.navigation.core.raamcosta)
    ksp(libs.navigation.raamcosta.ksp)
    // V2 only: for bottom sheet destination support, also add
    implementation(libs.navigation.raamcosta.bottom.sheet)
    implementation(libs.hilt.navigation.compose)

// DaggerHilt
    implementation(libs.hilt)
    implementation(libs.hilt.commons)
    implementation(libs.hilt.work)
    ksp(libs.android.hilt.compiler)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.work.runtime)


    implementation(libs.kotlinx.coroutines.android)

    //    Network
    implementation(libs.retrofit)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)
    implementation(libs.moshi.kotlin)


    // Preferences DataStore (SharedPreferences like APIs)
    implementation(libs.datastore.preferences)

    implementation(libs.accompanist.permissions)


    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}