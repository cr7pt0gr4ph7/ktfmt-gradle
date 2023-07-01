package com.ncorti.ktfmt.gradle

import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal object KtfmtPluginUtils {

    internal const val EXTENSION_NAME = "ktfmt"

    internal const val TASK_NAME_FORMAT = "ktfmtFormat"

    internal const val TASK_NAME_CHECK = "ktfmtCheck"

    @Suppress("LongParameterList")
    internal fun createTasksForSourceSet(
        context: KtfmtPluginContext,
        srcSetName: String,
        srcSetDir: FileCollection
    ) {
        val srcCheckTask = createCheckTask(context, srcSetName, srcSetDir)
        val srcFormatTask = createFormatTask(context, srcSetName, srcSetDir)

        // When running together with compileKotlin, ktfmt tasks should have precedence as
        // they're editing the source code
        context.project.tasks.withType(KotlinCompile::class.java).all {
            it.mustRunAfter(srcCheckTask, srcFormatTask)
        }

        context.topLevelFormat.configure { task -> task.dependsOn(srcFormatTask) }
        context.topLevelCheck.configure { task -> task.dependsOn(srcCheckTask) }

        context.project.plugins.withType(LifecycleBasePlugin::class.java) {
            context.project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure { task ->
                task.dependsOn(srcCheckTask)
            }
        }
    }

    private fun createCheckTask(
        context: KtfmtPluginContext,
        name: String,
        srcDir: FileCollection
    ): TaskProvider<KtfmtCheckTask> {
        val capitalizedName = toCapitalizedName(name)
        val taskName = "$TASK_NAME_CHECK$capitalizedName"
        return context.project.tasks.register(taskName, KtfmtCheckTask::class.java) {
            it.description =
                "Run Ktfmt formatter for sourceSet '$name' on project '${context.project.name}'"
            it.setSource(srcDir)
            it.setIncludes(KtfmtPlugin.defaultIncludes)
            it.setExcludes(KtfmtPlugin.defaultExcludes)
            it.bean = context.ktfmtExtension.toBean()
        }
    }

    private fun createFormatTask(
        context: KtfmtPluginContext,
        name: String,
        srcDir: FileCollection
    ): TaskProvider<KtfmtFormatTask> {
        val capitalizedName = toCapitalizedName(name)
        val taskName = "$TASK_NAME_FORMAT$capitalizedName"
        return context.project.tasks.register(taskName, KtfmtFormatTask::class.java) {
            it.description =
                "Run Ktfmt formatter validation for sourceSet '$name' on project '${context.project.name}'"
            it.setSource(srcDir)
            it.setIncludes(KtfmtPlugin.defaultIncludes)
            it.setExcludes(KtfmtPlugin.defaultExcludes)
            it.bean = context.ktfmtExtension.toBean()
        }
    }

    private fun toCapitalizedName(name: String): String {
        return name.split(" ").joinToString("") {
            val charArray = it.toCharArray()
            if (charArray[0].isLowerCase()) {
                // We use toUpperCase here to retain compatibility with Gradle 6.9 and Kotlin
                // 1.4
                @Suppress("DEPRECATION")
                charArray[0] = charArray[0].toUpperCase()
            }
            charArray.concatToString()
        }
    }
}
