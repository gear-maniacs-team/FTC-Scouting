plugins {
    id("com.android.library")

    kotlin("android")
    kotlin("kapt")

    id("dagger.hilt.android.plugin")
}

android {
    namespace = "net.gearmaniacs.login"
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target

        consumerProguardFiles("login-rules.pro")
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }
}

dependencies {
    implementation(project(Modules.core))
    implementation(project(Modules.database))

    implementation(libs.voyager.tabNavigator)

    implementation(Libs.gms_auth)

    implementation(libs.dagger.android)
    kapt(libs.dagger.compiler)
    kapt(libs.dagger.hilt.compiler)
}
