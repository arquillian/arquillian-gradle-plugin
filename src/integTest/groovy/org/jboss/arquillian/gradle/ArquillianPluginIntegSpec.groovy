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

import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.Task
import spock.lang.Specification

import static org.jboss.arquillian.gradle.IntegTestFixture.*

/**
 * Arquillian plugin integration specification.
 *
 * @author Benjamin Muschko
 */
class ArquillianPluginIntegSpec extends Specification {
    static final File PROJECT_DIR = new File('build/tmpproject')

    def setup() {
        createTempProjectDir(PROJECT_DIR)
    }

    def cleanup() {
        deleteTempProjectDir(PROJECT_DIR)
    }

    def "Applies plugin with default configuration"() {
        given:
            String[] tasks = ['tasks'] as String[]
            File buildFile = new File(PROJECT_DIR, 'build.gradle')
            buildFile << """
                            buildscript {
                                dependencies {
                                    classpath files('../../build/classes/main', '../../build/resources/main')
                                }
                            }

                            apply plugin: 'java'
                            apply plugin: 'arquillian'
                         """
        when:
            GradleProject project

            withGradleConnector(PROJECT_DIR, tasks) { ProjectConnection connection ->
                project = connection.getModel(GradleProject)
            }
        then:
            project.tasks*.name.containsAll(['arquillianDeployJetty', 'arquillianRunJetty', 'arquillianStartJetty',
                                             'arquillianStopJetty', 'arquillianUndeployJetty'])

            findTask(project, 'arquillianDeployJetty').description == 'Deploys an archive to Jetty 8 container.'
            findTask(project, 'arquillianRunJetty').description == 'Runs Jetty 8 container.'
            findTask(project, 'arquillianStartJetty').description == 'Starts Jetty 8 container.'
            findTask(project, 'arquillianStopJetty').description == 'Stops Jetty 8 container.'
            findTask(project, 'arquillianUndeployJetty').description == 'Undeploys an archive from Jetty 8 container.'
    }

    def "Applies plugin with provided user configuration for single container"() {
        given:
            String[] tasks = ['tasks'] as String[]
            File buildFile = new File(PROJECT_DIR, 'build.gradle')
            buildFile << """
                            buildscript {
                                dependencies {
                                    classpath files('../../build/classes/main', '../../build/resources/main')
                                }
                            }

                            apply plugin: 'java'
                            apply plugin: 'arquillian'

                            arquillian {
                                containers {
                                    tomcat {
                                        version = '7'
                                        type = 'embedded'
                                    }
                                }
                            }
                         """
        when:
            GradleProject project

            withGradleConnector(PROJECT_DIR, tasks) { ProjectConnection connection ->
                project = connection.getModel(GradleProject)
            }
        then:
            project.tasks*.name.containsAll(['arquillianDeployTomcat', 'arquillianRunTomcat', 'arquillianStartTomcat',
                    'arquillianStopTomcat', 'arquillianUndeployTomcat'])

            findTask(project, 'arquillianDeployTomcat').description == 'Deploys an archive to Tomcat 7 container.'
            findTask(project, 'arquillianRunTomcat').description == 'Runs Tomcat 7 container.'
            findTask(project, 'arquillianStartTomcat').description == 'Starts Tomcat 7 container.'
            findTask(project, 'arquillianStopTomcat').description == 'Stops Tomcat 7 container.'
            findTask(project, 'arquillianUndeployTomcat').description == 'Undeploys an archive from Tomcat 7 container.'
    }

    def "Applies plugin with provided user configuration for multiple containers"() {
        given:
            String[] tasks = ['tasks'] as String[]
            File buildFile = new File(PROJECT_DIR, 'build.gradle')
            buildFile << """
                            buildscript {
                                dependencies {
                                    classpath files('../../build/classes/main', '../../build/resources/main')
                                }
                            }

                            apply plugin: 'java'
                            apply plugin: 'arquillian'

                            arquillian {
                                containers {
                                    tomcat {
                                        version = '7'
                                        type = 'embedded'
                                    }

                                    glassfish {
                                        version = '3'
                                        type = 'embedded'
                                    }
                                }
                            }
                         """
        when:
            GradleProject project

            withGradleConnector(PROJECT_DIR, tasks) { ProjectConnection connection ->
                project = connection.getModel(GradleProject)
            }
        then:
            project.tasks*.name.containsAll(['arquillianDeployTomcat', 'arquillianRunTomcat', 'arquillianStartTomcat',
                    'arquillianStopTomcat', 'arquillianUndeployTomcat'])
            project.tasks*.name.containsAll(['arquillianDeployGlassfish', 'arquillianRunGlassfish', 'arquillianStartGlassfish',
                    'arquillianStopGlassfish', 'arquillianUndeployGlassfish'])

            findTask(project, 'arquillianDeployTomcat').description == 'Deploys an archive to Tomcat 7 container.'
            findTask(project, 'arquillianRunTomcat').description == 'Runs Tomcat 7 container.'
            findTask(project, 'arquillianStartTomcat').description == 'Starts Tomcat 7 container.'
            findTask(project, 'arquillianStopTomcat').description == 'Stops Tomcat 7 container.'
            findTask(project, 'arquillianUndeployTomcat').description == 'Undeploys an archive from Tomcat 7 container.'
            findTask(project, 'arquillianDeployGlassfish').description == 'Deploys an archive to Glassfish 3 container.'
            findTask(project, 'arquillianRunGlassfish').description == 'Runs Glassfish 3 container.'
            findTask(project, 'arquillianStartGlassfish').description == 'Starts Glassfish 3 container.'
            findTask(project, 'arquillianStopGlassfish').description == 'Stops Glassfish 3 container.'
            findTask(project, 'arquillianUndeployGlassfish').description == 'Undeploys an archive from Glassfish 3 container.'
    }

    private Task findTask(GradleProject project, String name) {
        project.tasks.find { it.name == name }
    }
}
