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

import org.gradle.util.ConfigureUtil
import org.jboss.arquillian.gradle.container.ContainerType

/**
 * Arquillian plugin extension.
 *
 * @author Benjamin Muschko
 * @author Aslak Knutsen
 */
class ArquillianPluginExtension {
    static final String EXTENSION_NAME = 'arquillian'

    /**
     * Configures the Arquillian container to run in debug mode. The debug mode gives you more detailed information
     * on what is happening under the cover when interacting with Arquillian.
     */
    Boolean debug = Boolean.FALSE

    /**
     * The deployable artifact. This can be a WAR, EAR or JAR file.
     */
    File deployable

    /**
     * Configuration options for a specific Arquillian containers.
     */
    List<ArquillianContainer> containers = new ArrayList<ArquillianContainer>()

    /**
     * Configures container configuration options. The specified closure
     * delegates to an instance of {@link ArquillianContainer}.
     *
     * @param config Configuration
     */
    void container(Closure config) {
        ArquillianContainer container = new ArquillianContainer()
        containers << container
        ConfigureUtil.configure(config, container, Closure.DELEGATE_FIRST)
    }
}

/**
 * Configuration options for an Arquillian container.
 */
class ArquillianContainer {
    /**
     * The container name.
     */
    String name = 'jetty'

    /**
     * The container version.
     */
    String version = '8'

    /**
     * The type of container e.g. embedded, managed or remote.
     */
    String type = ContainerType.EMBEDDED.identifier

    /**
     * The container configuration deviating from the default settings.
     */
    Map<String, Object> config = new HashMap<String, Object>()
}