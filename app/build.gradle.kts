import BuildTypes.DEBUG
import BuildTypes.RELEASE

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
        applicationId = "com.easyretro"
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
    (kotlinOptions as org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions).apply {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

androidExtensions {
    isExperimental = true
}

dependencies {
    implementation(Libraries.kotlin_std)
    implementation(Libraries.androidx_appcompat)
    implementation(Libraries.androidx_material)
    implementation(Libraries.androidx_constraint_layout)
    implementation(Libraries.androidx_lifecycle_extensions)
    implementation(Libraries.androidx_lifecycle_viewmodel_extensions)
    implementation(Libraries.androidx_lifecycle_livedata_extensions)
    implementation(Libraries.androidx_navigation_fragment_ktx)
    implementation(Libraries.androidx_navigation_ui_ktx)
    implementation(Libraries.firebase_firestore)
    implementation(Libraries.firebase_analytics)
    implementation(Libraries.firebase_dynamic_links)
    implementation(Libraries.firebase_auth)
    implementation(Libraries.coroutines_core)
    implementation(Libraries.coroutines_android)
    implementation(Libraries.koin_android)
    implementation(Libraries.koin_android_viewmodel)
    implementation(Libraries.timber)
    implementation(Libraries.androidx_room)
    implementation(Libraries.androidx_room_ktx)
    implementation(Libraries.arrow)
    implementation(Libraries.play_services_auth)
    implementation(Libraries.glide)
    debugImplementation(Libraries.room_debugger)
    kapt(Libraries.androidx_room_compiler)


    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.mockito_kotlin)
    androidTestImplementation(TestLibraries.androidx_test_espresso_core)
}
