@Suppress("MayBeConstant") // Improve performance on change
object Libs {
    // AndroidX
    val androidx_core = "androidx.core:core-ktx:1.9.0"
    val androidx_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"

    // Firebase
    val firebase_bom = "com.google.firebase:firebase-bom:${Versions.firebaseBom}"
    val firebase_analytics = "com.google.firebase:firebase-analytics-ktx"
    val firebase_database = "com.google.firebase:firebase-database-ktx"
    val firebase_auth = "com.google.firebase:firebase-auth-ktx"
    val firebase_crashlytics = "com.google.firebase:firebase-crashlytics"

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
