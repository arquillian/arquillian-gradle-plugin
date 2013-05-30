package org.jboss.arquillian.gradle

import org.gradle.util.ConfigureUtil
import org.jboss.arquillian.gradle.container.ContainerType

/**
 * Configuration options for an Arquillian container.
 *
 * @author Benjamin Muschko
 */
class ArquillianContainer {
    /**
     * The container name.
     */
    final String name

    ArquillianContainer() {
        this('jetty')
    }

    ArquillianContainer(String name) {
        this.name = name
    }

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

    /**
     * The dependency handler for a container.
     */
    private final ArquillianContainerDependencyHandler dependencyHandler = new ArquillianContainerDependencyHandler()

    /**
     * Configures dependencies for a container {@link ArquillianContainerDependencyHandler}.
     *
     * @param config Configuration
     */
    void dependencies(Closure config) {
        ConfigureUtil.configure(config, dependencyHandler, Closure.DELEGATE_ONLY)
    }

    /**
     * Gets registered dependencies for container.
     *
     * @return Dependencies
     */
    ArquillianContainerDependencyHandler getDependencies() {
        dependencyHandler
    }
}
