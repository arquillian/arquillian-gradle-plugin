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
package org.jboss.arquillian.gradle.task

import org.gradle.api.Project

/**
 * Container task.
 *
 * @author Benjamin Muschko
 */
enum ContainerTask {
    START('arquillianStart', ArquillianStart, 'Starts $containerName $containerVersion container.', []),
    STOP('arquillianStop', ArquillianStop, 'Stops $containerName $containerVersion container.', []),
    DEPLOY('arquillianDeploy', ArquillianDeploy, 'Deploys an archive to $containerName $containerVersion container.', ['assemble']),
    UNDEPLOY('arquillianUndeploy', ArquillianUndeploy, 'Undeploys an archive from $containerName $containerVersion container.', ['assemble']),
    RUN('arquillianRun', ArquillianRun, 'Runs $containerName $containerVersion container.', ['assemble'])

    private final String taskNamePrefix
    private final Class<ArquillianTask> taskType
    private final String description
    private final List<String> taskDependencies

    private ContainerTask(String taskNamePrefix, Class<ArquillianTask> taskType, String description, List<String> taskDependencies) {
        this.taskNamePrefix = taskNamePrefix
        this.taskType = taskType
        this.description = description
        this.taskDependencies = taskDependencies
    }

    /**
     * Creates container task.
     *
     * @param project Project
     * @param containerName Container name
     * @param containerVersion Container version
     * @return Task
     */
    ArquillianTask createTask(Project project, String containerName, String containerVersion) {
        String evaluatedDescription = description.replaceAll('\\$containerName', containerName).replaceAll('\\$containerVersion', containerVersion)
        project.task(buildTaskName(containerName), type: taskType, description: evaluatedDescription, dependsOn: taskDependencies)
    }

    /**
     * Builds task name.
     *
     * @param prefix Prefix
     * @param containerName Container name
     * @return Task name
     */
    private String buildTaskName(String containerName) {
        StringBuilder taskName = new StringBuilder()
        taskName <<= taskNamePrefix
        taskName <<= containerName
        taskName.toString()
    }
}