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

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.plugins.ear.EarPlugin
import org.jboss.arquillian.gradle.container.ArquillianContainerRegistry
import org.jboss.arquillian.gradle.task.ArquillianDeployableTask
import org.jboss.arquillian.gradle.task.ArquillianTask
import org.jboss.arquillian.gradle.task.ContainerTask

/**
 * Arquillian plugin.
 *
 * @author Benjamin Muschko
 * @author Aslak Knutsen
 */
class ArquillianPlugin implements Plugin<Project> {
    static final String CONFIGURATION_NAME = 'arquillian'

    @Override
    void apply(Project project) {
        project.plugins.apply(JavaPlugin)
        ArquillianPluginExtension extension = project.extensions.create(ArquillianPluginExtension.EXTENSION_NAME, ArquillianPluginExtension)
        project.configurations.create(CONFIGURATION_NAME).setVisible(false).setTransitive(true)
                              .setDescription('The Arquillian libraries to be used for this project.')

        configureDeployableTask(project, extension)
        configureLocalContainerTasks(project, extension)
    }

    /**
     * Configures parent task by setting convention properties that apply to all tasks.
     *
     * @param project Project
     * @param extension Extension
     */
    private void configureTask(Project project, DefaultTask task, ArquillianPluginExtension extension, ArquillianContainer containerConfig) {
        task.conventionMapping.map('arquillianClasspath') {
            def config = project.configurations[CONFIGURATION_NAME]

            if(config.dependencies.empty) {
                def container = ArquillianContainerRegistry.instance.getContainer(containerConfig)

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
        task.conventionMapping.map('containerName') { containerConfig.name }
        task.conventionMapping.map('config') { containerConfig.config }
        task.conventionMapping.map('debug') { extension.debug }
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
    private void configureLocalContainerTasks(Project project, ArquillianPluginExtension extension) {
        project.afterEvaluate {
            // If no container is added by the user, provide the default container.
            if(extension.containers.isEmpty()) {
                extension.containers << new ArquillianContainer()
            }

            extension.containers.each { containerConfig ->
                project.logger.info "Using $containerConfig.type '$containerConfig.name' container with version '$containerConfig.version'."

                String containerName = containerConfig.name.capitalize()
                String containerVersion = containerConfig.version

                ContainerTask.values().each {
                    ArquillianTask task = it.createTask(project, containerName, containerVersion)
                    configureTask(project, task, extension, containerConfig)
                }
            }
        }
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
