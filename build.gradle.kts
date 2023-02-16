buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(libs.agp)
        classpath(libs.kotlinGradlePlugin)
        classpath(libs.dagger.hilt.plugin)
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.4")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.45.0"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

