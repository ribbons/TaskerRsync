/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

@TaskerOutputObject
class RsyncOutput(
    @get:TaskerOutputVariable("stdout", R.string.standard_output, R.string.standard_output_desc)
    var stdout: String?,

    @get:TaskerOutputVariable("stderr", R.string.standard_error, R.string.standard_error_desc)
    var stderr: String?,
)
