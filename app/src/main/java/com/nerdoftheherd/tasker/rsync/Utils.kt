/*
 * Copyright © 2021-2024 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import java.io.File

class Utils {
    companion object {
        const val ERROR_NO_PRIVATE_KEY = 100
        const val ERROR_MISSING_STORAGE_PERMISSION = 101
        const val KEY_FILENAME = "id_dropbear"

        fun privateKeyFile(context: Context): File =
            File(context.filesDir, KEY_FILENAME)
    }
}
