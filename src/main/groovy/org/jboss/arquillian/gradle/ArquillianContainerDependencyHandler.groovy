package org.jboss.arquillian.gradle

import org.gradle.api.InvalidUserDataException

/**
 * Dependencies for an Arquillian container.
 *
 * @author Benjamin Muschko
 */
class ArquillianContainerDependencyHandler {
    private List<String> adapterDependencies = []
    private List<String> containerDependencies = []

    void adapter(String... dependencyNotations) {
        adapterDependencies.addAll(dependencyNotations)
    }

    void adapter(Map<String, String> dependencyNotation) {
        checkValidDependencyNotation(dependencyNotation)
        containerDependencies << "${dependencyNotation['group']}:${dependencyNotation['name']}:${dependencyNotation['version']}"
    }

    List<String> getAdapter() {
        adapterDependencies
    }

    void container(String... dependencyNotations) {
        containerDependencies.addAll(dependencyNotations)
    }

    void container(Map<String, String> dependencyNotation) {
        checkValidDependencyNotation(dependencyNotation)
        containerDependencies << "${dependencyNotation['group']}:${dependencyNotation['name']}:${dependencyNotation['version']}"
    }

    List<String> getContainer() {
        containerDependencies
    }

    private void checkValidDependencyNotation(Map<String, String> dependencyNotation) {
        if(!isValidDependencyNotation(dependencyNotation)) {
            throw new InvalidUserDataException("The dependency notation needs to provide the key/value pairs for 'group', 'name' and 'version'.")
        }
    }

    private boolean isValidDependencyNotation(Map<String, String> dependencyNotation) {
        dependencyNotation.containsKey('group') && dependencyNotation.containsKey('name') && dependencyNotation.containsKey('version')
    }
}
