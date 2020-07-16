@Suppress("unused", "MayBeConstant") // Improve performance on change
object Libs {
    // Kotlin
    val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.7"

    // AndroidX
    val google_material = "com.google.android.material:material:1.1.0"
    val androidx_appcompat = "androidx.appcompat:appcompat:1.1.0"
    val androidx_recycler_view = "androidx.recyclerview:recyclerview:1.1.0"
    val androidx_constraint_layout = "androidx.constraintlayout:constraintlayout:2.0.0-beta6"
    val androidx_lifecycle = "androidx.lifecycle:lifecycle-extensions:2.2.0"
    val androidx_swipe_refresh_layout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    val androidx_preference = "androidx.preference:preference:1.1.1"

    // AndroidX KTX
    val ktx_core = "androidx.core:core-ktx:1.3.0"
    val ktx_fragment = "androidx.fragment:fragment-ktx:1.2.5"
    val ktx_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0"

    // Firebase
    val firebase_core = "com.google.firebase:firebase-core:17.4.4"
    val firebase_database = "com.google.firebase:firebase-database-ktx:19.3.1"
    val firebase_auth = "com.google.firebase:firebase-auth:19.3.2"
    val firebase_crashlytics = "com.crashlytics.sdk.android:crashlytics:2.10.1"
    val firebase_ktx_common = "com.google.firebase:firebase-common-ktx:19.3.0"

    // Hilt
    private val dagger_hilt_android = "com.google.dagger:hilt-android:${Versions.dagger_hilt}"
    private val hilt_lifecycle = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hilt}"
    val hilt = arrayOf(dagger_hilt_android, hilt_lifecycle)

    // Hilt Compiler
    private val dagger_hilt_compiler = "com.google.dagger:hilt-android-compiler:${Versions.dagger_hilt}"
    private val hilt_android_compiler = "androidx.hilt:hilt-compiler:${Versions.hilt}"
    val hilt_compiler = arrayOf(dagger_hilt_compiler, hilt_android_compiler)

    // Other Libraries
    val material_intro = "com.heinrichreimersoftware:material-intro:2.0.0"
    val material_about = "com.github.daniel-stoneuk:material-about-library:3.1.2"
    val licenser = "com.github.marcoscgdev:Licenser:2.0.0"
    val apache_poi = "org.apache.poi:poi:4.0.1"

    // Tests
    private val tests_junit = "junit:junit:4.13"
    private val tests_androidx = "androidx.test:core:1.2.0"
    val tests = arrayOf(tests_junit, tests_androidx)
}
