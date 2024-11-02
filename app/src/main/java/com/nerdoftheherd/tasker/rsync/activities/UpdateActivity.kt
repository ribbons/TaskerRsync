/*
 * Copyright © 2022-2024 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.nerdoftheherd.tasker.rsync.R
import com.nerdoftheherd.tasker.rsync.Version
import com.nerdoftheherd.tasker.rsync.VersionInfo
import com.nerdoftheherd.tasker.rsync.databinding.UpdateActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection

class UpdateActivity : AppCompatActivity() {
    private lateinit var binding: UpdateActivityBinding
    private lateinit var info: VersionInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        info =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("info", VersionInfo::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("info")
            }!!

        binding = UpdateActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textSummary.text = getString(R.string.update_summary, info.version)

        if (info.version <= Version.current) {
            setTitle(R.string.update_installed)
            binding.textSummary.text = getString(R.string.updated_summary)
            binding.buttonUpdate.isEnabled = false
            binding.textInfo.isVisible = false
        }

        binding.buttonUpdate.setOnClickListener {
            binding.buttonUpdate.isEnabled = false
            binding.textInfo.text = getString(R.string.update_downloading)

            lifecycleScope.launch {
                val contentUri =
                    FileProvider.getUriForFile(
                        this@UpdateActivity,
                        "${applicationContext.packageName}.provider",
                        download(),
                    )

                val installIntent = Intent(Intent.ACTION_VIEW)
                installIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                installIntent.setDataAndType(
                    contentUri,
                    "application/vnd.android.package-archive",
                )

                startActivity(installIntent)

                binding.buttonUpdate.isEnabled = true
                binding.textInfo.text = getString(R.string.update_choices)
            }
        }

        binding.buttonMoreInfo.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(info.info.toString()),
                ),
            )
        }
    }

    private suspend fun download(): File {
        return withContext(Dispatchers.IO) {
            val conn = info.download.openConnection() as HttpURLConnection
            conn.connect()

            val updatesDir = File(cacheDir, "updates")
            updatesDir.mkdir()

            val file = File(updatesDir, "update.apk")
            val stream = FileOutputStream(file)
            conn.inputStream.copyTo(stream)

            stream.close()
            file
        }
    }
}
