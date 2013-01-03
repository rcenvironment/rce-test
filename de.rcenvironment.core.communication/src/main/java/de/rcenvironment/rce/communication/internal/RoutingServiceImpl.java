/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.internal;

import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.RoutingService;
import de.rcenvironment.rce.communication.impl.HostAndNumberPlatformIdentifier;
import de.rcenvironment.rce.configuration.ConfigurationService;

/**
 * Default {@link RoutingService} implementation.
 * 
 * @author Robert Mischke
 */
@Deprecated
public class RoutingServiceImpl implements RoutingService {

    private CommunicationService communicationService = ServiceUtils.createNullService(CommunicationService.class);

    private ConfigurationService configurationService;

    private CommunicationConfiguration communicationConfiguration;

    private BundleContext context;

    private PlatformService platformService = ServiceUtils.createNullService(PlatformService.class);

    /**
     * OSGi-DS activation callback.
     * 
     * @param newContext The injected {@link BundleContext}.
     **/
    public void activate(BundleContext newContext) {
        // not used yet
        this.context = newContext;
        this.communicationConfiguration =
            configurationService.getConfiguration(context.getBundle().getSymbolicName(), CommunicationConfiguration.class);
        // routingProtocol = new RoutingProtocol(platformService.getPlatformIdentifier());
    }

    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    protected void unbindPlatformService(PlatformService oldPlatformService) {
        platformService = ServiceUtils.createNullService(PlatformService.class);
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }

    protected void unbindCommunicationService(CommunicationService oldCommunicationService) {
        communicationService = ServiceUtils.createNullService(CommunicationService.class);
    }

    protected void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }

    @Override
    @AllowRemoteAccess
    public NetworkContact getNextRoutingStep(PlatformIdentifier platformIdentifier, CommunicationType communicationType)
        throws CommunicationException {

        // delegate to CommunicationContactMap for "legacy" platform identifiers
        if (platformIdentifier instanceof HostAndNumberPlatformIdentifier) {
            return CommunicationContactMap.getContact(communicationType, platformIdentifier);
        }

        throw new CommunicationException("Persistent ID identifiers are not supported yet");
    }

}
