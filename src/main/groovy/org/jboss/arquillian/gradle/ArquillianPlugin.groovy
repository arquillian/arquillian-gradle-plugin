/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugins.ear.EarPlugin
import org.jboss.arquillian.gradle.task.*

/**
 * Arquillian plugin.
 *
 * @author Benjamin Muschko
 */
class ArquillianPlugin implements Plugin<Project> {
    static final EXTENSION_NAME = 'arquillian'
    static final CONFIGURATION_NAME = 'arquillian'

    @Override
    void apply(Project project) {
        ArquillianPluginExtension extension = project.extensions.create(EXTENSION_NAME, ArquillianPluginExtension)
        project.configurations.add(CONFIGURATION_NAME).setVisible(false).setTransitive(true)
                              .setDescription('The Arquillian libraries to be used for this project.')

        configureParentTask(project, extension)
        configureDeployableTask(project, extension)
        configureLocalContainerTasks(project)
    }

    /**
     * Configures parent task by setting convention properties that apply to all tasks.
     *
     * @param project Project
     * @param extension Extension
     */
    private void configureParentTask(Project project, ArquillianPluginExtension extension) {
        project.tasks.withType(ArquillianTask).whenTaskAdded { task ->
            task.conventionMapping.map('debug') { extension.debug }
            task.conventionMapping.map('config') { extension.config }
            task.conventionMapping.map('launch') { extension.launch }
        }
    }

    /**
     * Configures tasks that handle a deployable.
     *
     * @param project Project
     * @param extension Extension
     */
    private void configureDeployableTask(Project project, ArquillianPluginExtension extension) {
        project.tasks.withType(ArquillianDeployableTask).whenTaskAdded { task ->
            task.conventionMapping.map('deployable') { getDeployable(project, extension) }
        }
    }

    /**
     * Adds and configures the tasks that deal with a local Arquillian container.
     *
     * @param project Project
     * @param extension Extension
     */
    private void configureLocalContainerTasks(Project project) {
        project.task('arquillianStart', type: ArquillianStart)
        project.task('arquillianStop', type: ArquillianStop)
        project.task('arquillianDeploy', type: ArquillianDeploy)
        project.task('arquillianUndeploy', type: ArquillianUndeploy)
        project.task('arquillianRun', type: ArquillianRun)
    }

    /**
     * Gets the deployable artifact. The artifact is determined by the specific nature of the project e.g. if the project
     * that applies the War plugin, assume that we want to to use the WAR archive.
     *
     * A deployable artifact can be provided by through the extension. If the extension defines a deployable, the project's
     * artifact is not used.
     *
     * @param project Project
     * @param extension Extension
     * @return Deployable artifact
     */
    private File getDeployable(Project project, ArquillianPluginExtension extension) {
        File deployable = extension.deployable

        if(deployable) {
            return deployable
        }
        else if(project.plugins.hasPlugin(WarPlugin)) {
            return getArchivePathByTaskName(project, WarPlugin.WAR_TASK_NAME)
        }
        else if(project.plugins.hasPlugin(EarPlugin)) {
            return getArchivePathByTaskName(project, EarPlugin.EAR_TASK_NAME)
        }

        getArchivePathByTaskName(project, Jar.TASK_NAME)
    }

    /**
     * Gets archive path by task name.
     *
     * @param project Project
     * @param taskName Task name
     * @return Archive
     */
    private File getArchivePathByTaskName(Project project, String taskName) {
        project.tasks.getByName(taskName).archivePath
    }
}
