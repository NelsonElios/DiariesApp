@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("io.realm.kotlin") version "1.9.0" // For MongoDb
    id("dagger.hilt.android.plugin")
    id("com.google.dagger.hilt.android")
   // id("com.google.gms.google-services")
    kotlin("kapt")
}

android {
    namespace = "com.example.mongo"
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
   // androidTestImplementation(libs.junit.ext)
  //  androidTestImplementation(libs.espresso.core)
    implementation(libs.firebase.crashlytics.buildtools)

    implementation(libs.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.realm.sync)

    // -- room database (need all this 4 dependencies otherwise i get the error( database_impl not exist )
    annotationProcessor(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)

    // coil
     implementation(libs.coil)


    // internal dependencies
    implementation(project(":core:util"))
}