package org.jboss.arquillian.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.jboss.arquillian.container.spi.Container
import org.jboss.arquillian.container.spi.ContainerRegistry
import org.jboss.arquillian.container.spi.client.deployment.Deployment
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData
import org.jboss.arquillian.container.spi.event.DeployDeployment
import org.jboss.arquillian.container.spi.event.SetupContainer
import org.jboss.arquillian.container.spi.event.StartContainer
import org.jboss.arquillian.container.spi.event.StopContainer
import org.jboss.arquillian.container.spi.event.UnDeployDeployment
import org.jboss.arquillian.core.api.Instance
import org.jboss.arquillian.core.api.annotation.Inject
import org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader
import org.jboss.arquillian.core.spi.Manager
import org.jboss.arquillian.core.spi.ManagerBuilder
import org.jboss.arquillian.core.spi.NonManagedObserver
import org.jboss.shrinkwrap.api.Archive

class ArquillianPlugin implements Plugin<Project> {

	private static Map<Archive<?>, Deployment> contextMap = new HashMap<Archive<?>, Deployment>();
	private Manager manager;
	
	@Override
	public void apply(Project project) {
		System.setProperty("arquillian.debug", "true");
		
		project.extensions.create('arquillian', ArquillianPluginExtension.class)
		project.task('run') << {
			run(project)
		}
		project.task('start') << {
			start(project)
		}
		project.task('deploy') << {
			deploy(project)
		}
		project.task('undeploy') << {
			undeploy(project)
		}
		project.task('stop') << {
			stop(project)
		}

		
		Set<Dependency> dependencies = [] as Set
		dependencies.add(project.dependencies.create("org.jboss.arquillian.core:arquillian-core-impl-base:1.0.3.Final"))
		dependencies.add(project.dependencies.create("org.jboss.arquillian.container:arquillian-container-impl-base:1.0.3.Final"))
		dependencies.add(project.dependencies.create("org.jboss.arquillian.container:arquillian-jetty-embedded-7:1.0.0.CR1"))
		dependencies.add(project.dependencies.create("org.eclipse.jetty:jetty-webapp:8.1.7.v20120910"))
		dependencies.add(project.dependencies.create("org.eclipse.jetty:jetty-plus:8.1.7.v20120910"))
		
		def container = project.configurations.detachedConfiguration(dependencies as Dependency[])
		
		
		// convert Set<File> to ClassLoader
		// Load Arquillian Gradle Commands
		// Execute Deploy
		// 		war.archivePath
		// 		ear.archivePath
		// 		jar.archivePath
		

		//System.out.println(container.resolve());
	}

	public void run(Project project) {

		start(project);
		deploy(project);
		
		try {
			while(true) {
				Thread.sleep(100);
			}
		}
		catch (InterruptedException e) {
			undeploy(project);
			stop(project);
			manager.fire(new StopContainer(container));
		}
	}

	public void start(Project project) {
		Manager manager = initManager();
		Container container = getDefaultContainer(manager);
		
		manager.fire(new SetupContainer(container));
		manager.fire(new StartContainer(container));
	}
		
	public void deploy(Project project) {
		Manager manager = initManager();
		Container container = getDefaultContainer(manager);
		
		Archive<?> deployment = null;
		manager.fire(new DeployDeployment(container, getOrCreateDeployment(deployment)),
			new NonManagedObserver<DeployDeployment>() {
				@Inject
				private Instance<ProtocolMetaData> metadataInst;

				@Override
				public void fired(DeployDeployment event) {
					ProtocolMetaData metadata = metadataInst.get();
					if (metadata != null) {
						System.out.println(metadata);
					}
				}
			});
	}

	public void undeploy(Project project) {
		Manager manager = initManager();
		Container container = getDefaultContainer(manager);
		
		Archive<?> deployment = null;
		manager.fire(new UnDeployDeployment(container, getOrCreateDeployment(deployment)));
 	}

	public void stop(Project project) {
		Manager manager = initManager();
		Container container = getDefaultContainer(manager);

		manager.fire(new StopContainer(container));
	}

	private static Deployment getOrCreateDeployment(Archive<?> archive) {
		if (contextMap.containsKey(archive)) {
			return contextMap.remove(archive);
		} else {
			Deployment deployment = new Deployment(new DeploymentDescription("NO-NAME", archive));
			contextMap.put(archive, deployment);

			return deployment;
		}
	}

	public Manager initManager() {
		if(manager == null) {
			manager = ManagerBuilder.from().extension(LoadableExtensionLoader.class).create();
			manager.start();
		}
		return manager;
	}

	public Container getDefaultContainer(Manager manager) {
		ContainerRegistry registry = manager.resolve(ContainerRegistry.class);
		return registry.getContainer(TargetDescription.DEFAULT);
	}

	static class ArquillianPluginExtension {
		String name
	}
}
