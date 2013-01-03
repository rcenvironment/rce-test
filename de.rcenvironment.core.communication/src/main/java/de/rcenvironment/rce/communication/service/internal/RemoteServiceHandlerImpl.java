/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.service.internal;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.callback.CallbackProxyService;
import de.rcenvironment.rce.communication.callback.CallbackService;
import de.rcenvironment.rce.communication.callback.internal.CallbackUtils;
import de.rcenvironment.rce.communication.service.RemoteServiceHandler;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * Implementation of the {@link RemoteServiceHandler}.
 * 
 * @author Dirk Rossow
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public final class RemoteServiceHandlerImpl implements RemoteServiceHandler {

    private static final String ASSERT_MUST_NOT_BE_NULL = " must not be null!";

    private static final long serialVersionUID = -4239349616520603192L;

    private static final Log LOGGER = LogFactory.getLog(RemoteServiceHandlerImpl.class);

    private PlatformService platformService;

    private CallbackService callbackService;

    private CallbackProxyService callbackProxyService;

    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    protected void bindCallbackService(CallbackService newCallbackService) {
        callbackService = newCallbackService;
    }

    protected void bindCallbackProxyService(CallbackProxyService newCallbackProxyService) {
        callbackProxyService = newCallbackProxyService;
    }

    @Override
    public Object createServiceProxy(PlatformIdentifier platformIdentifier, Class<?> serviceIface, Class<?>[] ifaces,
        Map<String, String> serviceProperties) {

        return createServiceProxy(platformIdentifier, serviceIface, ifaces, ServiceUtils.constructFilter(serviceProperties));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object createServiceProxy(final PlatformIdentifier platformIdentifier, final Class<?> serviceIface, Class<?>[] ifaces,
        final String serviceProperties) {

        Assertions.isDefined(platformIdentifier, "The identifier of the requested platform" + ASSERT_MUST_NOT_BE_NULL);
        Assertions.isDefined(serviceIface, "The interface of the requested service" + ASSERT_MUST_NOT_BE_NULL);

        InvocationHandler handler = new InvocationHandler() {

            private PlatformService ps = platformService;

            private CallbackService cs = callbackService;

            private CallbackProxyService cps = callbackProxyService;

            private PlatformIdentifier pi = platformIdentifier;

            private Class<?> myService = serviceIface;

            private String myProperties = serviceProperties;

            public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {

                List parameterList;
                if (parameters != null) {
                    int i = 0;
                    for (Object parameter : parameters) {
                        parameters[i] = CallbackUtils.handleCallbackObject(parameter, pi, cs);
                        i++;
                    }
                    parameterList = Arrays.asList(parameters);

                } else {
                    parameterList = new ArrayList<Serializable>();
                }

                if (myProperties != null && myProperties.isEmpty()) {
                    myProperties = null;
                }

                ServiceCallRequest serviceCallRequest = new ServiceCallRequest(pi, ps.getPlatformIdentifier(),
                    myService.getCanonicalName(), myProperties, method.getName(), parameterList);

                ServiceCallResult serviceCallResult =
                    RemoteServiceCallBridge.performRemoteServiceCall(serviceCallRequest);

                Throwable throwable = serviceCallResult.getThrowable();
                if (throwable != null) {
                    // TODO review: check for UndeclaredThrowables here and log as warning? -
                    // misc_ro
                    LOGGER.debug(MessageFormat.format("{0} thrown @ {1}", throwable.getClass().getName(),
                        serviceCallRequest.getRequestedPlatform()), throwable);
                    throw throwable;

                }
                Object returnValue = serviceCallResult.getReturnValue();
                if (returnValue != null) {
                    returnValue = CallbackUtils.handleCallbackProxy(returnValue, cs, cps);
                }

                return serviceCallResult.getReturnValue();
            }
        };

        if (ifaces == null) {
            return Proxy.newProxyInstance(serviceIface.getClassLoader(), new Class[] { serviceIface }, handler);
        } else {
            Class<?>[] allIfaces = new Class<?>[ifaces.length + 1];
            allIfaces[0] = serviceIface;
            System.arraycopy(ifaces, 0, allIfaces, 1, ifaces.length);
            return Proxy.newProxyInstance(serviceIface.getClassLoader(), allIfaces, handler);
        }
    }
}
