/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.login.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.authentication.AuthenticationService;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.login.AbstractLogin;
import de.rcenvironment.rce.notification.NotificationService;

/**
 * Class handling services used this {@link Bundle}. The services are injected then provided by
 * getters. This kind of workaround is needed because the class {@link AbstractLogin} can not get
 * the service injected directly because it is abstract and thus can not be instantiated. But this
 * is a prerequisite for declarative service components.
 * 
 * @author Doreen Seider
 * @author Tobias Menden
 */
public class ServiceHandler {

    private static String bundleSymbolicName;

    private static AuthenticationService nullAuthenticationService =
        ServiceUtils.createNullService(AuthenticationService.class);

    private static NotificationService nullNotificationService =
        ServiceUtils.createNullService(NotificationService.class);

    private static ConfigurationService nullConfigurationService =
        ServiceUtils.createNullService(ConfigurationService.class);

    private static AuthenticationService authenticationService = nullAuthenticationService;

    private static NotificationService notificationService = nullNotificationService;

    private static ConfigurationService configurationService = nullConfigurationService;
    
    private static final Log LOGGER = LogFactory.getLog(ServiceHandler.class);

    /**
     * Activation method called by OSGi. Sets the bundle symbolic name.
     * 
     * @param context of the Bundle
     */
    public void activate(BundleContext context) {
        bundleSymbolicName = context.getBundle().getSymbolicName();
        
//        LoginConfiguration loginConfiguration = configurationService.getConfiguration(bundleSymbolicName, LoginConfiguration.class);
        // tries to automatically log in
        
        new SingleUserAutoLogin().login();
        notificationService.send(AbstractLogin.LOGIN_NOTIFICATION_ID, 
            "Anonymouslogin"); //$NON-NLS-1$
        LOGGER.info("You logged in as anonymous! Use (Re-)Login to log in as an actual user.");
    }
    
    /**
     * Deactivation method called by OSGi. Unregisters the publisher.
     * 
     * @param context of the Bundle
     */
    public void deactivate(BundleContext context) {
        notificationService.removePublisher(AbstractLogin.LOGIN_NOTIFICATION_ID);
    }

    /**
     * Bind the ConfigurationService of the LoginConfiguration to configurationService.
     * 
     * @param newConfigurationService The {@link ConfigurationService} to bind.
     */
    public void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }

    /** 
     *
     * Bind the AuthenticationService to authenticationService.
     * 
     * @param newAuthenticationService The {@link AuthenticationService} to bind.
     */
    public void bindAuthenticationService(AuthenticationService newAuthenticationService) {
        authenticationService = newAuthenticationService;
    }

    /**
     * Bind the AuthenticationService to authenticationService.
     * 
     * @param newNotificationService The {@link NotificationService} to bind.
     */
    public void bindNotificationService(NotificationService newNotificationService) {
        notificationService = newNotificationService;
    }

    /**
     * Unbind the {@link NotificationService}.
     * 
     * @param oldConfigurationService The {@link ConfigurationService} to unbind.
     */
    public void unbindConfigurationService(ConfigurationService oldConfigurationService) {
        configurationService = nullConfigurationService;
    }

    /**
     * Unbind the {@link AuthenticationService}.
     * 
     * @param oldAuthenticationService The {@link AuthenticationService} to unbind.
     */
    public void unbindAuthenticationService(AuthenticationService oldAuthenticationService) {
        authenticationService = nullAuthenticationService;
    }
    
    /**
     * Unbind the {@link NotificationService}.
     * 
     * @param oldNotificationService The {@link NotificationService} to unbind.
     */
    protected void unbindNotificationService(NotificationService oldNotificationService) {
        notificationService = nullNotificationService;
    }

    public static String getBundleSymbolicName() {
        return bundleSymbolicName;
    }

    public static ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public static AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public static NotificationService getNotificationService() {
        return notificationService;
    }
}
