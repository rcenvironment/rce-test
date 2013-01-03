/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.login.internal;

import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import de.rcenvironment.rce.configuration.testutils.MockConfigurationService;
import de.rcenvironment.rce.login.LoginConfiguration;
import de.rcenvironment.rce.login.LoginTestConstants;
import de.rcenvironment.rce.login.LoginMockFactory;

/**
 * 
 * Test case for the class {@link ServiceHandler}.
 * 
 * @author Doreen Seider
 * @author Tobias Menden
 */
public class ServiceHandlerTest extends TestCase {

    private static final int VALIDITY_IN_DAYS = 7;

    /**
     * Test.
     * 
     * @throws Exception if an exception during start or stop calling.
     */
    public void testServiceHandlerForSuccess() throws Exception {
        ServiceHandler handler = new ServiceHandler();

        assertNotNull(ServiceHandler.getAuthenticationService());
        assertNotNull(ServiceHandler.getNotificationService());
        assertNotNull(ServiceHandler.getConfigurationService());

        handler.bindAuthenticationService(LoginMockFactory.getInstance().getAuthenticationServiceMock());
        handler.bindConfigurationService(LoginMockFactory.getInstance().getConfigurationServiceMock());
        handler.bindNotificationService(LoginMockFactory.getInstance().getNotificationServiceMock());
        handler.activate(LoginMockFactory.getInstance().getBundleContextMock());

        assertNotNull(ServiceHandler.getAuthenticationService());
        assertNotNull(ServiceHandler.getNotificationService());
        assertNotNull(ServiceHandler.getConfigurationService());

        handler.bindConfigurationService(new DummyConfigurationService());
        handler.activate(LoginMockFactory.getInstance().getBundleContextMock());

        handler.deactivate(LoginMockFactory.getInstance().getBundleContextMock());
        handler.unbindAuthenticationService(LoginMockFactory.getInstance().getAuthenticationServiceMock());
        handler.unbindConfigurationService(LoginMockFactory.getInstance().getConfigurationServiceMock());
        handler.unbindNotificationService(LoginMockFactory.getInstance().getNotificationServiceMock());

        assertNotNull(ServiceHandler.getAuthenticationService());
        assertNotNull(ServiceHandler.getNotificationService());
        assertNotNull(ServiceHandler.getConfigurationService());

        try {
            ServiceHandler.getAuthenticationService().createUser(EasyMock.createNiceMock(X509Certificate.class), VALIDITY_IN_DAYS);
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }

        try {
            ServiceHandler.getNotificationService().send("kabumm", "peng");
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }

        try {
            ServiceHandler.getConfigurationService().getPlatformNumber();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(true);
        }

    }

    /**
     * Test {@link ConfigurationService} implementation.
     * 
     * @author Doreen Seider
     */
    private class DummyConfigurationService extends MockConfigurationService.ThrowExceptionByDefault {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getConfiguration(String identifier, Class<T> clazz) {
            if (identifier.equals(LoginMockFactory.BUNDLE_SYMBOLIC_NAME)
                && clazz == LoginConfiguration.class) {
                LoginConfiguration config = new LoginConfiguration();
                config.setAutoLogin(true);
                config.setAutoLoginPassword(LoginMockFactory.PASSWORD);
                config.setCertificateFile(LoginTestConstants.USER_1_CERTIFICATE_FILENAME);
                config.setKeyFile(LoginTestConstants.USER_1_KEY_FILENAME);
                return (T) config;
            }
            return null;
        }

        @Override
        public String getAbsolutePath(String identifier, String path) {
            return path;
        }

    }

}
