package org.jboss.arquillian.gradle

import org.jboss.arquillian.gradle.container.ContainerType

/**
 * Configuration options for an Arquillian container.
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
}
