plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.googleServices) apply false
    id("com.github.ben-manes.versions") version "0.50.0"
}

tasks.named(
    "dependencyUpdates",
    com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class.java
).configure {
    rejectVersionIf {
        (candidate.version.contains("alpha") && !currentVersion.contains("alpha")) ||
                (candidate.version.contains("beta") && !currentVersion.contains("beta"))
    }
}


tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

