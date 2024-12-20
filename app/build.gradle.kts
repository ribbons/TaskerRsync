/*
 * Copyright © 2021-2024 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
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

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

android {
    namespace = "com.nerdoftheherd.tasker.rsync"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nerdoftheherd.tasker.rsync"
        minSdk = 21
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
        buildConfig = true
        viewBinding = true
    }

    kotlinOptions {
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
            XmlParser(false, false).parse(
                File(manifestDir.singleFile, "AndroidManifest.xml"),
            ).get("uses-permission") as NodeList

        val manifestPerms =
            permNodes.map { perm ->
                (perm as Node).attribute("android:name").toString()
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
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.joaomgcd:taskerpluginlibrary:0.4.10")
    implementation("com.nerdoftheherd:android-dropbear:2024.86")
    implementation("com.nerdoftheherd:android-rsync:3.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")

    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
}
