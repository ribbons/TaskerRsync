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

        fun privateKeyFile(context: Context): File {
            return File(context.filesDir, "id_dropbear")
        }
    }
}
