package org.jboss.arquillian.gradle.utils

import groovy.util.logging.Slf4j
import org.jboss.arquillian.container.spi.Container
import org.jboss.arquillian.container.spi.ContainerRegistry
import org.jboss.arquillian.container.spi.client.deployment.Deployment
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData
import org.jboss.arquillian.container.spi.event.*
import org.jboss.arquillian.core.api.Instance
import org.jboss.arquillian.core.api.annotation.Inject
import org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader
import org.jboss.arquillian.core.spi.Manager
import org.jboss.arquillian.core.spi.ManagerBuilder
import org.jboss.arquillian.core.spi.NonManagedObserver
import org.jboss.shrinkwrap.api.Archive
import org.jboss.shrinkwrap.api.GenericArchive
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.importer.ZipImporter

/**
 * Arquillian container manager that provides useful and reoccuring operations.
 *
 * @author Benjamin Muschko
 * @author Aslak Knutsen
 */
@Slf4j
class ArquillianContainerManager implements ContainerManager {
    private Manager manager
    private Container container
    private Map<Archive<?>, Deployment> contextMap = new HashMap<Archive<?>, Deployment>()

    ArquillianContainerManager() {
        initManager()
        createDefaultContainer()
    }

    /**
     * Initializes Arquillian manager.
     */
    private void initManager() {
        manager = ManagerBuilder.from().extension(LoadableExtensionLoader).create()
        manager.start()
    }

    /**
     * Creates default Arquillian container.
     */
    private void createDefaultContainer() {
        ContainerRegistry registry = manager.resolve(ContainerRegistry)
        container = registry.getContainer(TargetDescription.DEFAULT)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void setup() {
        manager.fire(new SetupContainer(container))
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void start() {
        manager.fire(new StartContainer(container))
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void stop() {
        manager.fire(new StopContainer(container))
    }

    /**
     * Creates deployable archive.
     *
     * @return Deployable archive
     */
    private Archive<GenericArchive> createDeployableArchive(File deployable) {
        ShrinkWrap.create(ZipImporter, deployable.name).importFrom(deployable).as(GenericArchive)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void deploy(File deployable) {
        Archive<GenericArchive> deployment = createDeployableArchive(deployable)
        manager.fire(new DeployDeployment(container, getOrCreateDeployment(deployment)),
                new NonManagedObserver<DeployDeployment>() {
                    @Inject
                    private Instance<ProtocolMetaData> metadataInst

                    @Override
                    public void fired(DeployDeployment event) {
                        ProtocolMetaData metadata = metadataInst.get()

                        if(metadata != null) {
                            log.info "Protocol meta data: $metadata"
                        }
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void undeploy(File deployable) {
        Archive<GenericArchive> deployment = createDeployableArchive(deployable)
        manager.fire(new UnDeployDeployment(container, getOrCreateDeployment(deployment)))
    }

    /**
     * Gets or creates deployment.
     *
     * @param archive Archive
     * @return Deployment
     */
    private Deployment getOrCreateDeployment(Archive<?> archive) {
        if(contextMap.containsKey(archive)) {
            return contextMap.remove(archive)
        }

        Deployment deployment = new Deployment(new DeploymentDescription('NO-NAME', archive))
        contextMap.put(archive, deployment)
        deployment
    }
}
