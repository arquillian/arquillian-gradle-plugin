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
package org.jboss.arquillian.gradle.container.parser

import spock.lang.Specification

/**
 * JSON container definition parser specification.
 *
 * @author Benjamin Muschko
 */
class JsonContainerDefinitionParserSpec extends Specification {
    ContainerDefinitionParser containerDefinitionParser

    def setup() {
        containerDefinitionParser = new JsonContainerDefinitionParser()
    }

    def "Successfully resolves provided container JSON file"() {
        given:
            Reader reader = new StringReader(getContainerJson())
        when:
            def json = containerDefinitionParser.parse(reader)
        then:
            json
            json.size() == 1
            json.get(0).containerName == 'jetty'
            json.get(0).containerVersion == '8'
            json.get(0).containerType == 'EMBEDDED'
            json.get(0).dependencies
            json.get(0).dependencies.size() == 2
            json.get(0).configurations
            json.get(0).configurations.size() == 4
    }

    private String getContainerJson() {
        """
            [
                {
                    "dependencies":[
                        {
                            "artifact_id":"jetty-webapp",
                            "group_id":"org.eclipse.jetty",
                            "version":"8.1.7.v20120910"
                        },
                        {
                            "artifact_id":"jetty-plus",
                            "group_id":"org.eclipse.jetty",
                            "version":"8.1.7.v20120910"
                        }
                    ],
                    "artifact_id":"arquillian-jetty-embedded-7",
                    "group_id":"org.jboss.arquillian.container",
                    "version":"1.0.0.CR1",
                    "name":"Arquillian Container Jetty Embedded 7.x and 8.x",
                    "containerName":"jetty",
                    "containerVersion":"8",
                    "containerType":"EMBEDDED",
                    "configurations":[
                        {
                            "type":"java.lang.Integer",
                            "description":"The HTTP port the server should bind to.",
                            "name":"bindHttpPort",
                            "default":"9090"
                        },
                        {
                            "type":"java.lang.String",
                            "description":"The host the server should be run on.",
                            "name":"bindAddress",
                            "default":"localhost"
                        },
                        {
                            "type":"java.lang.Boolean",
                            "description":"Activates the Jetty plus configuration to support JNDI resources (requires jetty-plus and jetty-naming artifacts on the classpath).",
                            "name":"jettyPlus",
                            "default":"true"
                        },
                        {
                            "type":"java.lang.Boolean",
                            "description":"Specify your own Jetty configuration classes as a comma separated list.",
                            "name":"configurationClasses",
                            "default":"null"
                        }
                    ]
                }
            ]
        """
    }
}
