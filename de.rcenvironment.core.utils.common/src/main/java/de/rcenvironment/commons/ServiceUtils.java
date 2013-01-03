/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Class providing utility methods used for handling services.
 * 
 * @author Doreen Seider
 */
public final class ServiceUtils {

    private ServiceUtils() {}

    /**
     * Constructs the properties filter by the given properties map.
     * 
     * @param properties The map with the properties.
     * @return the properties filter string.
     */
    public static String constructFilter(Map<String, String> properties) {

        String filter = null;
        StringBuffer filterBuffer = new StringBuffer();

        if (properties != null && properties.size() > 0) {
            filterBuffer.append("(&");

            Map<String, String> serviceProperties = properties;
            for (String key : serviceProperties.keySet()) {
                filterBuffer.append("(" + key + "=" + serviceProperties.get(key) + ")");
            }

            filterBuffer.append(")");
            filter = new String(filterBuffer);
        }

        return filter;
    }

    /**
     * Creates a Null object for the given interface.
     * 
     * TODO rename method so it reflects the actual behavior of the proxy -- misc_ro, July 2012
     * 
     * @param iface Java interface to create the Null object for.
     * @param <T> Same as iface.
     * @return The Null object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createNullService(final Class<T> iface) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] { iface },
            new InvocationHandler() {

                public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
                    throw new IllegalStateException("Service not available: " + iface.getCanonicalName());
                }
            });
    }
}
