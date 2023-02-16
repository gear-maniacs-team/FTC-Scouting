object Versions {
    object App {
        private const val major = 2
        private const val minor = 0
        private const val patch = 2

        const val versionCode: Int = major * 100 + minor * 10 + patch
        const val versionName: String = "$major.$minor.$patch"
    }

    object Sdk {
        const val min = 21
        const val compile = 33
        const val target = 30
    }

    const val lifecycle = "2.3.0-beta01"
    const val firebaseBom = "31.2.2"
}
