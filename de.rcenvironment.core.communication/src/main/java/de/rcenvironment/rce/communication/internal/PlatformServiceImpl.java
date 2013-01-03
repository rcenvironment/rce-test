/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.commons.IdGenerator;
import de.rcenvironment.core.communication.model.internal.NodeInformationRegistryImpl;
import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.PlatformIdentityInformation;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.impl.PlatformIdentityInformationImpl;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.configuration.PersistentSettingsService;

/**
 * Implementation of {@link PlatformService}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class PlatformServiceImpl implements PlatformService {

    /**
     * A system property to specify a certain node id for testing. Example usage in command line:
     * "-Dcommunication.overrideNodeId=12312312312312312312312312312312"
     */
    private static final String SYSTEM_PROPERTY_OVERRIDE_NODE_ID = "communication.overrideNodeId";

    /**
     * Regular expression for the node id override value.
     */
    private static final String NODE_ID_OVERRIDE_PATTERN = "[0-9a-f]{32}";

    private static final String PERSISTENT_SETTINGS_KEY_PLATFORM_ID = "rce.platform.persistentId";

    private final Log log = LogFactory.getLog(getClass());

    private ConfigurationService configurationService;

    private CommunicationConfiguration configuration;

    private String serviceBindAddress;

    private String externalHostAddress;

    private PlatformIdentityInformationImpl identityInformation;

    private PersistentSettingsService persistentSettingsService;

    private boolean usingNewCommunicationLayer;

    private PlatformIdentifier localPlatformIdentifier;

    private PlatformIdentifier legacyLocalPlatformIdentifier;

    protected void activate(BundleContext context) {
        configuration = configurationService.getConfiguration(context.getBundle().getSymbolicName(), CommunicationConfiguration.class);
        usingNewCommunicationLayer = configuration.getUseNewCommunicationLayer();
        initializeServiceBindAddress();
        initializeExternalAddress();
        initializePlatformInformation();
        // set up platform identifiers
        legacyLocalPlatformIdentifier = createLegacyPlatformIdentifier();
        if (usingNewCommunicationLayer) {
            localPlatformIdentifier = PlatformIdentifierFactory.fromNodeId(identityInformation.getPersistentNodeId());
        } else {
            localPlatformIdentifier = legacyLocalPlatformIdentifier;
        }
    }

    @Override
    @AllowRemoteAccess
    public PlatformIdentifier getPlatformIdentifier() {
        return localPlatformIdentifier;
    }

    @Override
    @AllowRemoteAccess
    @Deprecated
    public PlatformIdentifier getLegacyPlatformIdentifier() {
        return legacyLocalPlatformIdentifier;
    }

    @Override
    @AllowRemoteAccess
    public PlatformIdentifier getPersistentIdPlatformIdentifier() {
        return PlatformIdentifierFactory.fromNodeId(identityInformation.getPersistentNodeId());
    }

    @Override
    @AllowRemoteAccess
    public synchronized PlatformIdentityInformation getIdentityInformation() {
        // can be shared as it is immutable; synchronization ensures thread visibility
        return identityInformation;
    }

    @Override
    public String getServiceBindAddress() {
        return serviceBindAddress;
    }

    @Override
    @AllowRemoteAccess
    public Set<PlatformIdentifier> getRemotePlatforms() {
        Set<PlatformIdentifier> platforms = new HashSet<PlatformIdentifier>();

        List<String> remoteInstances = configuration.getRemotePlatforms();

        if (!remoteInstances.isEmpty()) {
            for (String remoteInstance : remoteInstances) {
                if (remoteInstance.split(":").length != 2) {
                    log.warn("Invalid configuration of a remote RCE platform: " + remoteInstance);
                } else {
                    platforms.add(PlatformIdentifierFactory.fromHostAndNumberString(remoteInstance));
                }
            }
        }

        return platforms;
    }

    @Override
    public boolean isLocalPlatform(PlatformIdentifier identifier) {
        Assertions.isDefined(identifier, "PlatformIdentifier must not be null.");

        boolean isLocal = false;

        if (usingNewCommunicationLayer) {
            return identityInformation.getPersistentNodeId().equals(identifier.getNodeId());
        }

        final PlatformIdentifier legacyLocalIdentifier = getLegacyPlatformIdentifier();
        // legacy check
        if (legacyLocalIdentifier.getPlatformNumber() == identifier.getPlatformNumber()) {

            if (legacyLocalIdentifier.getHost().equalsIgnoreCase(identifier.getHost())) {
                isLocal = true;
            } else {
                InetAddress[] localAddresses = null;
                try {
                    localAddresses = InetAddress.getAllByName(legacyLocalIdentifier.getHost());
                } catch (UnknownHostException e) {
                    throw new RuntimeException("Could not resolve local hostname, skipping isLocalPlatform test.", e);
                }

                for (InetAddress address : localAddresses) {
                    if (identifier.getHost().equalsIgnoreCase(address.getCanonicalHostName())
                        || identifier.getHost().equalsIgnoreCase(address.getHostName())
                        || identifier.getHost().equals(address.getHostAddress())) {

                        isLocal = true;
                        break;
                    }
                }

                if (!isLocal) {
                    isLocal = "127.0.0.1".equals(identifier.getHost()) || "localhost".equalsIgnoreCase(identifier.getHost());
                }
            }
        }

        return isLocal;
    }

    protected void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }

    protected void bindPersistentSettingsService(PersistentSettingsService newPersistentSettingsService) {
        persistentSettingsService = newPersistentSettingsService;
    }

    private void initializeExternalAddress() {
        externalHostAddress = configuration.getExternalAddress();
        if (externalHostAddress == null || externalHostAddress.isEmpty()) {
            log.warn("Invalid external address configured, using hardcoded default");
            externalHostAddress = "127.0.0.1"; // fallback
        }
        if (!usingNewCommunicationLayer) {
            log.info("Using address " + externalHostAddress + " for communication");
        }
    }

    private void initializeServiceBindAddress() {
        serviceBindAddress = configuration.getBindAddress();
        if (serviceBindAddress == null || serviceBindAddress.isEmpty()) {
            log.warn("Invalid bind address configured, using hardcoded default");
            serviceBindAddress = "0.0.0.0"; // fallback
        }
        if (!usingNewCommunicationLayer) {
            log.info("Using address " + serviceBindAddress + " for local services");
        }
    }

    private synchronized void initializePlatformInformation() {
        // check if a node id override is defined
        String nodeId = System.getProperty(SYSTEM_PROPERTY_OVERRIDE_NODE_ID);
        if (nodeId != null) {
            // validate id form
            if (nodeId.matches(NODE_ID_OVERRIDE_PATTERN)) {
                log.info("Overriding node id: " + nodeId);
            } else {
                log.warn("Ignoring node id override (property '" + SYSTEM_PROPERTY_OVERRIDE_NODE_ID
                    + "') as it does not match the pattern '" + NODE_ID_OVERRIDE_PATTERN + "': " + nodeId);
                // reset to null; this causes fallback to the normal startup behavior
                nodeId = null;
            }
        }
        // standard procedure
        if (nodeId == null) {
            // check for existing persistent node id
            nodeId = persistentSettingsService.readStringValue(PERSISTENT_SETTINGS_KEY_PLATFORM_ID);
            if (nodeId == null) {
                // not found -> generate and save
                nodeId = IdGenerator.randomUUIDWithoutDashes();
                persistentSettingsService.saveStringValue(PERSISTENT_SETTINGS_KEY_PLATFORM_ID, nodeId);
                log.info("Generated and stored persistent platform id " + nodeId);
            } else {
                log.info("Found persistent platform id " + nodeId + " in storage; reusing");
            }
        }
        String publicKey = null; // TODO implement public key
        String platformName = configurationService.getPlatformName();
        boolean isWorkflowHost = configurationService.getIsWorkflowHost();
        identityInformation = new PlatformIdentityInformationImpl(nodeId, publicKey, platformName, isWorkflowHost);
        // register own name for own node id for proper log output
        NodeInformationRegistryImpl.getInstance().updateFrom(identityInformation);
    }

    private PlatformIdentifier createLegacyPlatformIdentifier() {
        int instanceId = configurationService.getPlatformNumber();
        String host = externalHostAddress;
        String name = configurationService.getPlatformName();
        if (host.isEmpty()) {
            try {
                InetAddress addr = InetAddress.getLocalHost();
                host = addr.getCanonicalHostName();
            } catch (UnknownHostException e) {
                log.warn("Could not determine host name, using localhost as default.", e);
                host = "localhost";
            }
        }
        return PlatformIdentifierFactory.fromHostNumberAndName(host, instanceId, name);
    }

    @Override
    public CommunicationConfiguration getConfiguration() {
        return configuration;
    }

}
