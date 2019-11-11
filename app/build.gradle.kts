import BuildTypes.DEBUG
import BuildTypes.RELEASE
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    id(Plugins.androidApplication)
    id(Plugins.kotlinAndroid)
    id(Plugins.kotlinAndroidExtensions)
    id(Plugins.kotlinKapt)
    id(Plugins.googleServices)
}

android {
    compileSdkVersion(AndroidSdk.compile)
    defaultConfig {
        applicationId = "com.ibracero.retrum"
        minSdkVersion(AndroidSdk.min)
        targetSdkVersion(AndroidSdk.target)
        versionCode = Project.versionCode
        versionName = Project.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName(DEBUG) {
            isMinifyEnabled = false
            isDebuggable = true
        }

        getByName(RELEASE) {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(Libraries.kotlin_std)
    implementation(Libraries.androidx_appcompat)
    implementation(Libraries.androidx_core_ktx)
    implementation(Libraries.androidx_material)
    implementation(Libraries.androidx_constraint_layout)
    implementation(Libraries.androidx_lifecycle_extensions)
    implementation(Libraries.androidx_navigation_fragment)
    implementation(Libraries.androidx_navigation_fragment_ktx)
    implementation(Libraries.androidx_navigation_ui)
    implementation(Libraries.androidx_navigation_ui_ktx)
    implementation(Libraries.firebase_firestore)
    implementation(Libraries.firebase_analytics)
    implementation(Libraries.coroutines_core)
    implementation(Libraries.coroutines_android)
    implementation(Libraries.koin_android)
    implementation(Libraries.koin_android_viewmodel)
    implementation(Libraries.timber)
    implementation(Libraries.androidx_room)
    implementation(Libraries.androidx_room)
    kapt(Libraries.androidx_room_compiler)


    testImplementation(TestLibraries.junit)
    androidTestImplementation(TestLibraries.androidx_test_espresso_core)
}
