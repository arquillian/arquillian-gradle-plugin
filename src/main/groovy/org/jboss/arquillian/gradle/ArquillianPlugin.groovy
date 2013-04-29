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
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.plugins.ear.EarPlugin
import org.jboss.arquillian.gradle.container.ArquillianContainerRegistry
import org.jboss.arquillian.gradle.task.*

/**
 * Arquillian plugin.
 *
 * @author Benjamin Muschko
 * @author Aslak Knutsen
 */
class ArquillianPlugin implements Plugin<Project> {
    static final String CONFIGURATION_NAME = 'arquillian'
    static final String START_TASK_NAME = 'arquillianStart'
    static final String STOP_TASK_NAME = 'arquillianStop'
    static final String DEPLOY_TASK_NAME = 'arquillianDeploy'
    static final String UNDEPLOY_TASK_NAME = 'arquillianUndeploy'
    static final String RUN_TASK_NAME = 'arquillianRun'

    @Override
    void apply(Project project) {
        project.plugins.apply(JavaPlugin)
        ArquillianPluginExtension extension = project.extensions.create(ArquillianPluginExtension.EXTENSION_NAME, ArquillianPluginExtension)
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
            task.conventionMapping.map('arquillianClasspath') {
                def config = project.configurations[ArquillianPluginExtension.EXTENSION_NAME]

                if(config.dependencies.empty) {
                    def container = ArquillianContainerRegistry.instance.getContainer(extension.container)
                    logger.info "Using $extension.container.type '$extension.container.name' container with version '$extension.container.version'."

                    project.dependencies {
                        // Core Arquillian libraries
                        arquillian 'org.jboss.arquillian.core:arquillian-core-impl-base:1.0.3.Final'
                        arquillian 'org.jboss.arquillian.container:arquillian-container-impl-base:1.0.3.Final'
                        arquillian 'org.jboss.shrinkwrap:shrinkwrap-impl-base:1.1.2'

                        // Container adapter libraries
                        arquillian group: container.group_id, name: container.artifact_id, version: container.version

                        container.dependencies.each { dep ->
                            arquillian group: dep.group_id, name: dep.artifact_id, version: dep.version
                        }
                    }
                }

                config
            }
            task.conventionMapping.map('config') { extension.container.config }
            task.conventionMapping.map('debug') { extension.debug }
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
     * Adds and configures the tasks operating on a local Arquillian container.
     *
     * @param project Project
     * @param extension Extension
     */
    private void configureLocalContainerTasks(Project project) {
        project.task(START_TASK_NAME, type: ArquillianStart)
        project.task(STOP_TASK_NAME, type: ArquillianStop)
        project.task(DEPLOY_TASK_NAME, type: ArquillianDeploy, dependsOn: project.tasks.assemble)
        project.task(UNDEPLOY_TASK_NAME, type: ArquillianUndeploy, dependsOn: project.tasks.assemble)
        project.task(RUN_TASK_NAME, type: ArquillianRun, dependsOn: project.tasks.assemble)
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
            return getArchiveByTaskName(project, WarPlugin.WAR_TASK_NAME)
        }
        else if(project.plugins.hasPlugin(EarPlugin)) {
            return getArchiveByTaskName(project, EarPlugin.EAR_TASK_NAME)
        }
        else if(project.plugins.hasPlugin(JavaPlugin)) {
            return getArchiveByTaskName(project, JavaPlugin.JAR_TASK_NAME)
        }
    }

    /**
     * Gets archive by task name.
     *
     * @param project Project
     * @param taskName Task name
     * @return Archive
     */
    private File getArchiveByTaskName(Project project, String taskName) {
        project.tasks.getByName(taskName).archivePath
    }
}
