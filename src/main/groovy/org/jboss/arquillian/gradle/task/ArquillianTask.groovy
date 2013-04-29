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
import org.jboss.arquillian.gradle.utils.ContainerManager

import static org.jboss.arquillian.gradle.utils.ArquillianUtils.withThreadContextClassLoader

/**
 * Arquillian parent task.
 *
 * @author Benjamin Muschko
 * @author Aslak Knutsen
 */
abstract class ArquillianTask extends DefaultTask {
    static final String TASK_GROUP = 'Arquillian'
    static final String CONTAINER_PROFILE_NAME = 'gradle_container'
    protected ContainerManager containerManager

    /**
     * Arquillian classpath including the core libraries and container adapter libraries.
     */
    @InputFiles
    FileCollection arquillianClasspath

    /**
     * Defines the configuration options for a container.
     */
    @Input
    Map<String, Object> config

    /**
     * Flag that indicates if Arquillian should be run in debug mode.
     */
    @Input
    Boolean debug

    ArquillianTask(String description) {
        this.description = description
        group = TASK_GROUP
    }

    @TaskAction
    void run() {
        validateConfiguration()
        logger.info 'Configuring Arquillian container.'
        initSystemProperties()

        withThreadContextClassLoader(getArquillianClasspath().files) {
            try {
                containerManager = new ArquillianContainerManager()
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
            System.setProperty(ArquillianSystemProperty.LAUNCH.propName, CONTAINER_PROFILE_NAME)

            getConfig().each { key, value ->
                logger.info "Setting configuration property '$key' with value '$value'."
                System.setProperty("arq.container.${CONTAINER_PROFILE_NAME}.configuration.$key", value.toString())
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
