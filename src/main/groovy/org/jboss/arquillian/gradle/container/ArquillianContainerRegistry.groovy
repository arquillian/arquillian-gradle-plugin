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
package org.jboss.arquillian.gradle.container

import org.gradle.api.InvalidUserDataException
import org.jboss.arquillian.gradle.ArquillianContainer
import org.jboss.arquillian.gradle.container.parser.JsonContainerDefinitionParser
import org.jboss.arquillian.gradle.container.resolver.ContainerDefinitionResolver
import org.jboss.arquillian.gradle.container.resolver.ProvidedContainerDefinitionResolver

/**
 * Arquillian container registry. On instantiation this class reads the container definition and stores it in memory
 * for later use.
 *
 * @author Benjamin Muschko
 */
@Singleton
class ArquillianContainerRegistry {
    private static final CONTAINERS

    static {
        ContainerDefinitionResolver resolver = new ProvidedContainerDefinitionResolver()
        CONTAINERS = new JsonContainerDefinitionParser().parse(resolver.resolve())
    }

    /**
     * Gets a particular container from the definition. The container is looked up by name, version and type. If the
     * container cannot be found an {@see InvalidUserDataException} is thrown.
     *
     * @param containerConfig Container configuration
     * @return Container
     */
    def getContainer(ArquillianContainer containerConfig) {
        ContainerType containerType = ContainerType.getContainerTypeForIdentifier(containerConfig.type)

        def container = CONTAINERS.find { it.containerName == containerConfig.name &&
                                          it.containerVersion == containerConfig.version &&
                                          it.containerType == containerType.name() }

        if(!container) {
            throw new InvalidUserDataException("Undefined $containerConfig.type '$containerConfig.name' container with version '$containerConfig.version'.")
        }

        container
    }
}
