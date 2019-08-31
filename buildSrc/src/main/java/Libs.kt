@Suppress("unused")
object Libs {
    // Kotlin
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin_version}"
    const val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.0"

    // KTX
    const val ktx_core = "androidx.core:core-ktx:1.2.0-alpha02"
    const val ktx_fragment = "androidx.fragment:fragment-ktx:1.1.0-rc04"
    const val ktx_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.1.0-rc01"

    // AndroidX
    const val google_material = "com.google.android.material:material:1.1.0-alpha09"
    const val androidx_appcompat = "androidx.appcompat:appcompat:1.1.0-rc01"
    const val androidx_recycler_view = "androidx.recyclerview:recyclerview:1.0.0"
    const val androidx_card_view = "androidx.cardview:cardview:1.0.0"
    const val androidx_constraint_layout = "androidx.constraintlayout:constraintlayout:2.0.0-beta2"
    const val androidx_lifecycle = "androidx.lifecycle:lifecycle-extensions:2.1.0-rc01"
    const val androidx_swipe_refresh_layout = "androidx.swiperefreshlayout:swiperefreshlayout:1.0.0"

    // Firebase
    const val firebase_core = "com.google.firebase:firebase-core:17.2.0"
    const val firebase_database = "com.google.firebase:firebase-database:19.0.0"
    const val firebase_auth = "com.google.firebase:firebase-auth:19.0.0"
    const val firebase_crashlytics = "com.crashlytics.sdk.android:crashlytics:2.10.1"

    // Other Libraries
    const val material_about = "com.github.daniel-stoneuk:material-about-library:2.4.2"
    const val material_chooser = "net.theluckycoder.materialchooser:materialchooser:1.2.1"
    const val apache_poi = "org.apache.poi:poi:3.17"

    // Tests
    private const val tests_junit = "junit:junit:4.12"
    private const val tests_androidx = "androidx.test:core:1.0.0"
    val tests = arrayOf(tests_junit, tests_androidx)
}
