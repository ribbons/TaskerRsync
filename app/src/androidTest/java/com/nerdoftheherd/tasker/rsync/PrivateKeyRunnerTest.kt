/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.nerdoftheherd.tasker.rsync.config.PrivateKeyConfig
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PrivateKeyRunnerTest {
    @Test
    fun generateKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = TaskerInput(PrivateKeyConfig())

        val keyFile = File(context.filesDir, "id_dropbear")
        keyFile.delete()

        val result = PrivateKeyRunner().run(context, config)
        assertTrue(result.success)
        assertTrue(keyFile.exists())
    }

    @Test
    fun keyExists() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = TaskerInput(PrivateKeyConfig())

        val keyFile = File(context.filesDir, "id_dropbear")
        keyFile.createNewFile()

        val result = PrivateKeyRunner().run(context, config)
        assertFalse(result.success)

        val resultError = result as TaskerPluginResultError
        assertEquals(context.getString(R.string.key_exists), resultError.message)
    }

    @Test
    fun keyExistsOverwrite() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = TaskerInput(PrivateKeyConfig("Ed25519", 256, true))

        val keyFile = File(context.filesDir, "id_dropbear")
        keyFile.createNewFile()
        keyFile.setLastModified(0)

        val result = PrivateKeyRunner().run(context, config)
        assertTrue(result.success)
        assertTrue(keyFile.exists())
        assertThat(keyFile.lastModified(), greaterThan(0))
    }
}
