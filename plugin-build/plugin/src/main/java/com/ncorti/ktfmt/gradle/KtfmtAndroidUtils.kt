package com.ncorti.ktfmt.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet
import com.ncorti.ktfmt.gradle.KtfmtPluginUtils.createTasksForSourceSet
import java.util.concurrent.Callable
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

internal object KtfmtAndroidUtils {

    internal fun applyKtfmtToAndroidProject(context: KtfmtPluginContext) {
        fun applyKtfmtForAndroid() {
            context.project.extensions.configure(BaseExtension::class.java) {
                it.sourceSets.all { sourceSet ->
                    val srcDirs =
                        sourceSet.java.srcDirs +
                            runCatching {
                                    // As sourceSet.kotlin doesn't exist before AGP 7
                                    (sourceSet.kotlin as? DefaultAndroidSourceDirectorySet)?.srcDirs
                                }
                                .getOrNull()
                                .orEmpty()
                    // Passing Callable, so returned FileCollection, will lazy evaluate it
                    // only when task will need it.
                    // Solves the problem of having additional source dirs in
                    // current AndroidSourceSet, that are not available on eager
                    // evaluation.
                    createTasksForSourceSet(
                        context,
                        sourceSet.name,
                        context.project.files(Callable { srcDirs }),
                    )
                }
            }
        }

        context.project.plugins.withId("com.android.application") { applyKtfmtForAndroid() }
        context.project.plugins.withId("com.android.library") { applyKtfmtForAndroid() }
        context.project.plugins.withId("com.android.test") { applyKtfmtForAndroid() }
        context.project.plugins.withId("com.android.dynamic-feature") { applyKtfmtForAndroid() }
    }
}
