/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.internal;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.core.communication.routing.NetworkTopologyChangeListener;
import de.rcenvironment.rce.component.ComponentContext;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.ComponentRegistry;
import de.rcenvironment.rce.component.DistributedComponentRegistry;


/**
 * Implementation of {@link DistributedComponentRegistry}.
 *
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class DistributedComponentRegistryImpl implements DistributedComponentRegistry, NetworkTopologyChangeListener {

    private static final Log LOGGER = LogFactory.getLog(DistributedComponentRegistryImpl.class);
        
    private static List<ComponentDescription> descriptions;
    
    private CommunicationService communicationService;
    
    private PlatformService platformService;
    
    private PlatformIdentifier localPlatform;
    
    private BundleContext bundleCtx;
    
    protected void activate(BundleContext bundleContext) {
        this.bundleCtx = bundleContext;
        localPlatform = platformService.getPlatformIdentifier();
    }
    
    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        this.communicationService = newCommunicationService;
    }
    
    protected void bindPlatformService(PlatformService newPlatformService) {
        this.platformService = newPlatformService;
    }
    
    @Override
    public synchronized List<ComponentDescription> getAllComponentDescriptions(User user, boolean forceRefresh) {
        if (forceRefresh || descriptions == null) {
            descriptions = new ArrayList<ComponentDescription>();
            
            for (PlatformIdentifier pi : communicationService.getAvailableNodes(false)) {
                // Get Component of target platform
                ComponentRegistry registry = (ComponentRegistry) communicationService.getService(ComponentRegistry.class, pi, bundleCtx);
                try {
                    descriptions.addAll(registry.getComponentDescriptions(user, localPlatform));
                } catch (UndeclaredThrowableException e) {
                    LOGGER.warn("Failed to query remote components for platform: " + pi, e);
                }
            }
        }
        
        return descriptions;
    }
    
    @Override
    public ComponentInstanceDescriptor createComponentInstance(User certificate, ComponentDescription description,
        String componentName, ComponentContext context, Boolean inputConnected, PlatformIdentifier platformId) throws ComponentException {
        try {
            ComponentRegistry registry = (ComponentRegistry) communicationService
                .getService(ComponentRegistry.class, platformId, bundleCtx);
            return registry.createComponentInstance(certificate, description, context, componentName, inputConnected);
        } catch (UndeclaredThrowableException e) {
            throw new ComponentException("Failed to create remote component instance", e);
        }
    }

    @Override
    public void disposeComponentInstance(User certificate, String instanceId, PlatformIdentifier platformId) {
        try {
            ComponentRegistry registry = (ComponentRegistry) communicationService
                .getService(ComponentRegistry.class, platformId, bundleCtx);
            registry.disposeComponentInstance(certificate, instanceId);        
        } catch (UndeclaredThrowableException e) {
            LOGGER.warn("Failed to dipose remote component instance: ", e);
        }
    }

    @Override
    public ComponentInstanceDescriptor getComponentInstanceDescriptor(User certificate,
        String instanceId, PlatformIdentifier platformId) {
        try {
            ComponentRegistry registry = (ComponentRegistry) communicationService
                .getService(ComponentRegistry.class, platformId, bundleCtx);
            return registry.getComponentInstanceDescriptor(certificate, instanceId);        
        } catch (UndeclaredThrowableException e) {
            LOGGER.warn("Failed to get remote component information: ", e);
            return null;
        }
    }

    @Override
    public synchronized void onNetworkTopologyChanged() {
        descriptions = null;
    }
}
