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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.jboss.arquillian.container.spi.Container
import org.jboss.arquillian.container.spi.ContainerRegistry
import org.jboss.arquillian.container.spi.client.container.DeploymentException
import org.jboss.arquillian.container.spi.client.container.LifecycleException
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription
import org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader
import org.jboss.arquillian.core.spi.Manager
import org.jboss.arquillian.core.spi.ManagerBuilder

/**
 * Arquillian parent task.
 *
 * @author Benjamin Muschko
 */
abstract class ArquillianTask extends DefaultTask {
    /**
     * Flag that indicates if Arquillian should be run in debug mode.
     */
    @Input
    Boolean debug

    /**
     * The Arquillian configuration file.
     */
    @InputFile
    @Optional
    File config

    /**
     * The Arquillian container to be launched.
     */
    @Input
    @Optional
    String launch

    ArquillianTask(String description) {
        this.description = description
        group = 'Arquillian'
    }

    @TaskAction
    void run() {
        validateConfiguration()
        logger.info 'Configuring Arquillian container.'
        initSystemProperties()

        Manager manager = initManager()
        Container container = getDefaultContainer(manager)
        perform(manager, container)
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

    private Manager initManager() {
        Manager manager = ManagerBuilder.from().extension(LoadableExtensionLoader).create()
        manager.start()
        manager
    }

    private Container getDefaultContainer(Manager manager) {
        ContainerRegistry registry = manager.resolve(ContainerRegistry)
        return registry.getContainer(TargetDescription.DEFAULT)
    }

    /**
     * Validates configuration.
     */
    abstract void validateConfiguration()

    /**
     * Performs Arquillian operation.
     *
     * @param manager Manager
     * @param container Container
     * @throws DeploymentException
     * @throws LifecycleException
     */
    abstract void perform(Manager manager, Container container) throws DeploymentException, LifecycleException
}
