/*
 * Copyright © 2021-2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import kotlin.math.max

class Utils {
    companion object {
        const val ERROR_NO_PRIVATE_KEY = 100
        const val ERROR_MISSING_STORAGE_PERMISSION = 101
        const val KEY_FILENAME = "id_dropbear"

        fun privateKeyFile(context: Context): File =
            File(context.filesDir, KEY_FILENAME)

        fun setInsetsListeners(
            toolbar: View,
            content: View,
        ) {
            ViewCompat.setOnApplyWindowInsetsListener(
                toolbar,
            ) { view, windowInsets ->
                windowInsets
                    .getInsets(
                        WindowInsetsCompat.Type.systemBars() or
                            WindowInsetsCompat.Type.displayCutout(),
                    ).apply {
                        view.setPadding(this.left, this.top, 0, 0)
                    }

                WindowInsetsCompat.CONSUMED
            }

            ViewCompat.setOnApplyWindowInsetsListener(
                content,
            ) { view, windowInsets ->
                val contentPadding =
                    view.context.resources
                        .getDimension(R.dimen.content_padding)
                        .toInt()

                windowInsets
                    .getInsets(
                        WindowInsetsCompat.Type.systemBars() or
                            WindowInsetsCompat.Type.displayCutout(),
                    ).apply {
                        view.setPadding(
                            max(this.left, contentPadding),
                            contentPadding,
                            max(this.right, contentPadding),
                            max(this.bottom, contentPadding),
                        )
                    }

                WindowInsetsCompat.CONSUMED
            }
        }
    }
}
