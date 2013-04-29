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

import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.InputFile

/**
 * Arquillian task that handles a deployable.
 *
 * @author Benjamin Muschko
 * @author Aslak Knutsen
 */
abstract class ArquillianDeployableTask extends ArquillianTask {
    /**
     * The deployable artifact. This can either be a WAR, EAR or JAR file.
     */
    @InputFile
    File deployable

    ArquillianDeployableTask(String description) {
        super(description)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void validateConfiguration() {
        if(!getDeployable().exists()) {
            throw new InvalidUserDataException("The provided deployable file '${getDeployable().canonicalPath}' does not exist.")
        }
        else {
            logger.info "Deploying artifact '${getDeployable().canonicalPath}' to container."
        }
    }
}
