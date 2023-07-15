plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.realm.kotlin") version "1.9.0" // For MongoDb
    id("dagger.hilt.android.plugin")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services") // need for Firebase
   // id("kotlin-kapt")
    kotlin("kapt")

}


android {
    namespace = "com.example.diariesapp"
    compileSdk = 33
    packaging { resources.excludes.add("META-INF/*") }


    defaultConfig {
        applicationId = "com.example.diariesapp"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(11)
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.0-beta01")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    //Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.6.0") // 2.5.3

    //Runtime Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")

    //Firebase
    implementation("com.google.firebase:firebase-auth-ktx:22.0.0")
    implementation("com.google.firebase:firebase-storage-ktx:20.2.0")

    //Room
    implementation("androidx.room:room-runtime:2.5.1")
    annotationProcessor("androidx.room:room-compiler:2.5.1")
    implementation("androidx.room:room-ktx:2.5.1")
    kapt("androidx.room:room-compiler:2.5.1")


    //Splash API
    implementation("androidx.core:core-splashscreen:1.0.1")

    //MongoDb Realm
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt") {
        version {
            strictly("1.6.0-native-mt")
        }
    }

    //implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.realm.kotlin:library-sync:1.7.0")

    //Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.44")
    //implementation("com.google.dagger:hilt-compiler:2.44")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    kapt("com.google.dagger:hilt-android-compiler:2.44")

    // Google Auth
    implementation("com.google.android.gms:play-services-auth:20.5.0")

    //Coil
    implementation("io.coil-kt:coil-compose:2.2.2")

    // Pager - Accompanist
    implementation("com.google.accompanist:accompanist-pager:0.27.0")

    //Date-Time Picker
    // implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0") // not supported on material 3
    implementation("com.maxkeppeler.sheets-compose-dialogs:core:1.0.2")

    //Calendar
    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.2")

    //Clock
    implementation("com.maxkeppeler.sheets-compose-dialogs:clock:1.0.2")

    // One-Tap Compose
    implementation("com.github.stevdza-san:OneTapCompose:1.0.0")

    // CALENDAR
    // implementation "com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.2"

    // CLOCK
    // implementation "com.maxkeppeler.sheets-compose-dialogs:clock:1.0.2"

    //Message bar Compose
    implementation("com.github.stevdza-san:MessageBarCompose:1.0.5")

    //Desugar SDK
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.2.0") // useful to use feature higher than the specified SDK number

}

kapt {
    correctErrorTypes = true
}