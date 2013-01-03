/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.authentication.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.globus.gsi.CertUtil;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;

import de.rcenvironment.rce.authentication.AuthenticationService.LDAPAuthenticationResult;
import de.rcenvironment.rce.authentication.AuthenticationTestConstants;
import de.rcenvironment.rce.authentication.AuthenticationService.X509AuthenticationResult;
import de.rcenvironment.rce.authentication.User;

/**
 * Test case for the implementation of the <code>AuthenticationService</code>.
 * 
 * @author Doreen Seider
 * @author Alice Zorn
 */
public class AuthenticationServiceImplTest extends TestCase {
    
    private AuthenticationServiceImpl authService;
    
    private int validityInDays = 7;

    @Override
    public void setUp() throws Exception {
        authService = new AuthenticationServiceImpl();
        authService.bindConfigurationService(AuthenticationMockFactory.getConfigurationService());
        authService.activate(AuthenticationMockFactory.getBundleContextMock());
    }

    /**
     * Tests authentication for success.
     * 
     * @throws Exception
     *             if the test fails.
     * 
     */
    public void testAuthenticateForSuccess() throws Exception {

        X509Certificate certificate = CertUtil.loadCertificate(getClass()
            .getResourceAsStream(AuthenticationTestConstants.USERCERT_RCE_ENGINEER_PEM));
        OpenSSLKey key = new BouncyCastleOpenSSLKey(getClass().getResourceAsStream(AuthenticationTestConstants.USERKEY_RCE_ENGINEER_PEM));

        X509AuthenticationResult result = authService.authenticate(certificate, key, AuthenticationTestConstants
            .PASSWORD_RCE_ENGINEER);
        assertEquals(X509AuthenticationResult.AUTHENTICATED, result);

    }

    /**
     * Tests authentication for failure.
     * 
     * @throws Exception
     *             if the test fails.
     * 
     */
    public void testAuthenticateForSanity() throws Exception {

        // incorrect password

        X509Certificate certificate = CertUtil.loadCertificate(getClass()
            .getResourceAsStream(AuthenticationTestConstants.USERCERT_RCE_ENGINEER_PEM));
        OpenSSLKey key = new BouncyCastleOpenSSLKey(getClass().getResourceAsStream(AuthenticationTestConstants.USERKEY_RCE_ENGINEER_PEM));

        X509AuthenticationResult result = authService
            .authenticate(certificate, key, AuthenticationTestConstants.PASSWORD_RCE_ENEMY);
        assertEquals(X509AuthenticationResult.PASSWORD_INCORRECT, result);

        // private and public key do not belong together

        certificate = CertUtil.loadCertificate(getClass().getResourceAsStream(AuthenticationTestConstants.USERCERT_RCE_ENGINEER_PEM));
        key = new BouncyCastleOpenSSLKey(getClass().getResourceAsStream(AuthenticationTestConstants.KEY_RCE_ENEMY_PEM));

        result = authService.authenticate(certificate, key, AuthenticationTestConstants.PASSWORD_RCE_ENEMY);
        assertEquals(X509AuthenticationResult.PRIVATE_KEY_NOT_BELONGS_TO_PUBLIC_KEY, result);

        // not signed by trusted CA

        certificate = CertUtil.loadCertificate(getClass().getResourceAsStream(AuthenticationTestConstants.CERT_UNKNOWN_USER_PEM));
        key = new BouncyCastleOpenSSLKey(getClass().getResourceAsStream(AuthenticationTestConstants.KEY_UNKNOWN_USER_PEM));

        result = authService.authenticate(certificate, key, AuthenticationTestConstants.PASSWORD_UNKNOWN_USER);
        assertEquals(X509AuthenticationResult.NOT_SIGNED_BY_TRUSTED_CA, result);

        // revoked

        certificate = CertUtil.loadCertificate(getClass().getResourceAsStream(AuthenticationTestConstants.CERT_RCE_ENEMY_PEM));
        key = new BouncyCastleOpenSSLKey(getClass().getResourceAsStream(AuthenticationTestConstants.KEY_RCE_ENEMY_PEM));

        result = authService.authenticate(certificate, key, AuthenticationTestConstants.PASSWORD_RCE_ENEMY);
        assertEquals(X509AuthenticationResult.CERTIFICATE_REVOKED, result);

        // no password, but encrypted key

        certificate = CertUtil.loadCertificate(getClass().getResourceAsStream(AuthenticationTestConstants.USERCERT_RCE_ENGINEER_PEM));
        key = new BouncyCastleOpenSSLKey(getClass().getResourceAsStream(AuthenticationTestConstants.USERKEY_RCE_ENGINEER_PEM));

        result = authService.authenticate(certificate, key, null);
        assertEquals(X509AuthenticationResult.PASSWORD_REQUIRED, result);

    }

    /**
     * Tests authentication for failure.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testAuthenticateForFailure() throws Exception {

        // no certificate
        try {
            OpenSSLKey key = new BouncyCastleOpenSSLKey(getClass()
                .getResourceAsStream(AuthenticationTestConstants.USERKEY_RCE_ENGINEER_PEM));
            authService.authenticate(null, key, AuthenticationTestConstants.PASSWORD_RCE_ENEMY);
            fail();

        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        // no private key
        try {
            X509Certificate certificate = CertUtil.loadCertificate(getClass()
                .getResourceAsStream(AuthenticationTestConstants.USERCERT_RCE_ENGINEER_PEM));

            authService.authenticate(certificate, null, AuthenticationTestConstants.PASSWORD_RCE_ENEMY);

            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

    }

    /**
     * Tests getting a CertificateUser for success.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testGetCertificateUserForSuccess() throws Exception {

        X509Certificate certificate = CertUtil.loadCertificate(getClass()
            .getResourceAsStream(AuthenticationTestConstants.USERCERT_RCE_ENGINEER_PEM));
        User certificateUser = authService.createUser(certificate, validityInDays);

        assertTrue(certificateUser.isValid());
    }
    
    /**
     * Tests getting an LDAPUser for success.
     * 
     * @throws Exception if the test fails.
     */
    public void testGetLdapUserForSuccess() throws Exception {
        User ldapUser = authService.createUser("testUser", validityInDays);
        assertTrue(ldapUser.isValid());
    }

    /**
     * Tests getting a proxy certificate for failure.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testGetProxyCertificateForFailure() throws Exception {

        // no certificate
        try {
            authService.createUser((X509Certificate) null, validityInDays);

            fail();

        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

    }

    /**
     * Tests getting a proxy certificate for success.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testLoadCertificateForSuccess() throws Exception {

        X509Certificate certificate = authService.loadCertificate(System.getProperty(AuthenticationTestConstants.USER_DIR)
            + AuthenticationTestConstants.TESTRESOURCES_DIR + AuthenticationTestConstants.USERCERT_RCE_ENGINEER_PEM);

        assertNotNull(certificate);

    }

    /**
     * Tests getting a proxy certificate for failure.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testLoadCertificateForFailure() throws Exception {
        try {
            authService.loadCertificate(null);

            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Tests getting a proxy certificate for success.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testLoadCertificateRevocationListsForSuccess() throws Exception {

        OpenSSLKey key = authService.loadKey(System.getProperty(AuthenticationTestConstants.USER_DIR)
            + AuthenticationTestConstants.TESTRESOURCES_DIR + AuthenticationTestConstants.USERKEY_RCE_ENGINEER_PEM);

        assertNotNull(key);

    }

    /**
     * Tests getting a proxy certificate for failure.
     * 
     * @throws Exception
     *             if the test fails.
     */
    public void testLoadCertificateRevocationListsForFailure() throws Exception {

        try {
            authService.loadKey(null);

            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
    
    /**
     * Tests arguments of password and user id for failure.
     */
    public void testLdapArgumentForFailure(){
        String uid = "";
        String password = "test";
        
        LDAPAuthenticationResult res = authService.authenticate(uid, password);
        if (res == LDAPAuthenticationResult.AUTHENTICATED){
            fail();
        }
        
    }
    
    /**
     * Tests authentication at LDAP for failure.
     */
    public void testLdapAuthenticationForFailure(){
        String uid = "_";
        String password = "";
        
        LDAPAuthenticationResult res = authService.authenticate(uid, password);
        if (res == LDAPAuthenticationResult.AUTHENTICATED){
            fail();
        }
    }
    
    /**
     * Tests authentication at LDAP for success.
     */
    public void testLdapAuthenticationForSuccess(){
        String uid = "f_rcelda";    //User is not valid any more
        String password = "1314Fre0607";
        
        // if the intra-net is not available, don't make the test
        try {
            InetAddress.getByName("intra.dlr.de");
        } catch (UnknownHostException e) {
            return;
        }
               
        assertEquals(LDAPAuthenticationResult.AUTHENTICATED, authService.authenticate(uid, password));
    }
    
    /**
     * Tests authentication at LDAP for success.
     */
    public void testCreateUser(){
        User user = authService.createUser(4);
        assertEquals(4, user.getValidityInDays());
    }
    
}
