/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper

class DbclientHelper(config: TaskerPluginConfig<DbclientConfig>) :
    TaskerPluginConfigHelper<DbclientConfig, CommandOutput, DbclientRunner>(config) {
    override val runnerClass = DbclientRunner::class.java
    override val inputClass = DbclientConfig::class.java
    override val outputClass = CommandOutput::class.java
}
