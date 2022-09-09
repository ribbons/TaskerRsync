/*
 * Copyright © 2021-2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.nerdoftheherd.tasker.rsync.config.DbclientConfig
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DbclientRunnerTest {
    @Rule @JvmField
    val expecter: ExpectedException = ExpectedException.none()

    @Test
    fun noPrivateKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = DbclientConfig("-h", "", false)

        val keyFile = File(context.filesDir, "id_dropbear")
        keyFile.delete()

        expecter.expect(RuntimeException::class.java)
        expecter.expectMessage(context.getString(R.string.no_private_key))
        DbclientRunner().run(context, TaskerInput(config))
    }

    @Test
    fun errorFromFailure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = DbclientConfig("localhost", "", false)

        expecter.expect(RuntimeException::class.java)
        DbclientRunner().run(context, TaskerInput(config))
    }
}
