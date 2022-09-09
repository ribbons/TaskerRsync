/*
 * Copyright © 2021-2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.nerdoftheherd.tasker.rsync.config.DbclientConfig
import com.nerdoftheherd.tasker.rsync.databinding.DbclientConfigActivityBinding
import com.nerdoftheherd.tasker.rsync.helpers.DbclientHelper

class DbclientConfigActivity : AppCompatActivity(), TaskerPluginConfig<DbclientConfig> {
    override val context: Context get() = applicationContext
    private val taskerHelper by lazy { DbclientHelper(this) }
    private lateinit var binding: DbclientConfigActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DbclientConfigActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskerHelper.onCreate()
    }

    override fun onBackPressed() {
        taskerHelper.onBackPressed()
    }

    override fun assignFromInput(input: TaskerInput<DbclientConfig>) = input.regular.run {
        binding.editTextArgs.setText(this.args)
        binding.editTextKnownHosts.setText(this.knownHosts)
    }

    override val inputForTasker get() = TaskerInput(
        DbclientConfig(
            binding.editTextArgs.text.toString(),
            binding.editTextKnownHosts.text.toString()
        )
    )
}
