buildscript {

    repositories {
        google()
        maven {
            setUrl("https://maven.fabric.io/public")
        }
    }

    dependencies {
        classpath(BuildPlugins.android_gradle_plugin)
        classpath(BuildPlugins.kotlin_gradle_plugin)
        classpath(BuildPlugins.google_services_plugin)
        classpath(BuildPlugins.dagger_hilt_plugin)
        classpath(BuildPlugins.crashlytics_plugin)
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.38.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {

    gradleReleaseChannel = "current"
    // optional parameters
    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}