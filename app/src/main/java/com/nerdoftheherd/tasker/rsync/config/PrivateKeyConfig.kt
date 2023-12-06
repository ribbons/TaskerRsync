/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.config

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
class PrivateKeyConfig
    @JvmOverloads
    constructor(
        @field:TaskerInputField("keytype", labelResIdName = "key_type")
        var keyType: String = "Ed25519",
        @field:TaskerInputField("keysize", labelResIdName = "key_size")
        var keySize: Int = 256,
        @field:TaskerInputField("overwrite", labelResIdName = "overwrite_key")
        var overwrite: Boolean = false,
    )
