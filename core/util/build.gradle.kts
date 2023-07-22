@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.example.util"
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
   // implementation(libs.core.ktx)
   // implementation(libs.appcompat)
  //  implementation(libs.material)
  //  testImplementation(libs.junit)
  //  androidTestImplementation(libs.junit.ext)
  //  androidTestImplementation(libs.espresso.core)

    implementation(libs.firebase.storage)
    implementation(libs.realm.sync)
    implementation(libs.coroutines.core)
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.material3.compose)

    // material preview
    implementation(libs.compose.tooling.preview)

    implementation(project(":core:ui"))
    //implementation(project(":data:mongo"))
}