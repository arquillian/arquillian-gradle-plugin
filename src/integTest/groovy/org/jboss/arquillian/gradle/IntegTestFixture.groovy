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

import org.gradle.api.GradleException
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

/**
 * Integration test fixture.
 *
 * @author Benjamin Muschko
 */
class IntegTestFixture {
    /**
     * Executes build script with Gradle connector. Provides an instance of {@link ProjectConnection} as closure parameter.
     *
     * @param projectDir Project directory
     * @param tasks Tasks
     * @param closure Closure
     */
    static void withGradleConnector(File projectDir, String[] tasks, Closure closure) {
        GradleConnector connector = GradleConnector.newConnector()
        ProjectConnection connection

        try {
            connection = connector.forProjectDirectory(projectDir).connect()
            BuildLauncher buildLauncher = connection.newBuild()
            buildLauncher.forTasks(tasks).run()
            closure(connection)
        }
        finally {
            connection.close()
        }
    }

    /**
     * Creates new temporary directory.
     *
     * @param projectDir Project directory
     */
    static void createTempProjectDir(File projectDir) {
        boolean success = projectDir.mkdirs()

        if(!success) {
            throw new GradleException("Failed to create temporary project directory '$projectDir.canonicalPath'")
        }
    }

    /**
     * Deletes new temporary directory.
     *
     * @param projectDir Project directory
     */
    static void deleteTempProjectDir(File projectDir) {
        if(projectDir.exists()) {
            boolean success = projectDir.deleteDir()

            if(!success) {
                throw new GradleException("Failed to delete temporary project directory '$projectDir.canonicalPath'")
            }
        }
    }
}
