import BuildTypes.DEBUG
import BuildTypes.RELEASE
import Name.ENABLE_ANALYTICS
import Type.TYPE_BOOLEAN
import Value.FALSE
import Value.TRUE
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
    id(Plugins.androidApplication)
    id(Plugins.kotlinAndroid)
    id(Plugins.kotlinAndroidExtensions)
    id(Plugins.kotlinKapt)
    id(Plugins.googleServices)
    id(Plugins.crashlytics)
    id(Plugins.hilt)
}

val keystorePropertiesFile = file(".signing/keystore.properties")
val keystoreProperties = Properties()
val isLocalBuild = keystorePropertiesFile.exists()
if (isLocalBuild) keystoreProperties.load(FileInputStream(keystorePropertiesFile))
val signingConfigName = "releaseConfig"

android {
    compileSdk = AndroidSdk.compileVersion

    defaultConfig {
        applicationId = "com.easyretro"
        minSdk = AndroidSdk.minVersion
        targetSdk = AndroidSdk.targetVersion
        versionCode = Project.versionCode
        versionName = Project.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    signingConfigs {
        register(signingConfigName).configure {
            if (isLocalBuild) {
                keyAlias = keystoreProperties["LOCAL_KEY_ALIAS"] as String
                keyPassword = keystoreProperties["LOCAL_KEY_PASSWORD"] as String
                storeFile = file(keystoreProperties["LOCAL_KEYSTORE_FILE"] as String)
                storePassword = keystoreProperties["LOCAL_STORE_PASSWORD"] as String
            } else {
                keyAlias = System.getenv("CI_KEY_ALIAS") as String
                keyPassword = System.getenv("CI_KEY_PASSWORD") as String
                storeFile = file("../${System.getenv("CI_STORE_FILE") as String}")
                storePassword = System.getenv("CI_STORE_PASSWORD") as String
            }
        }
    }

    buildTypes {
        getByName(DEBUG) {
            buildConfigField(TYPE_BOOLEAN, ENABLE_ANALYTICS, FALSE)
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isDebuggable = true
        }

        getByName(RELEASE) {
            buildConfigField(TYPE_BOOLEAN, ENABLE_ANALYTICS, TRUE)
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName(signingConfigName)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

androidExtensions {
    isExperimental = true
}

dependencies {
    implementation(Libraries.kotlin_std)
    implementation(Libraries.androidx_appcompat)
    implementation(Libraries.androidx_material)
    implementation(Libraries.androidx_constraint_layout)
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
    implementation(Libraries.timber)
    implementation(Libraries.androidx_room)
    implementation(Libraries.androidx_room_ktx)
    implementation(Libraries.arrow)
    implementation(Libraries.play_services_auth)
    implementation(Libraries.glide)
    implementation(Libraries.firebase_crashlytics)

    implementation(Libraries.dagger_hilt_android)
    implementation(Libraries.dagger_hilt_viewmodel)

    kapt(Libraries.androidx_room_compiler)

    kapt(Libraries.dagger_hilt_compiler)

    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.mockito_kotlin)
    testImplementation(TestLibraries.mockito_inline)
    testImplementation(TestLibraries.coroutines_test)
    testImplementation(TestLibraries.androidx_core_testing)

    androidTestImplementation(TestLibraries.androidx_test_espresso_core)
}

kapt {
    correctErrorTypes = true
}