/*
 * Copyright © 2021-2024 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    kotlin("android")
    kotlin("plugin.serialization") version("1.9.0")
}

fun gitVersionCode(): Int {
    val out = ByteArrayOutputStream()

    exec {
        commandLine = arrayListOf("git", "rev-list", "--count", "HEAD")
        standardOutput = out
    }

    return out.toString().trimEnd().toInt()
}

fun gitVersionName(): String {
    val out = ByteArrayOutputStream()

    exec {
        commandLine = arrayListOf("git", "describe", "--tags", "--always")
        standardOutput = out
    }

    return out.toString().trimEnd().replace("-g", "-")
}

android {
    namespace = "com.nerdoftheherd.tasker.rsync"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nerdoftheherd.tasker.rsync"
        minSdk = 19
        targetSdk = 34
        versionCode = gitVersionCode()
        versionName = gitVersionName()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isCrunchPngs = false // Legacy launcher icons are pre-crunched
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        debug {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        allWarningsAsErrors = true
    }

    lint {
        warningsAsErrors = true
        textReport = true

        // Dependabot notifies us about new versions and failing the
        // build causes problems updating single dependencies via PRs
        disable += "GradleDependency"

        // GitHub Actions installs pre-release SDKs which triggers
        // this before the final SDK, AGP & Android Studio release
        disable += "OldTargetApi"
    }

    testOptions {
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }

    packaging {
        jniLibs.excludes.add("lib/*/libdropbear.so")
        jniLibs.excludes.add("lib/*/libdropbearconvert.so")
        jniLibs.excludes.add("lib/*/libscp.so")
    }
}

dependencies {
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.joaomgcd:taskerpluginlibrary:0.4.10")
    implementation("com.nerdoftheherd:android-dropbear:2022.83")
    implementation("com.nerdoftheherd:android-rsync:3.2.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")

    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
}
