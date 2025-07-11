/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigNoInput
import com.nerdoftheherd.tasker.rsync.helpers.PublicKeyHelper

class PublicKeyConfigActivity :
    AppCompatActivity(),
    TaskerPluginConfigNoInput {
    override val context: Context get() = applicationContext
    private val taskerHelper by lazy { PublicKeyHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerHelper.finishForTasker()
    }
}
