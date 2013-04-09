package org.jboss.arquillian.gradle.utils

import org.jboss.arquillian.container.spi.Container
import org.jboss.arquillian.container.spi.client.container.DeploymentException
import org.jboss.arquillian.container.spi.client.container.LifecycleException
import org.jboss.arquillian.container.spi.client.deployment.Deployment
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData
import org.jboss.arquillian.container.spi.event.*
import org.jboss.arquillian.core.api.Instance
import org.jboss.arquillian.core.api.annotation.Inject
import org.jboss.arquillian.core.spi.Manager
import org.jboss.arquillian.core.spi.NonManagedObserver
import org.jboss.shrinkwrap.api.Archive

/**
 * Arquillian container manager that provides useful and reoccuring operations.
 *
 * @author Benjamin Muschko
 */
class ArquillianContainerManager {
    private Map<Archive<?>, Deployment> contextMap = new HashMap<Archive<?>, Deployment>()

    void setup(Manager manager, Container container) throws LifecycleException {
        manager.fire(new SetupContainer(container))
    }

    void start(Manager manager, Container container) throws LifecycleException {
        manager.fire(new StartContainer(container))
    }

    void stop(Manager manager, Container container) throws LifecycleException {
        manager.fire(new StopContainer(container))
    }

    void deploy(Manager manager, Container container, Archive<?> deployment) throws DeploymentException {
        manager.fire(new DeployDeployment(container, getOrCreateDeployment(deployment)),
                new NonManagedObserver<DeployDeployment>() {
                    @Inject
                    private Instance<ProtocolMetaData> metadataInst

                    @Override
                    public void fired(DeployDeployment event) {
                        ProtocolMetaData metadata = metadataInst.get()

                        if (metadata != null) {
                            System.out.println(metadata)
                        }
                    }
                });
    }

    void undeploy(Manager manager, Container container, Archive<?> deployment) throws DeploymentException {
        manager.fire(new UnDeployDeployment(container, getOrCreateDeployment(deployment)));
    }

    private Deployment getOrCreateDeployment(Archive<?> archive) {
        if (contextMap.containsKey(archive)) {
            return contextMap.remove(archive)
        }
        else {
            Deployment deployment = new Deployment(new DeploymentDescription('NO-NAME', archive))
            contextMap.put(archive, deployment)
            return deployment
        }
    }
}
