import org.gradle.api.JavaVersion

object Versions {
    object App {
        private const val major = 3
        private const val minor = 0
        private const val patch = 0

        const val versionCode: Int = major * 100 + minor * 10 + patch
        const val versionName: String = "$major.$minor.$patch"
    }

    object Sdk {
        const val min = 26
        const val compile = 33
        const val target = 33
    }

    const val lifecycle = "2.3.0-beta01"
    const val firebaseBom = "31.2.2"
    val java = JavaVersion.VERSION_17
}
