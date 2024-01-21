plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("kotlin-parcelize")
}

android {
    namespace = "net.gearmaniacs.core"
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        minSdk = Versions.Sdk.min

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

    api(platform(libs.firebase.bom))
    api(libs.firebase.analytics)
    api(libs.firebase.auth) {
        exclude(module = "play-services-safetynet")
    }
    api(libs.firebase.database)

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

    implementation(libs.dagger.android)
    ksp(libs.dagger.compiler)
    ksp(libs.dagger.hilt.compiler)
}
