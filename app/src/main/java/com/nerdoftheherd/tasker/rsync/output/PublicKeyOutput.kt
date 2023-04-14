/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.output

import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

@TaskerOutputObject
class PublicKeyOutput(
    @get:TaskerOutputVariable("pubkey", labelResIdName = "public_key", htmlLabelResIdName = "public_key_desc")
    var pubkey: String
)
