/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.output

import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

@TaskerOutputObject
class CommandOutput(
    @get:TaskerOutputVariable(
        "stdout",
        labelResIdName = "standard_output",
        htmlLabelResIdName = "standard_output_desc",
    )
    var stdout: String?,
    @get:TaskerOutputVariable(
        "stderr",
        labelResIdName = "standard_error",
        htmlLabelResIdName = "standard_error_desc",
    )
    var stderr: String?,
)
