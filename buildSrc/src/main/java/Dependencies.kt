import BuildPlugins.Versions.android_gradle_plugin_version
import BuildPlugins.Versions.crashlytics_plugin_version
import BuildPlugins.Versions.google_services_version
import Libraries.Versions.androidx_compat_version
import Libraries.Versions.androidx_constraint_layout_version
import Libraries.Versions.androidx_lifecycle_version
import Libraries.Versions.androidx_navigation_version
import Libraries.Versions.arrow_version
import Libraries.Versions.coroutines_version
import Libraries.Versions.firebase_crashlytics_version
import Libraries.Versions.dagger_hilt_viewmodel_version
import Libraries.Versions.firebase_analytics_version
import Libraries.Versions.firebase_auth_version
import Libraries.Versions.firebase_dynamic_links_version
import Libraries.Versions.firebase_firestore_version
import Libraries.Versions.glide_version
import Libraries.Versions.google_material_version
import Libraries.Versions.play_services_auth_version
import Libraries.Versions.room_version
import Libraries.Versions.timber_version
import TestLibraries.Versions.androidx_testing_version
import TestLibraries.Versions.espresso_version
import TestLibraries.Versions.junit_version
import TestLibraries.Versions.mockito_inline_version
import TestLibraries.Versions.mockito_version

const val kotlin_version = "1.5.31"
const val dagger_hilt_version = "2.38.1"


object AndroidSdk {
    const val minVersion = 21
    const val compileVersion = 30
    const val targetVersion = compileVersion
}

object Project {
    const val versionCode = 1
    const val versionName = "1.0.0"
}

object BuildPlugins {
    object Versions {
        const val android_gradle_plugin_version = "7.0.2"
        const val google_services_version = "4.3.10"
        const val crashlytics_plugin_version = "2.7.1"
    }

    const val android_gradle_plugin = "com.android.tools.build:gradle:$android_gradle_plugin_version"
    const val kotlin_gradle_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    const val google_services_plugin = "com.google.gms:google-services:$google_services_version"
    const val crashlytics_plugin = "com.google.firebase:firebase-crashlytics-gradle:$crashlytics_plugin_version"
    const val dagger_hilt_plugin = "com.google.dagger:hilt-android-gradle-plugin:$dagger_hilt_version"
}

object Plugins {
    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val googleServices = "com.google.gms.google-services"
    const val crashlytics = "com.google.firebase.crashlytics"
    const val kotlinKapt = "kotlin-kapt"
    const val hilt = "dagger.hilt.android.plugin"
}

object Libraries {
    object Versions {
        const val androidx_navigation_version = "2.3.5"
        const val androidx_compat_version = "1.3.1"
        const val androidx_constraint_layout_version = "2.1.0"
        const val androidx_lifecycle_version = "2.3.1"
        const val google_material_version = "1.4.0"
        const val firebase_firestore_version = "23.0.3"
        const val firebase_analytics_version = "19.0.1"
        const val firebase_dynamic_links_version = "20.1.1"
        const val firebase_auth_version = "21.0.1"
        const val firebase_crashlytics_version = "18.0.3"
        const val coroutines_version = "1.5.2"
        const val timber_version = "5.0.1"
        const val room_version = "2.3.0"
        const val room_debugger_version = "1.0.6"
        const val arrow_version = "0.10.3"
        const val play_services_auth_version = "19.0.0"
        const val glide_version = "4.12.0"
        const val dagger_hilt_viewmodel_version = "1.0.0-alpha03"
    }

    const val kotlin_std = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"

    const val androidx_appcompat = "androidx.appcompat:appcompat:$androidx_compat_version"
    const val androidx_material = "com.google.android.material:material:$google_material_version"
    const val androidx_lifecycle_viewmodel_extensions =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:$androidx_lifecycle_version"
    const val androidx_lifecycle_livedata_extensions =
        "androidx.lifecycle:lifecycle-livedata-ktx:$androidx_lifecycle_version"
    const val androidx_constraint_layout =
        "androidx.constraintlayout:constraintlayout:$androidx_constraint_layout_version"
    const val androidx_navigation_fragment_ktx =
        "androidx.navigation:navigation-fragment-ktx:$androidx_navigation_version"
    const val androidx_navigation_ui_ktx = "androidx.navigation:navigation-ui-ktx:$androidx_navigation_version"

    const val firebase_firestore = "com.google.firebase:firebase-firestore:$firebase_firestore_version"
    const val firebase_analytics = "com.google.firebase:firebase-analytics:$firebase_analytics_version"
    const val firebase_dynamic_links = "com.google.firebase:firebase-dynamic-links:$firebase_dynamic_links_version"
    const val firebase_auth = "com.google.firebase:firebase-auth:$firebase_auth_version"
    const val firebase_crashlytics = "com.google.firebase:firebase-analytics:$firebase_crashlytics_version"

    const val coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"
    const val coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"
    const val timber = "com.jakewharton.timber:timber:$timber_version"
    const val androidx_room = "androidx.room:room-runtime:$room_version"
    const val androidx_room_compiler = "androidx.room:room-compiler:$room_version"
    const val androidx_room_ktx = "androidx.room:room-ktx:$room_version"
    const val arrow = "io.arrow-kt:arrow-core:$arrow_version"
    const val play_services_auth = "com.google.android.gms:play-services-auth:$play_services_auth_version"
    const val glide = "com.github.bumptech.glide:glide:$glide_version"
    const val dagger_hilt_android = "com.google.dagger:hilt-android:$dagger_hilt_version"
    const val dagger_hilt_compiler = "com.google.dagger:hilt-compiler:$dagger_hilt_version"
    const val dagger_hilt_viewmodel = "androidx.hilt:hilt-lifecycle-viewmodel:$dagger_hilt_viewmodel_version"
}

object TestLibraries {
    private object Versions {
        const val junit_version = "4.13.2"
        const val espresso_version = "3.4.0"
        const val mockito_version = "2.1.0"
        const val mockito_inline_version = "3.12.4"
        const val androidx_testing_version = "2.1.0"
    }

    const val junit = "junit:junit:$junit_version"
    const val mockito_kotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:$mockito_version"
    const val mockito_inline = "org.mockito:mockito-inline:$mockito_inline_version"
    const val coroutines_test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version"
    const val androidx_core_testing = "androidx.arch.core:core-testing:$androidx_testing_version"
    const val androidx_test_espresso_core = "androidx.test.espresso:espresso-core:$espresso_version"
}