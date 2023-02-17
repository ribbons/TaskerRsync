/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
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

        onBackPressedDispatcher.addCallback(this) {
            backPressed()
        }

        taskerHelper.onCreate()
    }

    private fun backPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            binding.checkForUpdates.isChecked &&
            context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_DENIED
        ) {
            this.requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
            return
        }

        taskerHelper.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            taskerHelper.finishForTasker()
        } else {
            binding.checkForUpdates.isChecked = false
        }
    }

    override fun assignFromInput(input: TaskerInput<RsyncConfig>) = input.regular.run {
        binding.editTextArgs.setText(this.args)
        binding.editTextKnownHosts.setText(this.knownHosts)
        binding.checkForUpdates.isChecked = this.checkForUpdates
    }

    override val inputForTasker get() = TaskerInput(
        RsyncConfig(
            binding.editTextArgs.text.toString(),
            binding.editTextKnownHosts.text.toString(),
            binding.checkForUpdates.isChecked
        )
    )
}
