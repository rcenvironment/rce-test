/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.callback.internal;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;

import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.callback.CallbackProxy;
import de.rcenvironment.rce.communication.callback.CallbackProxyService;

/**
 * Implementation of {@link CallbackProxyService}.
 * 
 * @author Doreen Seider
 */
public class CallbackProxyServiceImpl implements CallbackProxyService {

    private Map<String, WeakReference<Object>> proxies = Collections.synchronizedMap(new HashMap<String, WeakReference<Object>>());

    private Map<String, PlatformIdentifier> homePlatforms = Collections.synchronizedMap(new HashMap<String, PlatformIdentifier>());

    private Map<String, Long> ttls = Collections.synchronizedMap(new HashMap<String, Long>());

    protected void activate(BundleContext context) {
        CleanJob.scheduleJob(CallbackProxyService.class, proxies, ttls, homePlatforms);
    }

    protected void deactivate(BundleContext context) {
        CleanJob.unscheduleJob(CallbackProxyService.class);
    }

    @Override
    public void addCallbackProxy(CallbackProxy callBackProxy) {
        String identifier = callBackProxy.getObjectIdentifier();
        proxies.put(identifier, new WeakReference<Object>(callBackProxy));
        homePlatforms.put(identifier, callBackProxy.getHomePlatform());
        ttls.put(identifier, new Date(System.currentTimeMillis() + CleanJob.TTL).getTime());
    }

    @Override
    public Object getCallbackProxy(String objectIdentifier) {
        WeakReference<Object> weakRef = proxies.get(objectIdentifier);
        if (weakRef != null) {
            return weakRef.get();
        } else {
            return null;
        }
    }

    @Override
    @AllowRemoteAccess
    public void setTTL(String objectIdentifier, Long ttl) {
        ttls.put(objectIdentifier, ttl);
    }
}
