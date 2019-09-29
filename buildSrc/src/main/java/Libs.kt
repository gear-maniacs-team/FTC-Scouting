@Suppress("unused", "MayBeConstant") // Improve performance on change
object Libs {
    // Kotlin
    val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin_version}"
    val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.2"

    // AndroidX
    val google_material = "com.google.android.material:material:1.1.0-alpha10"
    val androidx_appcompat = "androidx.appcompat:appcompat:1.1.0"
    val androidx_recycler_view = "androidx.recyclerview:recyclerview:1.0.0"
    val androidx_card_view = "androidx.cardview:cardview:1.0.0"
    val androidx_constraint_layout = "androidx.constraintlayout:constraintlayout:2.0.0-beta2"
    val androidx_lifecycle = "androidx.lifecycle:lifecycle-extensions:2.1.0"
    val androidx_swipe_refresh_layout = "androidx.swiperefreshlayout:swiperefreshlayout:1.0.0"

    // KTX
    val ktx_core = "androidx.core:core-ktx:1.1.0"
    val ktx_fragment = "androidx.fragment:fragment-ktx:1.1.0"
    val ktx_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.1.0"

    // Firebase
    val firebase_core = "com.google.firebase:firebase-core:17.2.0"
    val firebase_database = "com.google.firebase:firebase-database:19.1.0"
    val firebase_auth = "com.google.firebase:firebase-auth:19.1.0"
    val firebase_crashlytics = "com.crashlytics.sdk.android:crashlytics:2.10.1"

    // Other Libraries
    val material_intro = "com.heinrichreimersoftware:material-intro:2.0.0"
    val material_about = "com.github.daniel-stoneuk:material-about-library:2.4.2"
    val material_chooser = "net.theluckycoder.materialchooser:materialchooser:1.2.1"
    val licenser = "com.github.marcoscgdev:Licenser:1.0.5"
    val apache_poi = "org.apache.poi:poi:3.17"

    // Tests
    private const val tests_junit = "junit:junit:4.12"
    private const val tests_androidx = "androidx.test:core:1.0.0"
    val tests = arrayOf(tests_junit, tests_androidx)
}
