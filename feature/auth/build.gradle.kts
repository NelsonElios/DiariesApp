@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
  //  id("com.google.gms.google-services")
    id("io.realm.kotlin") version "1.9.0" // For MongoDb
}

android {
    namespace = "com.example.auth"
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

    /*implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)*/

    // --for compose UI
    implementation(libs.material3.compose)
    implementation(libs.activity.compose)
    implementation(libs.compose.tooling.preview)
    implementation(libs.navigation.compose)

    // --firebase auth)
  //  implementation(libs.gms.services.auth)
    implementation(libs.firebase.auth)



    // --MessageBar compose
    implementation(libs.message.bar.compose)
    // -- onTap Compose
    implementation(libs.one.tap.compose)

    // -- For realm mongoDb
    implementation(libs.coroutines.core)
    implementation(libs.realm.sync)

    // --internal dependencies
    implementation(project(":core:util"))
    implementation(project(":core:ui"))


}