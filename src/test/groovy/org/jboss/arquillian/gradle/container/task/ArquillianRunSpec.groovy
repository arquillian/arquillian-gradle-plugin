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
package org.jboss.arquillian.gradle.container.task

import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.jboss.arquillian.gradle.UnitTestFixture
import org.jboss.arquillian.gradle.task.ArquillianRun
import org.jboss.arquillian.gradle.utils.ContainerManager
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Arquillian run task unit test.
 *
 * @author Benjamin Muschko
 */
class ArquillianRunSpec extends Specification {
    static final TASK_NAME = 'arquillianJettyRun'
    Project project
    ContainerManager mockContainerManager
    final File deployableFile = new File('build/tmp/tests/deployable.war')

    def setup() {
        project = ProjectBuilder.builder().build()
        mockContainerManager = Mock(ContainerManager)
        UnitTestFixture.createTempFile(deployableFile)
    }

    def cleanup() {
        UnitTestFixture.deleteTempFile(deployableFile)
    }

    def "Executes task for thrown exception"() {
        expect:
            project.tasks.findByName(TASK_NAME) == null
        when:
            Task task = project.task(TASK_NAME, type: ArquillianRun) {
                arquillianClasspath = project.files([])
                deployable = deployableFile
            }

            task.containerManager = mockContainerManager
            task.start()
        then:
            project.tasks.findByName(TASK_NAME) != null
            1 * mockContainerManager.init() >> { throw new RuntimeException() }
            thrown(GradleException)
            0 * mockContainerManager.setup()
            0 * mockContainerManager.start()
            0 * mockContainerManager.deploy(deployableFile)
    }

    def "Executes task if deployable is not provided"() {
        expect:
            project.tasks.findByName(TASK_NAME) == null
        when:
            File nonExistentDeployable = new File('build/tmp/tests/notexistent.war')
            Task task = project.task(TASK_NAME, type: ArquillianRun) {
                arquillianClasspath = project.files([])
                deployable = nonExistentDeployable
            }

            task.containerManager = mockContainerManager
            task.start()
        then:
            project.tasks.findByName(TASK_NAME) != null
            thrown(InvalidUserDataException)
            0 * mockContainerManager.init()
            0 * mockContainerManager.deploy(nonExistentDeployable)
    }

    @Ignore
    def "Executes task successfully"() {
        expect:
            project.tasks.findByName(TASK_NAME) == null
        when:
            Task task = project.task(TASK_NAME, type: ArquillianRun) {
                arquillianClasspath = project.files([])
                deployable = deployableFile
            }

            task.containerManager = mockContainerManager
            task.start()
        then:
            project.tasks.findByName(TASK_NAME) != null
            1 * mockContainerManager.init()
            1 * mockContainerManager.setup()
            1 * mockContainerManager.start()
            1 * mockContainerManager.deploy(deployableFile)
    }
}
