/*
 * Copyright © 2021-2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.storage.StorageManager
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

class TestUtils {
    companion object {
        fun primaryStorageDir(context: Context): File {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val manager =
                    context.getSystemService(
                        Context.STORAGE_SERVICE,
                    ) as StorageManager

                val volume = manager.primaryStorageVolume
                return volume.directory!!
            }

            return Environment.getExternalStorageDirectory()
        }

        fun setExternalStoragePermission(
            context: Context,
            enabled: Boolean,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val appOps =
                    context.getSystemService(
                        Context.APP_OPS_SERVICE,
                    ) as AppOpsManager

                val permAllowed = fun(): Boolean =
                    appOps.checkOpNoThrow(
                        "android:manage_external_storage",
                        Process.myUid(),
                        context.packageName,
                    ) == AppOpsManager.MODE_ALLOWED

                if (permAllowed() == enabled) {
                    return
                }

                InstrumentationRegistry
                    .getInstrumentation()
                    .uiAutomation
                    .executeShellCommand(
                        "appops set ${context.packageName} " +
                            "MANAGE_EXTERNAL_STORAGE " +
                            if (enabled) "allow" else "deny",
                    )

                while (permAllowed() != enabled) {
                    Thread.sleep(1)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (enabled) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                        InstrumentationRegistry
                            .getInstrumentation()
                            .uiAutomation
                            .grantRuntimePermission(
                                context.packageName,
                                android.Manifest.permission
                                    .READ_EXTERNAL_STORAGE,
                            )
                    }

                    InstrumentationRegistry
                        .getInstrumentation()
                        .uiAutomation
                        .grantRuntimePermission(
                            context.packageName,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        )
                } else {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                        InstrumentationRegistry
                            .getInstrumentation()
                            .uiAutomation
                            .revokeRuntimePermission(
                                context.packageName,
                                android.Manifest.permission
                                    .READ_EXTERNAL_STORAGE,
                            )
                    }

                    InstrumentationRegistry
                        .getInstrumentation()
                        .uiAutomation
                        .revokeRuntimePermission(
                            context.packageName,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uiAutomation =
                    InstrumentationRegistry
                        .getInstrumentation()
                        .uiAutomation

                val updatePerm =
                    uiAutomation::class.java.getMethod(
                        if (enabled) {
                            "grantRuntimePermission"
                        } else {
                            "revokeRuntimePermission"
                        },
                        String::class.java,
                        String::class.java,
                        Process.myUserHandle()::class.java,
                    )

                updatePerm.invoke(
                    uiAutomation,
                    context.packageName,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    Process.myUserHandle(),
                )

                updatePerm.invoke(
                    uiAutomation,
                    context.packageName,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Process.myUserHandle(),
                )
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                val uiAutomation =
                    InstrumentationRegistry
                        .getInstrumentation()
                        .uiAutomation

                val uiacField =
                    uiAutomation::class.java.getDeclaredField(
                        "mUiAutomationConnection",
                    )

                uiacField.isAccessible = true

                val handle = Process.myUserHandle()
                val getIdentifier =
                    handle::class.java.getMethod("getIdentifier")

                val updatePerm =
                    Class
                        .forName(
                            "android.app.IUiAutomationConnection",
                        ).getMethod(
                            if (enabled) {
                                "grantRuntimePermission"
                            } else {
                                "revokeRuntimePermission"
                            },
                            String::class.java,
                            String::class.java,
                            Integer.TYPE,
                        )

                updatePerm.invoke(
                    uiacField.get(uiAutomation),
                    context.packageName,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    getIdentifier.invoke(handle),
                )

                updatePerm.invoke(
                    uiacField.get(uiAutomation),
                    context.packageName,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    getIdentifier.invoke(handle),
                )
            }
        }
    }
}
