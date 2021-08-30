/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RsyncRunnerTest {
    @Rule @JvmField
    val expecter: ExpectedException = ExpectedException.none()

    @Test
    fun errorFromFailure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = RsyncConfig("--invalid")

        expecter.expect(RuntimeException::class.java)
        RsyncRunner().run(context, TaskerInput(config))
    }
}
