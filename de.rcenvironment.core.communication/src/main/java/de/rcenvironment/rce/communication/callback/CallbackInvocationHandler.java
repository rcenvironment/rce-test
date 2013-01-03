/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.callback;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.service.ServiceCallRequest;
import de.rcenvironment.rce.communication.service.ServiceCallResult;
import de.rcenvironment.rce.communication.service.internal.RemoteServiceCallBridge;

/**
 * {@link InvocationHandler} implementation used to create proxy for objects which need to be called
 * back.
 * 
 * @author Doreen Seider
 * @author Robert Mischke
 */
public class CallbackInvocationHandler implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOGGER = LogFactory.getLog(CallbackInvocationHandler.class);

    private final CallbackObject callbackObject;

    private final String objectIdentifier;

    private final PlatformIdentifier objectHome;

    private final PlatformIdentifier proxyHome;

    public CallbackInvocationHandler(CallbackObject callbackObject, String objectIdentifier,
        PlatformIdentifier objectHome, PlatformIdentifier proxyHome) {

        this.callbackObject = callbackObject;
        this.objectIdentifier = objectIdentifier;
        this.objectHome = objectHome;
        this.proxyHome = proxyHome;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
        final String methodName = method.getName();
        Object returnValue; // extracted to satisfy CheckStyle
        if (methodName.equals("getHomePlatform")) {
            returnValue = objectHome;
        } else if (methodName.equals("getObjectIdentifier")) {
            returnValue = objectIdentifier;
        } else {
            if (matchesCallbackMethod(method)) {
                returnValue = invokeRemoteMethod(methodName, parameters);
            } else {
                returnValue = method.invoke(callbackObject, parameters);
            }
        }
        return returnValue;
    }

    /**
     * Performs a remote method invocation on the wrapped {@link CallbackObject}.
     * 
     * @param methodName the name of the method to invoke
     * @param parameters the parameters to pass to the method
     * 
     * @return the return value of the remote method call
     * @throws Throwable any {@link Throwable} that was throws by the remote method call
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object invokeRemoteMethod(final String methodName, Object[] parameters) throws Throwable {

        List<Serializable> parameterList = new ArrayList<Serializable>();
        parameterList.add(0, objectIdentifier);
        parameterList.add(1, methodName);
        if (parameters != null) {
            // TODO improve generics handling -- misc_ro
            final List<Object> parametersAsList = Arrays.asList(parameters);
            parameterList.add(2, new ArrayList(parametersAsList));
        } else {
            parameterList.add(2, new ArrayList<Serializable>());
        }

        ServiceCallRequest serviceCallRequest = new ServiceCallRequest(objectHome, proxyHome,
            CallbackService.class.getCanonicalName(), null, "callback", parameterList);

        ServiceCallResult serviceCallResult = RemoteServiceCallBridge.performRemoteServiceCall(serviceCallRequest);

        Throwable throwable = serviceCallResult.getThrowable();
        if (throwable != null) {
            throw throwable;
        }

        return serviceCallResult.getReturnValue();
    }

    /**
     * Determines whether the given method is applicable for remote invocation. This is the case if
     * and only if it implements a method in the interface returned by
     * {@link CallbackObject#getInterface()} of the wrapped {@link CallbackObject}, and if the
     * implemented method has a @{@link Callback} annotation in this interface.
     * 
     * @param method the method to invoke
     * @return true if the method is applicable for remote invocation; see method description
     */
    private boolean matchesCallbackMethod(Method method) {
        final String methodName = method.getName();
        try {
            // check whether the method to invoke is part of the callback interface
            Method interfaceMethod = callbackObject.getInterface().getMethod(methodName, method.getParameterTypes());
            // if it exists, check if it has a @Callback method annotation in the interface
            return interfaceMethod.isAnnotationPresent(Callback.class);
        } catch (NoSuchMethodException e) {
            if (LOGGER.isDebugEnabled()) {
                // do not log some common methods to reduce log volume
                final boolean isCommonMethod =
                    methodName.equals("hashCode") || methodName.equals("equals") || methodName.equals("toString");
                if (!isCommonMethod) {
                    LOGGER.debug("Non-interface method called: " + method);
                }
            }
            // not present in interface -> always dispatch locally
            return false;
        }
    }

}
