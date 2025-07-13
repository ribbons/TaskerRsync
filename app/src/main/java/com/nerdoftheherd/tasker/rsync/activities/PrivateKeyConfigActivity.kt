/*
 * Copyright © 2021-2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.nerdoftheherd.tasker.rsync.Utils
import com.nerdoftheherd.tasker.rsync.config.PrivateKeyConfig
import com.nerdoftheherd.tasker.rsync.databinding.PrivateKeyConfigActivityBinding
import com.nerdoftheherd.tasker.rsync.helpers.PrivateKeyHelper

class PrivateKeyConfigActivity :
    AppCompatActivity(),
    TaskerPluginConfig<PrivateKeyConfig> {
    override val context: Context get() = applicationContext
    private val taskerHelper by lazy { PrivateKeyHelper(this) }

    private lateinit var binding: PrivateKeyConfigActivityBinding
    private lateinit var keyTypeAdapter: ArrayAdapter<String>
    private lateinit var keySizeAdapter: ArrayAdapter<Int>
    private var initKeySize: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))

        binding = PrivateKeyConfigActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.setInsetsListeners(binding.toolbar, binding.content)

        keyTypeAdapter =
            ArrayAdapter<String>(
                this,
                androidx.appcompat.R.layout
                    .support_simple_spinner_dropdown_item,
            ).also {
                it.addAll("Ed25519", "ECDSA", "RSA")
                binding.keyType.adapter = it
            }

        keySizeAdapter =
            ArrayAdapter<Int>(
                this,
                androidx.appcompat.R.layout
                    .support_simple_spinner_dropdown_item,
            ).also {
                binding.keySize.adapter = it
            }

        binding.keyType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    pos: Int,
                    id: Long,
                ) {
                    keySizeAdapter.clear()

                    when (parent.getItemAtPosition(pos).toString()) {
                        "Ed25519" -> keySizeAdapter.addAll(256)
                        "ECDSA" -> keySizeAdapter.addAll(384, 521)
                        "RSA" -> keySizeAdapter.addAll(2048, 4096)
                    }

                    if (initKeySize != null) {
                        binding.keySize.setSelection(
                            keySizeAdapter.getPosition(initKeySize),
                        )
                        initKeySize = null
                    }
                }

                override fun onNothingSelected(parentView: AdapterView<*>) {}
            }

        onBackPressedDispatcher.addCallback(this) {
            taskerHelper.onBackPressed()
        }

        taskerHelper.onCreate()
    }

    override fun assignFromInput(input: TaskerInput<PrivateKeyConfig>) =
        input.regular.run {
            binding.keyType.setSelection(
                keyTypeAdapter.getPosition(input.regular.keyType),
            )
            initKeySize = input.regular.keySize
            binding.overwrite.isChecked = input.regular.overwrite
        }

    override val inputForTasker get() =
        TaskerInput(
            PrivateKeyConfig(
                binding.keyType.selectedItem.toString(),
                binding.keySize.selectedItem as Int,
                binding.overwrite.isChecked,
            ),
        )
}
