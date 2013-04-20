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
package org.jboss.arquillian.gradle.utils

import groovy.util.logging.Slf4j

import java.lang.reflect.Constructor

import static org.jboss.arquillian.gradle.utils.ArquillianUtils.loadClass

/**
 * Arquillian container manager that provides useful and reoccuring operations.
 *
 * @author Benjamin Muschko
 * @author Aslak Knutsen
 */
@Slf4j
class ArquillianContainerManager implements ContainerManager {
    private manager
    private container
    private contextMap = [:]

    ArquillianContainerManager() {
        initManager()
        createDefaultContainer()
    }

    /**
     * Initializes Arquillian manager.
     */
    private void initManager() {
        Class extension = loadClass('org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader')
        def managerBuilder = loadClass('org.jboss.arquillian.core.spi.ManagerBuilder')
        manager = managerBuilder.from().extension(extension).create()
        manager.start()
    }

    /**
     * Creates default Arquillian container.
     */
    private void createDefaultContainer() {
        Class registryClass = loadClass('org.jboss.arquillian.container.spi.ContainerRegistry')
        def registry = manager.resolve(registryClass)
        Class targetDescription = loadClass('org.jboss.arquillian.container.spi.client.deployment.TargetDescription')
        container = registry.getContainer(targetDescription.DEFAULT)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void setup() {
        fireContainerEvent('org.jboss.arquillian.container.spi.event.SetupContainer')
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void start() {
        fireContainerEvent('org.jboss.arquillian.container.spi.event.StartContainer')
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void stop() {
        fireContainerEvent('org.jboss.arquillian.container.spi.event.StopContainer')
    }

    private void fireContainerEvent(String className) {
        Class setupContainer = loadClass(className)
        Class containerClazz = loadClass('org.jboss.arquillian.container.spi.Container')
        Constructor constructor = setupContainer.getConstructor(containerClazz)
        manager.fire(constructor.newInstance(container))
    }

    /**
     * Creates deployable archive.
     *
     * @return Deployable archive
     */
    private createDeployableArchive(File deployable) {
        Class genericArchive = loadClass('org.jboss.shrinkwrap.api.GenericArchive')
        Class zipImporter = loadClass('org.jboss.shrinkwrap.api.importer.ZipImporter')
        Class shrinkWrap = loadClass('org.jboss.shrinkwrap.api.ShrinkWrap')
        shrinkWrap.create(zipImporter, deployable.name).importFrom(deployable).as(genericArchive)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void deploy(File deployable) {
        def deployment = createDeployableArchive(deployable)
        Class containerClazz = loadClass('org.jboss.arquillian.container.spi.Container')
        Class deploymentClazz = loadClass('org.jboss.arquillian.container.spi.client.deployment.Deployment')
        Class deployDeployment = loadClass('org.jboss.arquillian.container.spi.event.DeployDeployment')
        Constructor constructor = deployDeployment.getConstructor(containerClazz, deploymentClazz)

        manager.fire(constructor.newInstance(container, getOrCreateDeployment(deployment)))
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void undeploy(File deployable) {
        def deployment = createDeployableArchive(deployable)
        Class containerClazz = loadClass('org.jboss.arquillian.container.spi.Container')
        Class deploymentClazz = loadClass('org.jboss.arquillian.container.spi.client.deployment.Deployment')
        Class deployDeployment = loadClass('org.jboss.arquillian.container.spi.event.UnDeployDeployment')
        Constructor constructor = deployDeployment.getConstructor(containerClazz, deploymentClazz)

        manager.fire(constructor.newInstance(container, getOrCreateDeployment(deployment)))
    }

    /**
     * Gets or creates deployment.
     *
     * @param archive Archive
     * @return Deployment
     */
    private getOrCreateDeployment(archive) {
        if(contextMap.containsKey(archive)) {
            return contextMap.remove(archive)
        }

        Class deploymentClazz = loadClass('org.jboss.arquillian.container.spi.client.deployment.Deployment')
        Class deploymentDescriptionClazz = loadClass('org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription')
        Class archiveClass = loadClass('org.jboss.shrinkwrap.api.Archive')
        Constructor deploymentDescriptionConstructor = deploymentDescriptionClazz.getConstructor(String, archiveClass)
        def deploymentDescription = deploymentDescriptionConstructor.newInstance('NO-NAME', archive)
        Constructor deploymentConstructor = deploymentClazz.getConstructor(deploymentDescriptionClazz)
        def deployment = deploymentConstructor.newInstance(deploymentDescription)
        contextMap.put(archive, deployment)
        deployment
    }
}
