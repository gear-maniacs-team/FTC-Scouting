object Versions {
    object App {
        private const val major = 2
        private const val minor = 0
        private const val patch = 1

        const val versionCode: Int = major * 100 + minor * 10 + patch
        const val versionName: String = "$major.$minor.$patch"
    }

    object Sdk {
        const val min = 21
        const val compile = 30
        const val target = 30
    }

    const val kotlin = "1.4.10"
    const val kotlinCoroutines = "1.3.9"
    const val room = "2.2.5"
    const val daggerHilt = "2.29.1-alpha"
    const val hilt = "1.0.0-alpha02"
    const val lifecycle = "2.3.0-beta01"
}
