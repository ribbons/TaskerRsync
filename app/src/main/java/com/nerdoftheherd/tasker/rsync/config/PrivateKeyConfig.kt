/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.config

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.nerdoftheherd.tasker.rsync.R

@TaskerInputRoot
class PrivateKeyConfig @JvmOverloads constructor(
    @field:TaskerInputField("keytype", R.string.key_type)
    var keyType: String = "Ed25519",

    @field:TaskerInputField("keysize", R.string.key_size)
    var keySize: Int = 256,

    @field:TaskerInputField("overwrite", R.string.overwrite_key)
    var overwrite: Boolean = false,
)
