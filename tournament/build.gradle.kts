plugins {
    id("com.android.library")

    kotlin("android")
    id("kotlin-parcelize")
    kotlin("kapt")

    id("dagger.hilt.android.plugin")
}

android {
    namespace = "net.gearmaniacs.tournament"
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target

        consumerProguardFiles("tournament-rules.pro")
    }

    buildFeatures.compose = true

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

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.8.0")

    implementation(libs.voyager.tabNavigator)

    implementation(libs.dagger.android)
    kapt(libs.dagger.compiler)
    kapt(libs.dagger.hilt.compiler)
}
