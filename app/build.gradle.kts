plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinAndroid)

    alias(libs.plugins.ksp)

    id("com.google.gms.google-services")
    alias(libs.plugins.hilt)
}

android {
    namespace = "net.gearmaniacs.ftcscouting"
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        applicationId = "net.gearmaniacs.ftcscouting"
        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target
        versionCode = Versions.App.versionCode
        versionName = Versions.App.versionName
        resourceConfigurations += listOf("en", "ro")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    buildTypes {
        getByName("debug") {
            addManifestPlaceholders(mapOf("disableFirebase" to true))
        }
        getByName("release") {
            addManifestPlaceholders(mapOf("disableFirebase" to false))

            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", true.toString())
    arg("room.expandProjection", true.toString())
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf("-Xjvm-default=all")
    }
}

dependencies {
    debugImplementation(libs.kotlin.reflect)
    implementation(project(Modules.core))
    implementation(project(Modules.login))
    implementation(project(Modules.tournament))
    implementation(project(Modules.database))

    implementation(libs.compose.compiler)

    ksp(libs.room.compiler)

    implementation(libs.dagger.android)
    ksp(libs.dagger.compiler)
    ksp(libs.dagger.hilt.compiler)

    testImplementation(Libs.Test.junit)
    testImplementation(Libs.Test.core)
    testImplementation(libs.dagger.android)
    kspTest(libs.dagger.compiler)
    kspTest(libs.dagger.hilt.compiler)
}
