plugins {
    id("com.android.application")

    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdkVersion(Versions.compileSdk)

    defaultConfig {
        applicationId("net.gearmaniacs.ftcscouting")
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode(Versions.App.versionCode)
        versionName(Versions.App.versionName)
        resConfigs("en")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders(mapOf("firebaseDisabled" to true, "crashlyticsEnabled" to false))

            extra.set("enableCrashlytics", false)
            extra.set("alwaysUpdateBuildId", false)
            isCrunchPngs = false
        }
        getByName("release") {
            setManifestPlaceholders(mapOf("firebaseDisabled" to false, "crashlyticsEnabled" to true))

            isMinifyEnabled = true
            isShrinkResources = true
            isZipAlignEnabled = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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

    implementation(Libs.hilt_dagger_android)
    implementation(Libs.hilt_lifecycle)
    kapt(Libs.hilt_dagger_compiler)
    kapt(Libs.hilt_android_compiler)

    testImplementation(Libs.tests_androidx)
    testImplementation(Libs.tests_junit)
}
