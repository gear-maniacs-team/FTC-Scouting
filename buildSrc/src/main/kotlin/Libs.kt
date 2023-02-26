@Suppress("MayBeConstant") // Improve performance on change
object Libs {
    // AndroidX
    val google_material = "com.google.android.material:material:1.3.0-alpha04"
    val androidx_core = "androidx.core:core-ktx:1.9.0"
    val androidx_appcompat = "androidx.appcompat:appcompat:1.3.0-alpha02"
    val androidx_recycler_view = "androidx.recyclerview:recyclerview:1.2.0"
    val androidx_constraint_layout = "androidx.constraintlayout:constraintlayout:2.0.4"
    val androidx_swipe_refresh_layout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    val androidx_preference = "androidx.preference:preference:1.1.1"
    val androidx_fragment = "androidx.fragment:fragment-ktx:1.3.0-beta02"
    val androidx_lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    val androidx_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"

    // Firebase
    val firebase_bom = "com.google.firebase:firebase-bom:${Versions.firebaseBom}"
    val firebase_analytics = "com.google.firebase:firebase-analytics-ktx"
    val firebase_database = "com.google.firebase:firebase-database-ktx"
    val firebase_auth = "com.google.firebase:firebase-auth-ktx"
    val firebase_crashlytics = "com.google.firebase:firebase-crashlytics"

    // GMS
    val gms_auth = "com.google.android.gms:play-services-auth:20.4.1"

    // Other Libraries
    val licenser = "com.github.marcoscgdev:Licenser:2.0.0"

    // Tests
    object Test {
        val junit = "junit:junit:4.13.1"
        val core = "androidx.test:core:1.3.0"
        val runner = "androidx.test.ext:junit:1.1.2"
        val rules = "androidx.test:rules:1.3.0"
        val espresso_core = "androidx.test.espresso:espresso-core:3.3.0"
        val espresso_contrib = "androidx.test.espresso:espresso-contrib:3.3.0"
        val uiautomator = "androidx.test.uiautomator:uiautomator:2.2.0"
    }
}
