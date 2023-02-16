plugins {
    id("com.android.library")

    kotlin("android")
    id("kotlin-parcelize")
    kotlin("kapt")

    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target

        consumerProguardFiles("core-rules.pro")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.incremental" to "true",
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.expandProjection" to "true"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
//    api(Libs.kotlin_stdlib)
    api(libs.kotlin.coroutines.android)
    api(libs.kotlin.coroutines.playServices)

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
    api(Libs.androidx_nav_fragment)
    api(Libs.androidx_nav_ui)
    api(libs.androidX.dataStore)

    api(platform(Libs.firebase_bom))
    api(Libs.firebase_analytics)
    api(Libs.firebase_auth)
    api(Libs.firebase_database)

    api(libs.room.runtime)
    api(libs.room.paging)
    kapt(libs.room.compiler)

    implementation(libs.dagger.android)
    kapt(libs.dagger.compiler)
    kapt(libs.dagger.hilt.compiler)
}
