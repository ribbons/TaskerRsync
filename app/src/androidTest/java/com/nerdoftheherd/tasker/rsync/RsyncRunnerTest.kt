/*
 * Copyright © 2021-2024 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultErrorWithOutput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.config.RsyncConfig
import com.nerdoftheherd.tasker.rsync.output.CommandOutput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class RsyncRunnerTest {
    @Test
    fun noPrivateKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = RsyncConfig("-h", "", false)

        val keyFile = File(context.filesDir, "id_dropbear")
        keyFile.delete()

        val output = RsyncRunner().run(context, TaskerInput(config))
        assertFalse(output.success)

        val error = output as TaskerPluginResultErrorWithOutput<CommandOutput>
        assertEquals(context.getString(R.string.no_private_key), error.message)
    }

    @Test
    fun errorFromFailure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = RsyncConfig("--invalid", "", false)

        File(context.filesDir, "id_dropbear").createNewFile()

        val output = RsyncRunner().run(context, TaskerInput(config))
        assertFalse(output.success)

        val error = output as TaskerPluginResultErrorWithOutput<CommandOutput>
        assertTrue(error.message.startsWith("rsync: --invalid: unknown option"))
    }

    @Test
    fun successFromNormalExit() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = RsyncConfig("-V", "", false)

        File(context.filesDir, "id_dropbear").createNewFile()

        val output = RsyncRunner().run(context, TaskerInput(config))
        assertTrue(output.success)
    }

    @Test
    fun captureStdout() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = RsyncConfig("-h", "", false)

        File(context.filesDir, "id_dropbear").createNewFile()

        val output = RsyncRunner().run(context, TaskerInput(config))
        val outputSuccess = output as TaskerPluginResultSucess<CommandOutput>
        val stdout = outputSuccess.regular?.stdout!!

        assertTrue(stdout.startsWith("rsync  version "))
        assertTrue(stdout.contains("for full documentation.\n"))
    }

    @Test
    fun copyFromAndToPrimaryStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            TestUtils.setManageStoragePermission(context, true)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }

        File(context.filesDir, "id_dropbear").createNewFile()

        val pkgName = context.applicationContext.packageName
        val sourceDir = File(TestUtils.primaryStorageDir(context), "$pkgName-source")
        val targetDir = File(TestUtils.primaryStorageDir(context), "$pkgName-target")

        sourceDir.mkdir()
        File(sourceDir, "testfile").createNewFile()
        targetDir.deleteRecursively()

        val config =
            RsyncConfig(
                "-r ${sourceDir.absolutePath}/ ${targetDir.absolutePath}/",
                "",
                false,
            )
        RsyncRunner().run(context, TaskerInput(config))

        assertTrue(File(targetDir, "testfile").exists())
        sourceDir.deleteRecursively()
        targetDir.deleteRecursively()
    }
}
