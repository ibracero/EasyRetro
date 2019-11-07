import BuildTypes.DEBUG
import BuildTypes.RELEASE

plugins{
    id(Plugins.androidApplication)
    id(Plugins.kotlinAndroid)
    id(Plugins.kotlinAndroidExtensions)
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

    implementation("com.google.firebase:firebase-analytics:17.2.0")

    testImplementation(TestLibraries.junit)
    androidTestImplementation(TestLibraries.androidx_test_espresso_core)
}
