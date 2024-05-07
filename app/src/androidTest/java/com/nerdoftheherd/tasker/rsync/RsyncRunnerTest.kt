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
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.io.File

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    fun syncFromAndToPrimaryStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        File(context.filesDir, "id_dropbear").createNewFile()
        TestUtils.setExternalStoragePermission(context, true)

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

        val output = RsyncRunner().run(context, TaskerInput(config))
        assertTrue(output.success)

        assertTrue(File(targetDir, "testfile").exists())
        sourceDir.deleteRecursively()
        targetDir.deleteRecursively()
    }

    @Test(timeout = 1500)
    fun errorFromTimeout() {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = RsyncConfig("-v test@example.com:remote local", "", false)

        File(context.filesDir, "id_dropbear").outputStream().use { fileOut ->
            assets.open("private_key_ed25519").copyTo(fileOut)
        }

        val output = RsyncRunner(1500).run(context, TaskerInput(config))
        assertFalse(output.success)

        val error = output as TaskerPluginResultErrorWithOutput<CommandOutput>
        assertTrue(error.message.endsWith("\n${context.getString(R.string.process_killed_timeout)}\n"))
    }

    @Test
    fun errorMessageFromMissingSrcPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val srcPath = "${TestUtils.primaryStorageDir(context)}/src"
        val config = RsyncConfig("$srcPath dest", "", false)

        File(context.filesDir, "id_dropbear").createNewFile()
        TestUtils.setExternalStoragePermission(context, false)

        val output = RsyncRunner().run(context, TaskerInput(config))
        assertFalse(output.success)

        val error = output as TaskerPluginResultErrorWithOutput<CommandOutput>

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            assertEquals(
                context.getString(R.string.missing_legacy_storage_permission, srcPath),
                error.message,
            )
            return
        }

        assertEquals(
            context.getString(R.string.missing_storage_permission, srcPath),
            error.message,
        )
    }

    @Test
    fun errorMessageFromMissingDestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val destPath = "${TestUtils.primaryStorageDir(context)}/dest"
        val config = RsyncConfig("src $destPath", "", false)

        File(context.filesDir, "id_dropbear").createNewFile()
        TestUtils.setExternalStoragePermission(context, false)

        val output = RsyncRunner().run(context, TaskerInput(config))
        assertFalse(output.success)

        val error = output as TaskerPluginResultErrorWithOutput<CommandOutput>

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            assertEquals(
                context.getString(R.string.missing_legacy_storage_permission, destPath),
                error.message,
            )
            return
        }

        assertEquals(
            context.getString(R.string.missing_storage_permission, destPath),
            error.message,
        )
    }

    @Test
    fun errorMessageFromMissingLogPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val logPath = "${TestUtils.primaryStorageDir(context)}/rsync.log"
        val config = RsyncConfig("--log-file=$logPath src dest", "", false)

        File(context.filesDir, "id_dropbear").createNewFile()
        TestUtils.setExternalStoragePermission(context, false)

        val output = RsyncRunner().run(context, TaskerInput(config))
        assertFalse(output.success)

        val error = output as TaskerPluginResultErrorWithOutput<CommandOutput>

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            assertEquals(
                context.getString(R.string.missing_legacy_storage_permission, logPath),
                error.message,
            )
            return
        }

        assertEquals(
            context.getString(R.string.missing_storage_permission, logPath),
            error.message,
        )
    }

    @Test
    fun errorMessageFromMissingSdcardPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = RsyncConfig("/sdcard/src dest", "", false)

        File(context.filesDir, "id_dropbear").createNewFile()
        TestUtils.setExternalStoragePermission(context, false)

        val output = RsyncRunner().run(context, TaskerInput(config))
        assertFalse(output.success)

        val error = output as TaskerPluginResultErrorWithOutput<CommandOutput>

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            assertEquals(
                context.getString(R.string.missing_legacy_storage_permission, "/sdcard/src"),
                error.message,
            )
            return
        }

        assertEquals(
            context.getString(R.string.missing_storage_permission, "/sdcard/src"),
            error.message,
        )
    }
}
