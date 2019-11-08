import Libraries.Versions.androidx_compat_version
import Libraries.Versions.androidx_constraint_layout_version
import Libraries.Versions.androidx_core_version
import Libraries.Versions.androidx_lifecycle_version
import Libraries.Versions.androidx_version
import Libraries.Versions.coroutines_version
import Libraries.Versions.firebase_analytics_version
import Libraries.Versions.firebase_firestore_version
import Libraries.Versions.google_material_version
import Libraries.Versions.koin_version
import Libraries.Versions.timber_version
import TestLibraries.Versions.espresso_version
import TestLibraries.Versions.junit_version

const val kotlin_version = "1.3.50"

object AndroidSdk {
    const val min = 21
    const val compile = 29
    const val target = compile
}

object Project{
    const val versionCode = 1
    const val versionName = "1.0.0"
}

object Plugins{
    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val googleServices = "com.google.gms.google-services"
}

object Libraries {
    object Versions {
        const val androidx_version = "2.0.0"
        const val androidx_compat_version = "1.0.2"
        const val androidx_core_version = "1.0.2"
        const val androidx_constraint_layout_version = "1.1.3"
        const val androidx_lifecycle_version = "2.0.0"
        const val google_material_version = "1.0.0"
        const val firebase_firestore_version = "21.1.1"
        const val firebase_analytics_version = "17.2.1"
        const val coroutines_version = "1.3.2"
        const val koin_version = "2.0.1"
        const val timber_version = "4.7.1"
    }

    const val kotlin_std = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"

    const val androidx_appcompat = "androidx.appcompat:appcompat:$androidx_compat_version"
    const val androidx_core_ktx = "androidx.core:core-ktx:$androidx_core_version"
    const val androidx_material = "com.google.android.material:material:$google_material_version"
    const val androidx_lifecycle_extensions = "androidx.lifecycle:lifecycle-extensions:$androidx_lifecycle_version"
    const val androidx_constraint_layout = "androidx.constraintlayout:constraintlayout:$androidx_constraint_layout_version"
    const val androidx_navigation_fragment = "androidx.navigation:navigation-fragment:$androidx_version"
    const val androidx_navigation_fragment_ktx = "androidx.navigation:navigation-fragment-ktx:$androidx_version"
    const val androidx_navigation_ui = "androidx.navigation:navigation-ui:$androidx_version"
    const val androidx_navigation_ui_ktx = "androidx.navigation:navigation-ui-ktx:$androidx_version"
    const val firebase_firestore = "com.google.firebase:firebase-firestore:$firebase_firestore_version"
    const val firebase_analytics = "com.google.firebase:firebase-analytics:$firebase_analytics_version"
    const val coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"
    const val coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"
    const val koin_android = "org.koin:koin-android:$koin_version"
    const val koin_android_viewmodel = "org.koin:koin-android-viewmodel:$koin_version"
    const val timber = "com.jakewharton.timber:timber:$timber_version"
}

object TestLibraries {
    private object Versions {
        const val junit_version = "4.12"
        const val espresso_version = "3.1.1"
    }

    const val junit = "junit:junit:$junit_version"
    const val androidx_test_espresso_core = "androidx.test.espresso:espresso-core:$espresso_version"
}