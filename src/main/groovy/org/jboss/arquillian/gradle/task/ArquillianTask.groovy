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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.jboss.arquillian.gradle.utils.ArquillianContainerManager
import org.jboss.arquillian.gradle.utils.ArquillianSystemProperty
import org.jboss.arquillian.gradle.utils.ArquillianThreadContextClassLoader
import org.jboss.arquillian.gradle.utils.ContainerManager
import org.jboss.arquillian.gradle.utils.ThreadContextClassLoader

/**
 * Arquillian parent task.
 *
 * @author Benjamin Muschko
 * @author Aslak Knutsen
 */
abstract class ArquillianTask extends DefaultTask {
    static final String TASK_GROUP = 'Arquillian'
    static final String CONTAINER_PROFILE_PREFIX = 'gradle_container'
    ThreadContextClassLoader threadContextClassLoader
    ContainerManager containerManager

    /**
     * Arquillian classpath including the core libraries and container adapter libraries.
     */
    @InputFiles
    FileCollection arquillianClasspath

    /**
     * Arquillian container name needed to set the correct configuration properties via the profile.
     */
    @Input
    String containerName

    /**
     * Defines the configuration options for a container.
     */
    @Input
    Map<String, Object> config = [:]

    /**
     * Flag that indicates if Arquillian should be run in debug mode.
     */
    @Input
    Boolean debug

    ArquillianTask() {
        group = TASK_GROUP
        threadContextClassLoader = new ArquillianThreadContextClassLoader()
        containerManager = new ArquillianContainerManager()
    }

    @TaskAction
    void start() {
        validateConfiguration()
        logger.info 'Configuring Arquillian container.'
        initSystemProperties()

        threadContextClassLoader.withClasspath(getArquillianClasspath().files) {
            try {
                containerManager.init()
                perform()
            }
            catch(Exception e) {
                logger.error "Failed to perform Arquillian container action", e
                throw new GradleException("Failed to perform Arquillian container action", e)
            }
        }
    }

    /**
     * Initializes Arquillian system properties.
     */
    private void initSystemProperties() {
        if(getDebug()) {
            logger.info "Arquillian container debug logging set to ${getDebug()}."
            System.setProperty(ArquillianSystemProperty.DEBUG.propName, getDebug().toString())
        }

        // Defines a launch profile to set container configuration
        if(getConfig().size() > 0) {
            String launchProfile = "${CONTAINER_PROFILE_PREFIX}_${getContainerName()}"
            System.setProperty(ArquillianSystemProperty.LAUNCH.propName, launchProfile)

            getConfig().each { key, value ->
                logger.info "Setting configuration property '$key' with value '$value'."
                System.setProperty("arq.container.${launchProfile}.configuration.$key", value.toString())
            }
        }
    }

    /**
     * Validates configuration.
     */
    abstract void validateConfiguration()

    /**
     * Performs Arquillian operation.
     */
    abstract void perform()
}
