/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.service;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Service for creating proxies for remote OSGi services.
 * 
 * @author Dirk Rossow
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public interface RemoteServiceHandler extends Serializable {

    /**
     * Creates a proxy for a remote OSGi service. Every invocation of a method of the proxy object
     * can throw a {@link CommunicationException} wrapped into an
     * {@link UndeclaredThrowableException}.
     * 
     * @param platformIdentifier {@link PlatformIdentifier} where the remote service is hosted.
     * @param serviceIface Interface of the desired service.
     * @param ifaces Interfaces of the expected return object. null if no additional interfaces
     *        expected.
     * @param serviceProperties The desired properties of the remote service. The properties will
     *        coupled by a logical And. null if no properties desired.
     * @return The proxy.
     */
    Object createServiceProxy(PlatformIdentifier platformIdentifier, Class<?> serviceIface, Class<?>[] ifaces,
        Map<String, String> serviceProperties);

    /**
     * Creates a proxy for a remote OSGi service. Every invocation of a method of the proxy object
     * can throw a {@link CommunicationException} wrapped into an
     * {@link UndeclaredThrowableException}.
     * 
     * @param platformIdentifier {@link PlatformIdentifier} where the remote service is hosted.
     * @param serviceIface Interface of the desired service.
     * @param ifaces Interfaces of the expected return object.
     * @param serviceProperties The desired properties of the remote service as LDAP name.
     * @return The proxy.
     */
    Object createServiceProxy(PlatformIdentifier platformIdentifier, Class<?> serviceIface, Class<?>[] ifaces, String serviceProperties);

}
