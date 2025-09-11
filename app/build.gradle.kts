/*
 * Copyright © 2021-2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    kotlin("android")
    kotlin("plugin.serialization") version "2.2.20"
}

fun gitVersionCode(): Int {
    val result =
        providers.exec {
            commandLine = arrayListOf("git", "rev-list", "--count", "HEAD")
        }

    return result.standardOutput.asText
        .get()
        .trimEnd()
        .toInt()
}

fun gitVersionName(): String {
    val result =
        providers.exec {
            commandLine = arrayListOf("git", "describe", "--tags", "--always")
        }

    return result.standardOutput.asText
        .get()
        .trimEnd()
        .replace("-g", "-")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

android {
    namespace = "com.nerdoftheherd.tasker.rsync"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nerdoftheherd.tasker.rsync"
        minSdk = 21
        targetSdk = 35
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
        buildConfig = true
        viewBinding = true
    }

    kotlin {
        compilerOptions {
            allWarningsAsErrors = true
        }
    }

    lint {
        warningsAsErrors = true
        textReport = true

        // Causes unrelated PR failures after a new Gradle release
        disable += "AndroidGradlePluginVersion"

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

abstract class ReadmePermsCheckTask : DefaultTask() {
    @get:InputFiles
    var manifestDir: FileCollection = project.objects.fileCollection()

    @get:InputFile
    val readmeFile = project.objects.fileProperty()

    @get:Input
    val applicationId = project.objects.property<String>()

    @TaskAction
    fun execute() {
        val permNodes =
            XmlParser(false, false)
                .parse(
                    File(manifestDir.singleFile, "AndroidManifest.xml"),
                ).get("uses-permission") as NodeList

        val manifestPerms =
            permNodes
                .map { perm ->
                    (perm as Node)
                        .attribute("android:name")
                        .toString()
                        .removePrefix("android.permission.")
                }.filter { !it.startsWith(applicationId.get()) }

        val permLine = Regex("""^ - `([A-Z_]+)` \\$""")
        val readmePerms =
            readmeFile.get().asFile.readLines().mapNotNull { line ->
                permLine.matchEntire(line)?.groupValues?.get(1)
            }

        if (manifestPerms.sorted() != readmePerms.sorted()) {
            throw GradleException(
                "Manifest permissions mismatch with readme:\n" +
                    "  AndroidManifest.xml: $manifestPerms\n" +
                    "  README.md: $readmePerms",
            )
        }
    }
}

tasks.register<ReadmePermsCheckTask>("readmePermsCheck") {
    manifestDir = tasks.getByName("processDebugManifest").outputs.files
    readmeFile = file("../README.md")
    applicationId = android.defaultConfig.applicationId
}

tasks.check {
    dependsOn("readmePermsCheck")
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.3")
    implementation("com.google.android.material:material:1.13.0")
    implementation("com.joaomgcd:taskerpluginlibrary:0.4.10")
    implementation("com.nerdoftheherd:android-dropbear:2025.88")
    implementation("com.nerdoftheherd:android-rsync:3.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.7.0")
}
