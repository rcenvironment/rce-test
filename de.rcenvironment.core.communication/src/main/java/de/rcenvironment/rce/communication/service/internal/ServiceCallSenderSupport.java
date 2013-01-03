/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.service.internal;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.RoutingService;
import de.rcenvironment.rce.communication.internal.CommunicationType;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSender;
import de.rcenvironment.rce.communication.service.spi.ServiceCallSenderFactory;

/**
 * Supportive class that returns {@link ServiceCallSender} objects which are created by
 * {@link ServiceCallSenderFactory}. This class decides which factory has to be used by means of the
 * information of the {@link ServiceCallRequest}.
 * 
 * @author Thijs Metsch
 * @author Heinrich Wendel
 * @author Doreen Seider
 * @author Robert Mischke (switched to RoutingService)
 */
public final class ServiceCallSenderSupport {

    private static final String ERROR_SERVICE_NOT_REGISTERED = "A Communicator factory service providing the desired"
        + " communication protocol \"{0}\" is not registered.";

    private static final String ERROR_BUNDLE_NOT_INSTALLED = "A bundle providing the desired"
        + " communication protocol \"{0}\" is not installed.";

    private static final Log LOGGER = LogFactory.getLog(ServiceCallSenderSupport.class);

    private static BundleContext bundleContext;

    private static volatile RoutingService routingService = ServiceUtils.createNullService(RoutingService.class);;

    /** Only called by OSGi. */
    @Deprecated
    public ServiceCallSenderSupport() {}

    /**
     * Activation method alled by OSGi.
     * 
     * @param context The injected {@link BundleContext}.
     **/
    public void activate(BundleContext context) {
        bundleContext = context;
    }

    /**
     * OSGi service injection.
     * 
     * @param newRoutingService The routingService to set.
     */
    protected static void bindRoutingService(RoutingService newRoutingService) {
        ServiceCallSenderSupport.routingService = newRoutingService;
    }

    /**
     * OSGi service disconnection.
     * 
     * @param oldRoutingService The routingService to remove.
     */
    protected static void unbindRoutingService(RoutingService oldRoutingService) {
        ServiceCallSenderSupport.routingService = ServiceUtils.createNullService(RoutingService.class);
    }

    /**
     * Return a new {@link ServiceCallSender} instance.
     * 
     * @param serviceCallRequest The {@link ServiceCallRequest} for which a
     *        {@link ServiceCallSender} should be created.
     * @return A new {@link ServiceCallSender} object.
     * @throws CommunicationException Thrown if the {@link ServiceCallSender} could not be created.
     */
    public static ServiceCallSender getServiceCallSender(ServiceCallRequest serviceCallRequest)
        throws CommunicationException {

        NetworkContact contact = routingService.getNextRoutingStep(
            serviceCallRequest.getRequestedPlatform(), CommunicationType.SERVICE_CALL);

        // try to start the communication protocol bundle
        Bundle[] bundles = bundleContext.getBundles();

        if (bundles == null) {
            throw new CommunicationException(MessageFormat.format(ERROR_BUNDLE_NOT_INSTALLED, contact.getProtocol()));
        } else {
            for (Bundle bundle : bundles) {
                synchronized (bundleContext) {
                    if (bundle.getSymbolicName().equals(contact.getProtocol()) && bundle.getState() == Bundle.RESOLVED) {
                        try {
                            bundle.start();
                        } catch (BundleException e) {
                            throw new CommunicationException(MessageFormat.format(ERROR_BUNDLE_NOT_INSTALLED, contact.getProtocol()), e);
                        }
                    }
                }
            }
        }

        // TODO review: rewrite to OSGi-DS by building a protocol->service map in bind()? - misc_ro
        // TODO review: add contact->ServiceCallSender cache? - misc_ro

        // try to get the communicator by getting and calling the communicator factory service
        String protocolFilter = "(" + ServiceCallSenderFactory.PROTOCOL + "=" + contact.getProtocol() + ")";

        ServiceReference[] factoryReferences = null;
        try {
            factoryReferences = bundleContext.getAllServiceReferences(ServiceCallSenderFactory.class.getName(), protocolFilter);
        } catch (InvalidSyntaxException e) {
            LOGGER.error("Failed to get a communicator factory service. Invalid protocol filter syntax.");
        }

        ServiceCallSenderFactory serviceCallerFactory;
        if (factoryReferences != null && factoryReferences.length > 0) {
            serviceCallerFactory = (ServiceCallSenderFactory) bundleContext.getService(factoryReferences[0]);
            if (serviceCallerFactory == null) {
                throw new CommunicationException(MessageFormat.format(ERROR_SERVICE_NOT_REGISTERED, contact.getProtocol()));
            }
        } else {
            throw new CommunicationException(MessageFormat.format(ERROR_SERVICE_NOT_REGISTERED, contact.getProtocol()));
        }
        return serviceCallerFactory.createServiceCallSender(contact);
    }
}
