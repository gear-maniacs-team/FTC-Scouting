plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("kotlin-parcelize")
}

android {
    namespace = "net.gearmaniacs.database"
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        minSdk = Versions.Sdk.min
    }

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }
    kotlin {
        sourceSets {
            debug {
                kotlin.srcDir("build/generated/source/proto/debug/java")
            }
            release {
                kotlin.srcDir("build/generated/source/proto/release/java")
            }
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", true.toString())
    arg("room.expandProjection", true.toString())
}

dependencies {
    implementation(project(Modules.core))

    implementation("com.jakewharton:process-phoenix:2.1.2")

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.dagger.android)
    ksp(libs.dagger.compiler)
    ksp(libs.dagger.hilt.compiler)
}