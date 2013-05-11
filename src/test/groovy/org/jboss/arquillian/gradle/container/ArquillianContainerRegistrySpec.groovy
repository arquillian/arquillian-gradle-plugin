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
import spock.lang.Specification

/**
 * Arquillian container registry unit test.
 *
 * @author Benjamin Muschko
 */
class ArquillianContainerRegistrySpec extends Specification {
    def "Get container for default container definition"() {
        given:
            ArquillianContainer containerConfig = new ArquillianContainer()
        when:
            def containerJson = ArquillianContainerRegistry.instance.getContainer(containerConfig)
        then:
            containerJson
            containerJson.containerName == 'jetty'
            containerJson.containerVersion == '8'
            containerJson.containerType == 'EMBEDDED'
    }

    def "Get container for provided container definition"() {
        given:
            ArquillianContainer containerConfig = new ArquillianContainer('glassfish')
            containerConfig.version = '3'
            containerConfig.type = 'embedded'
        when:
            def containerJson = ArquillianContainerRegistry.instance.getContainer(containerConfig)
        then:
            containerJson
            containerJson.containerName == 'glassfish'
            containerJson.containerVersion == '3'
            containerJson.containerType == 'EMBEDDED'
    }

    def "Get container for container that is not available in registry"() {
        given:
            ArquillianContainer containerConfig = new ArquillianContainer('dubiousContainer')
            containerConfig.version = '1'
            containerConfig.type =  'embedded'
        when:
            ArquillianContainerRegistry.instance.getContainer(containerConfig)
        then:
            thrown(InvalidUserDataException)
    }

    def "Get container for invalid container type"() {
        given:
            ArquillianContainer containerConfig = new ArquillianContainer('dubiousContainer')
            containerConfig.version = '1'
            containerConfig.type = 'unknown'
        when:
            ArquillianContainerRegistry.instance.getContainer(containerConfig)
        then:
            thrown(IllegalArgumentException)
    }
}
