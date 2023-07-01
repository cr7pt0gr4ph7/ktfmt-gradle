package com.ncorti.ktfmt.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

internal data class KtfmtPluginContext(
    val project: Project,
    val ktfmtExtension: KtfmtExtension,
    val topLevelFormat: TaskProvider<Task>,
    val topLevelCheck: TaskProvider<Task>
)
