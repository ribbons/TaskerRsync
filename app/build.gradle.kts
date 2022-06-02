/*
 * Copyright © 2021-2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    kotlin("android")
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
    compileSdk = 31

    defaultConfig {
        applicationId = "com.nerdoftheherd.tasker.rsync"
        minSdk = 19
        targetSdk = 31
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
                "proguard-rules.pro"
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
    }

    lint {
        warningsAsErrors = true
        textReport = true

        // https://github.com/joaomgcd/TaskerPluginSample/issues/7
        disable += "NonConstantResourceId"

        // Dependabot notifies us about new versions and failing the
        // build causes problems updating single dependencies via PRs
        disable += "GradleDependency"
    }

    testOptions {
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }

    packagingOptions {
        jniLibs.excludes.add("lib/*/libdropbear.so")
        jniLibs.excludes.add("lib/*/libdropbearconvert.so")
        jniLibs.excludes.add("lib/*/libscp.so")
    }
}

dependencies {
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("com.joaomgcd:taskerpluginlibrary:0.4.2")
    implementation("com.nerdoftheherd:android-dropbear:2022.82")
    implementation("com.nerdoftheherd:android-rsync:3.2.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")

    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
