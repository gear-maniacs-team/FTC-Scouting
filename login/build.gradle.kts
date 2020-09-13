plugins {
    id("com.android.library")

    kotlin("android")
    kotlin("kapt")

    id("dagger.hilt.android.plugin")
}

android {
    compileSdkVersion(Versions.Sdk.compile)

    defaultConfig {
        minSdkVersion(Versions.Sdk.min)
        targetSdkVersion(Versions.Sdk.target)

        consumerProguardFiles("login-rules.pro")
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(Modules.core))
    implementation(project(Modules.database))

    implementation(Libs.gms_auth)

    implementation(Libs.hilt_dagger_android)
    implementation(Libs.hilt_lifecycle)
    kapt(Libs.hilt_dagger_compiler)
    kapt(Libs.hilt_android_compiler)
}
