/*
 * Copyright © 2021-2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.nerdoftheherd.tasker.rsync.Utils
import com.nerdoftheherd.tasker.rsync.config.DbclientConfig
import com.nerdoftheherd.tasker.rsync.databinding.DbclientConfigActivityBinding
import com.nerdoftheherd.tasker.rsync.helpers.DbclientHelper

class DbclientConfigActivity :
    AppCompatActivity(),
    TaskerPluginConfig<DbclientConfig> {
    override val context: Context get() = applicationContext
    private val taskerHelper by lazy { DbclientHelper(this) }
    private lateinit var binding: DbclientConfigActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))

        binding = DbclientConfigActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.setInsetsListeners(binding.toolbar, binding.content)

        onBackPressedDispatcher.addCallback(this) {
            backPressed()
        }

        taskerHelper.onCreate()
    }

    private fun backPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            binding.checkForUpdates.isChecked &&
            context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_DENIED
        ) {
            this.requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0,
            )
            return
        }

        taskerHelper.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
        )

        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            taskerHelper.finishForTasker()
        } else {
            binding.checkForUpdates.isChecked = false
        }
    }

    override fun assignFromInput(input: TaskerInput<DbclientConfig>) =
        input.regular.run {
            binding.editTextArgs.setText(this.args)
            binding.editTextKnownHosts.setText(this.knownHosts)
            binding.checkForUpdates.isChecked =
                this.checkForUpdates ?: if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ) {
                    context.packageManager
                        .getInstallSourceInfo(
                            context.packageName,
                        ).packageSource != PackageInstaller.PACKAGE_SOURCE_STORE
                } else {
                    true
                }
        }

    override val inputForTasker get() =
        TaskerInput(
            DbclientConfig(
                binding.editTextArgs.text.toString(),
                binding.editTextKnownHosts.text.toString(),
                binding.checkForUpdates.isChecked,
            ),
        )
}
