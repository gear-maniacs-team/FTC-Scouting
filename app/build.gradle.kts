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
    compileSdkVersion(Versions.Sdk.compile)

    defaultConfig {
        applicationId("net.gearmaniacs.ftcscouting")
        minSdkVersion(Versions.Sdk.min)
        targetSdkVersion(Versions.Sdk.target)
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

        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
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
            addManifestPlaceholders(mapOf("disableFirebase" to true, "enableCrashlytics" to false))

            extra.set("enableCrashlytics", false)
            extra.set("alwaysUpdateBuildId", false)
            isCrunchPngs = false
        }
        getByName("release") {
            addManifestPlaceholders(mapOf("disableFirebase" to false, "enableCrashlytics" to true))

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

    kapt(Libs.room_compiler)

    implementation(Libs.hilt_dagger_android)
    implementation(Libs.hilt_lifecycle)
    kapt(Libs.hilt_dagger_compiler)
    kapt(Libs.hilt_android_compiler)

    testImplementation(Libs.Test.junit)
    testImplementation(Libs.Test.core)
    testImplementation(Libs.hilt_dagger_android)
    kaptTest(Libs.hilt_dagger_compiler)
    kaptTest(Libs.hilt_android_compiler)

    androidTestImplementation(Libs.Test.runner)
    androidTestImplementation(Libs.Test.rules)
    androidTestImplementation(Libs.Test.espresso_core)
    androidTestImplementation(Libs.Test.espresso_contrib)
    androidTestImplementation(Libs.Test.uiautomator)
}

kapt {
    correctErrorTypes = true
}
