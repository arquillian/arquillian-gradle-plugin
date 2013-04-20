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

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Arquillian plugin extension.
 *
 * @author Benjamin Muschko
 * @author Aslak Knutsen
 */
class ArquillianPluginSpec extends Specification {
    static final List<String> ALL_TASK_NAMES

    static {
        ALL_TASK_NAMES = [ArquillianPlugin.START_TASK_NAME, ArquillianPlugin.STOP_TASK_NAME, ArquillianPlugin.DEPLOY_TASK_NAME,
                          ArquillianPlugin.UNDEPLOY_TASK_NAME, ArquillianPlugin.RUN_TASK_NAME]
        ALL_TASK_NAMES.asImmutable()
    }

    Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def "Applies plugin and checks created tasks"() {
        expect:
            ALL_TASK_NAMES.each {
                project.tasks.findByName(it) == null
            }
        when:
            project.apply plugin: 'arquillian'
        then:
            project.plugins.hasPlugin(JavaPlugin)
            project.extensions.findByName(ArquillianPluginExtension.EXTENSION_NAME) != null

            ALL_TASK_NAMES.each {
                project.tasks.findByName(it) != null
            }
    }

    def "Applies plugin for sample task without custom extension configuration"() {
        when:
            project.apply plugin: 'arquillian'
        then:
            project.extensions.findByName(ArquillianPluginExtension.EXTENSION_NAME) != null

            Task task = project.tasks.arquillianRun
            task != null
            !task.debug
            task.deployable == project.tasks.jar.archivePath
            task.arquillianClasspath != null
    }

    def "Applies plugin and configures sample task through extension"() {
        given:
            File warFile = new File('this/is/mywebapp.war')
        when:
            project.apply plugin: 'arquillian'

            project.arquillian {
                debug = true
                deployable = warFile

                container {
                    name = 'tomcat'
                    version = '7'
                    type = 'embedded'
                }
            }
        then:
            Task task = project.tasks.arquillianRun
            task != null
            task.debug == true
            task.deployable == warFile
            task.arquillianClasspath != null
    }

    def "Determines deployable for WAR project"() {
        when:
            project.apply plugin: 'war'
            project.apply plugin: 'arquillian'
        then:
            Task task = project.tasks.arquillianRun
            task != null
            task.deployable == project.tasks.war.archivePath
    }

    def "Determines deployable for EAR project"() {
        when:
            project.apply plugin: 'ear'
            project.apply plugin: 'arquillian'
        then:
            Task task = project.tasks.arquillianRun
            task != null
            task.deployable == project.tasks.ear.archivePath
    }

    def "Determines deployable for Java project"() {
        when:
            project.apply plugin: 'arquillian'
        then:
            Task task = project.tasks.arquillianRun
            task != null
            task.deployable == project.tasks.jar.archivePath
    }

    def "Determines deployable for Groovy project"() {
        when:
            project.apply plugin: 'groovy'
            project.apply plugin: 'arquillian'
        then:
            Task task = project.tasks.arquillianRun
            task != null
            task.deployable == project.tasks.jar.archivePath
    }
}
