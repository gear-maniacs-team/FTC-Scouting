plugins {
    id("com.android.library")

    kotlin("android")
    id("kotlin-parcelize")
    kotlin("kapt")

    id("dagger.hilt.android.plugin")
}

android {
    namespace = "net.gearmaniacs.core"
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target

        consumerProguardFiles("core-rules.pro")
    }

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    buildFeatures.compose = true

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    api(libs.kotlin.coroutines.android)
    api(libs.kotlin.coroutines.playServices)

    api(libs.androidX.dataStore)

    api(platform(Libs.firebase_bom))
    api(Libs.firebase_analytics)
    api(Libs.firebase_auth)
    api(Libs.firebase_database)

    api(libs.compose.compiler)
    api(libs.compose.foundation)
    api(libs.compose.ui)
    api(libs.compose.material3)
    api(libs.compose.animation)
    api(libs.compose.toolingPreview)
    debugApi(libs.compose.tooling)
    api(libs.compose.activity)
    api(libs.compose.viewmodel)

    api(libs.voyager.navigator)
    api(libs.voyager.transitions)
    api(libs.voyager.androidx)

    implementation(libs.room.runtime)
    kapt(libs.room.compiler)

    implementation(libs.dagger.android)
    kapt(libs.dagger.compiler)
    kapt(libs.dagger.hilt.compiler)
}
