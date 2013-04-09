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

import org.jboss.arquillian.container.spi.Container
import org.jboss.arquillian.container.spi.client.container.DeploymentException
import org.jboss.arquillian.container.spi.client.container.LifecycleException
import org.jboss.arquillian.core.spi.Manager
import org.jboss.arquillian.gradle.utils.ArquillianContainerManager
import org.jboss.shrinkwrap.api.Archive
import org.jboss.shrinkwrap.api.GenericArchive

/**
 * Arquillian run task.
 *
 * @author Benjamin Muschko
 */
class ArquillianRun extends ArquillianDeployableTask {
    ArquillianRun() {
        super('Runs Arquillian container.')
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void perform(Manager manager, Container container) throws DeploymentException, LifecycleException {
        logger.info 'Runs Arquillian container and deploying archive to it.'
        Archive<GenericArchive> deployment = createDeployableArchive()
        ArquillianContainerManager arquillianContainerManager = new ArquillianContainerManager()
        arquillianContainerManager.setup(manager, container)
        arquillianContainerManager.start(manager, container)
        arquillianContainerManager.deploy(manager, container, deployment)

        try {
            while(true) {
                Thread.sleep(100)
            }
        }
        catch(InterruptedException e) {
            logger.error "Container was stopped", e
            arquillianContainerManager.undeploy(manager, container, deployment)
            arquillianContainerManager.stop(manager, container)
        }
    }
}