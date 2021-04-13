buildscript {

    repositories {
        google()
        maven { url = uri("https://maven.fabric.io/public") }
        jcenter()
    }

    dependencies {
        classpath(BuildPlugins.android_gradle_plugin)
        classpath(BuildPlugins.kotlin_gradle_plugin)
        classpath(BuildPlugins.google_services_plugin)
        classpath(BuildPlugins.dagger_hilt_plugin)
        classpath(BuildPlugins.fabric_gradle_plugin)
    }
}

plugins {
    id ("com.github.ben-manes.versions") version "0.28.0"
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
