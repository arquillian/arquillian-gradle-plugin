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

/**
 * The Arquillian container type.
 *
 * @author Benjamin Muschko
 */
enum ContainerType {
    REMOTE('remote'), MANAGED('managed'), EMBEDDED('embedded')

    private final static Map<String, ContainerType> CONTAINERS

    static {
        CONTAINERS = [:]

        values().each {
            CONTAINERS[it.identifier] = it
        }

        CONTAINERS.asImmutable()
    }

    private final String identifier

    private ContainerType(String identifier) {
        this.identifier = identifier
    }

    static ContainerType getContainerTypeForIdentifier(String identifier) {
        if(!CONTAINERS.containsKey(identifier)) {
            throw new IllegalArgumentException("Unsupported or unknown container type for identifier '$identifier'")
        }

        CONTAINERS.get(identifier)
    }

    String getIdentifier() {
        identifier
    }
}