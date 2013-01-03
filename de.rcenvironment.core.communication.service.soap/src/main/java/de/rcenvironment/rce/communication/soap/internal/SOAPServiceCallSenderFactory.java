/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import java.io.IOException;

import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.service.ServiceCallHandler;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSenderFactory;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.jetty.JettyService;

/**
 * Implementation of {@link ServiceCallSenderFactory}.
 * Deploys a Web service server instance and creates SOAP {@link ServiceCallSender} objects.
 *
 * @author Doreen Seider
 * @author Tobias Menden
 */
public class SOAPServiceCallSenderFactory implements ServiceCallSenderFactory {

    private String bundleSymbolicName;

    private PlatformService platformService;

    private JettyService jettyService;
    
    private ConfigurationService configurationService;
    
    private ServiceCallHandler serviceCallHandler;
    
    private CommunicationWebService serverInstance;

    private String serverAddress;

    private String protocol = "http://";

    protected void activate(BundleContext context) throws IOException {
        bundleSymbolicName = context.getBundle().getSymbolicName();
        SOAPConfiguration configuration = configurationService.getConfiguration(bundleSymbolicName, SOAPConfiguration.class);
        serverAddress = protocol + platformService.getServiceBindAddress()
            + ":" + configuration.getPort() + "/SOAPCommunication";
        serverInstance = new CommunicationWebServiceImpl();
        ((CommunicationWebServiceImpl) serverInstance).setServiceCallHandler(serviceCallHandler);
        jettyService.deployWebService(serverInstance, serverAddress);
    }
    
    protected void deactivate() {
        jettyService.undeployWebService(serverAddress);
    }
    
    protected void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }
        
    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }
    
    protected void bindJettyService(JettyService newJettyService) {
        jettyService = newJettyService;
    }
    
    protected void bindServiceCallHandler(ServiceCallHandler newServiceCallHandler) {
        serviceCallHandler = newServiceCallHandler;
    }
    
    @Override
    public ServiceCallSender createServiceCallSender(NetworkContact contact) throws CommunicationException {
        Assertions.isDefined(contact, "The parameter \"contact\" must not be null.");
        SOAPServiceCallSender requestSender = new SOAPServiceCallSender();
        requestSender.setJettyService(jettyService);
        requestSender.setProtocol(protocol);
        requestSender.initialize(contact);
        return requestSender;
    }

}
