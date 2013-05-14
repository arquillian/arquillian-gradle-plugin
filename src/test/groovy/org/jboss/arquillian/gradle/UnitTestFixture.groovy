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

/**
 * Unit test fixtures.
 *
 * @author Benjamin Muschko
 */
class UnitTestFixture {
    /**
     * Creates new temporay file.
     *
     * @param tempFile Temporary file
     */
    static void createTempFile(File tempFile) {
        if(!tempFile.exists()) {
            File tempDir = tempFile.parentFile

            if(!tempDir.exists()) {
                boolean success = tempDir.mkdirs()

                if(!success) {
                    throw new GradleException("Failed to create temporary directory '$tempDir.canonicalPath'")
                }
            }

            boolean createdFile = tempFile.createNewFile()

            if(!createdFile) {
                throw new GradleException("Failed to create file '$tempFile.canonicalPath'")
            }
        }
    }

    /**
     * Deletes new temporary file.
     *
     * @param tempFile Tempory file
     */
    static void deleteTempFile(File tempFile) {
        File tempDir = tempFile.parentFile

        if(tempFile.exists()) {
            boolean deletedFile = tempFile.delete()

            if(!deletedFile) {
                throw new GradleException("Failed to delete temporary file '$tempFile.canonicalPath'")
            }

            boolean deletedDir = tempDir.deleteDir()

            if(!deletedDir) {
                throw new GradleException("Failed to create temporary directory '$tempDir.canonicalPath'")
            }
        }
    }
}
