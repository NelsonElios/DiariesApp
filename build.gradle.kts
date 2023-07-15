buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15")
       //classpath("com.google.dagger:hilt-android-gradle-plugin:2.44.2")
       // classpath ("io.realm:realm-gradle-plugin:6.0.1") // May be not useful
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0-alpha09" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("io.realm.kotlin") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
}



