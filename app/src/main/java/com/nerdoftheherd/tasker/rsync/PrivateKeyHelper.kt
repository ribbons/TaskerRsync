/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutput

class PrivateKeyHelper(config: TaskerPluginConfig<PrivateKeyConfig>) :
    TaskerPluginConfigHelperNoOutput<PrivateKeyConfig, PrivateKeyRunner>(config) {
    override val runnerClass = PrivateKeyRunner::class.java
    override val inputClass = PrivateKeyConfig::class.java
}
