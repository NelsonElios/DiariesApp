@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("io.realm.kotlin") version "1.9.0" // For MongoDb
    kotlin("kapt")
}

android {
    namespace = "com.example.edit"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        minSdk = ProjectConfig.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    kotlin {
        jvmToolchain(11)
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = ProjectConfig.kotlinCompilerExtensionVersion
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.material3.compose)
    implementation(libs.activity.compose)
    implementation(libs.compose.tooling.preview)
    implementation(libs.navigation.compose)


    // -- For realm mongoDb
    implementation(libs.coroutines.core)
    implementation(libs.realm.sync)

    // -- Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)

    // --internal dependencies
    implementation(project(":core:util"))
    implementation(project(":core:ui"))
    implementation(project(mapOf("path" to ":data:mongo")))

    // --Date-Time Picker
    implementation(libs.date.time.picker)

    // --Calendar
    implementation(libs.date.dialog)

    // --firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)

    // --coil
    implementation(libs.coil)

    // --Pager - Accompanist
    implementation(libs.accompanist.pager)

    // --Clock
    implementation(libs.time.dialog)
}