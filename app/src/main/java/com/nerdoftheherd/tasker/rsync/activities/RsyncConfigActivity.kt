/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.nerdoftheherd.tasker.rsync.config.RsyncConfig
import com.nerdoftheherd.tasker.rsync.databinding.RsyncConfigActivityBinding
import com.nerdoftheherd.tasker.rsync.helpers.RsyncHelper

class RsyncConfigActivity : AppCompatActivity(), TaskerPluginConfig<RsyncConfig> {
    override val context: Context get() = applicationContext
    private val taskerHelper by lazy { RsyncHelper(this) }
    private lateinit var binding: RsyncConfigActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = RsyncConfigActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskerHelper.onCreate()
    }

    override fun onBackPressed() {
        taskerHelper.onBackPressed()
    }

    override fun assignFromInput(input: TaskerInput<RsyncConfig>) = input.regular.run {
        binding.editTextArgs.setText(this.args)
    }

    override val inputForTasker get() = TaskerInput(RsyncConfig(binding.editTextArgs.text.toString()))
}
