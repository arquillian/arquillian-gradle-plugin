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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.jboss.arquillian.gradle.utils.ArquillianContainerManager
import org.jboss.arquillian.gradle.utils.ContainerManager

/**
 * Arquillian parent task.
 *
 * @author Benjamin Muschko
 * @author Aslak Knutsen
 */
abstract class ArquillianTask extends DefaultTask {
    static final String TASK_GROUP = 'Arquillian'
    protected ContainerManager containerManager

    /**
     * Flag that indicates if Arquillian should be run in debug mode.
     */
    @Input
    Boolean debug

    /**
     * The Arquillian configuration file. If no configuration file is provided, a container can be managed but no
     * integration tests can be run against it.
     */
    @InputFile
    @Optional
    File config

    /**
     * The Arquillian container to be launched. If no container name is provided, Arquillian will pick the one from the
     * configuration file marked as default.
     */
    @Input
    @Optional
    String launch

    ArquillianTask(String description) {
        this.description = description
        group = TASK_GROUP
    }

    @TaskAction
    void run() {
        validateConfiguration()
        logger.info 'Configuring Arquillian container.'
        initSystemProperties()

        try {
            containerManager = new ArquillianContainerManager()
            perform()
        }
        catch(Exception e) {
            logger.error "Failed to perform Arquillian container action", e
            throw new GradleException("Failed to perform Arquillian container action", e)
        }
    }

    /**
     * Initializes Arquillian system properties.
     */
    private void initSystemProperties() {
        if(getDebug()) {
            logger.info "Arquillian container debug logging set to ${getDebug()}."
            System.setProperty('arquillian.debug', getDebug().toString())
        }

        if(getConfig()) {
            logger.info "Using Arquillian configuration file '${getConfig()}'."
            System.setProperty('arquillian.xml', getConfig().canonicalPath)
        }

        if(getLaunch()) {
            logger.info "Selecting Arquillian container '${getLaunch()}'."
            System.getProperty('arquillian.launch', getLaunch())
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
