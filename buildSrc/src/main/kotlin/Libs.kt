@Suppress("MayBeConstant") // Improve performance on change
object Libs {
    // Kotlin
    val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val kotlin_coroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}"
    val kotlin_coroutines_play_services =
        "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.kotlinCoroutines}"

    // AndroidX
    val google_material = "com.google.android.material:material:1.3.0-alpha03"
    val androidx_core = "androidx.core:core-ktx:1.3.2"
    val androidx_appcompat = "androidx.appcompat:appcompat:1.3.0-alpha02"
    val androidx_recycler_view = "androidx.recyclerview:recyclerview:1.2.0-alpha06"
    val androidx_constraint_layout = "androidx.constraintlayout:constraintlayout:2.0.2"
    val androidx_swipe_refresh_layout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    val androidx_preference = "androidx.preference:preference:1.1.1"
    val androidx_fragment = "androidx.fragment:fragment-ktx:1.3.0-beta01"
    val androidx_lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    val androidx_livedata = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    val androidx_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"

    val datastore_preferences = "androidx.datastore:datastore-preferences:1.0.0-alpha01"

    // Room
    val room_runtime = "androidx.room:room-runtime:${Versions.room}"
    val room_ktx = "androidx.room:room-ktx:${Versions.room}"
    val room_compiler = "androidx.room:room-compiler:${Versions.room}"

    // Firebase
    val firebase_analytics = "com.google.firebase:firebase-analytics-ktx:17.6.0"
    val firebase_database = "com.google.firebase:firebase-database-ktx:19.5.0"
    val firebase_auth = "com.google.firebase:firebase-auth-ktx:19.4.0"
    val firebase_crashlytics = "com.google.firebase:firebase-crashlytics:17.2.2"

    // GMS
    val gms_auth = "com.google.android.gms:play-services-auth:18.1.0"

    // Hilt
    val hilt_dagger_android = "com.google.dagger:hilt-android:${Versions.daggerHilt}"
    val hilt_lifecycle = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hilt}"

    // Hilt Compiler
    val hilt_dagger_compiler = "com.google.dagger:hilt-android-compiler:${Versions.daggerHilt}"
    val hilt_android_compiler = "androidx.hilt:hilt-compiler:${Versions.hilt}"

    // Other Libraries
    val material_intro = "com.heinrichreimersoftware:material-intro:2.0.0"
    val material_about = "com.github.daniel-stoneuk:material-about-library:3.2.0-rc01"
    val licenser = "com.github.marcoscgdev:Licenser:2.0.0"
    val apache_poi = "org.apache.poi:poi:4.0.1"

    // Tests
    object Test {
        val junit = "junit:junit:4.13"
        val core = "androidx.test:core:1.3.0"
        val runner = "androidx.test.ext:junit:1.1.2"
        val rules = "androidx.test:rules:1.3.0"
        val espresso_core = "androidx.test.espresso:espresso-core:3.3.0"
        val espresso_contrib = "androidx.test.espresso:espresso-contrib:3.3.0"
        val uiautomator = "androidx.test.uiautomator:uiautomator:2.2.0"
    }
}
