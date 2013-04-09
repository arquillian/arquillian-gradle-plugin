package org.jboss.arquillian.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class ArquillianTestCase {

	@Test
	public void shouldBeAbleToRun() {
		
		Project project = ProjectBuilder.builder().build();
		project.repositories {
		 mavenLocal()
		 mavenCentral()
		}
		project.apply plugin: 'arquillian'
		
		project.tasks.start.execute()
		project.tasks.stop.execute()
		
	}

}
