/*
 * Copyright © 2021-2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20")
    }
}

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/ribbons/android-rsync")
            credentials {
                username = project.findProperty("gpr.user") as String?
                    ?: System.getenv("PKG_USERNAME")
                password = project.findProperty("gpr.key") as String?
                    ?: System.getenv("PKG_TOKEN")
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
