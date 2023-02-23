plugins {
    id("com.android.application")

    kotlin("android")
    kotlin("kapt")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("dagger.hilt.android.plugin")
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

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.expandProjection" to "true"
                )
            }
        }

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
            addManifestPlaceholders(mapOf("disableFirebase" to true, "enableCrashlytics" to false))

            extra.set("enableCrashlytics", false)
            extra.set("alwaysUpdateBuildId", false)
            isCrunchPngs = false
        }
        getByName("release") {
            addManifestPlaceholders(mapOf("disableFirebase" to false, "enableCrashlytics" to true))

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf("-Xjvm-default=all")
    }
}

dependencies {
    implementation(project(Modules.core))
    implementation(project(Modules.login))
    implementation(project(Modules.tournament))
    implementation(project(Modules.database))

    implementation(Libs.firebase_crashlytics)

    implementation(Libs.material_intro)
    implementation(Libs.material_about)
    implementation(Libs.licenser)

    kapt(libs.room.compiler)

    implementation(libs.dagger.android)
    kapt(libs.dagger.compiler)
    kapt(libs.dagger.hilt.compiler)

    testImplementation(Libs.Test.junit)
    testImplementation(Libs.Test.core)
    testImplementation(libs.dagger.android)
    kaptTest(libs.dagger.compiler)
    kaptTest(libs.dagger.hilt.compiler)
}

kapt {
    correctErrorTypes = true
}
