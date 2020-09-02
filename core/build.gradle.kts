plugins {
    id("com.android.library")

    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")

    id("dagger.hilt.android.plugin")
}

android {
    compileSdkVersion(Versions.Sdk.compile)

    defaultConfig {
        minSdkVersion(Versions.Sdk.min)
        targetSdkVersion(Versions.Sdk.target)

        consumerProguardFiles("core-rules.pro")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.incremental" to "true"
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
        freeCompilerArgs = listOf(
                "-progressive",
                "-Xopt-in=kotlin.RequiresOptIn"
        )
    }

    androidExtensions {
        features = setOf("parcelize")
    }
}

dependencies {
    api(Libs.kotlin_stdlib)
    api(Libs.kotlin_coroutines)
    api(Libs.kotlin_coroutines_play_services)

    api(Libs.google_material)
    api(Libs.androidx_core)
    api(Libs.androidx_appcompat)
    api(Libs.androidx_recycler_view)
    api(Libs.androidx_constraint_layout)
    api(Libs.androidx_swipe_refresh_layout)
    api(Libs.androidx_preference)
    api(Libs.androidx_fragment)
    api(Libs.androidx_lifecycle)
    api(Libs.androidx_livedata)
    api(Libs.androidx_viewmodel)

    api(Libs.firebase_analytics)
    api(Libs.firebase_auth)
    api(Libs.firebase_database)

    api(Libs.flow_preferences)

    api(Libs.room_runtime)
    api(Libs.room_ktx)
    kapt(Libs.room_compiler)

    implementation(Libs.hilt_dagger_android)
    implementation(Libs.hilt_lifecycle)
    kapt(Libs.hilt_dagger_compiler)
    kapt(Libs.hilt_android_compiler)
}
