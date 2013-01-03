/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.jetty.internal;

import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;

/**
 * Unit test for {@link JettyServiceImpl}.
 * 
 * @author Tobias Menden
*/ 
public class JettyServiceImplTest extends TestCase {


    private static final String ADDRESS = "http://localhost:6666/WebCallTest";
    
    private static final int REQUEST = 334;
    
    private static final int RESULT = 1000;
    
    private static JettyServiceImpl jettyService;
      
    @Override
    public void setUp() throws Exception {
        jettyService = new JettyServiceImpl();
        WebCallImpl serverInstance = new WebCallImpl();
        jettyService.deployWebService(serverInstance, ADDRESS);
    }
    
    @Override
    public void tearDown() throws Exception {
        jettyService.undeployWebService(ADDRESS);
    }

    /**
     * Test method for 'de.rcenvironment.rce.communication.jetty.internal.JettyServiceImpl.undeployJetty()' for
     * success.
    */ 
    public void testUndeployWebServiceForSuccess() {
        jettyService.undeployWebService(ADDRESS);
        WebCall testService = (WebCall) jettyService.createWebServiceClient(WebCall.class, ADDRESS);
        try {
            testService.call(REQUEST);
            fail();
        } catch (WebServiceException e) {
            assertNotNull(e);
        }
    }
    
    /**
     * Test method for 'de.rcenvironment.rce.communication.jetty.internal.JettyServiceImpl.getWebServiceClient' for
     * sanity.
    */ 
    public void testCreateWebServiceClientForSanity() {
        WebCall testService = (WebCall) jettyService.createWebServiceClient(WebCall.class, ADDRESS);
        assertEquals(RESULT, testService.call(REQUEST));
    }
    
    /**
     * Test method for 'de.rcenvironment.rce.communication.jetty.internal.WebCallImpl' for
     * success.
     */
    public void testCallForSucess() {
        WebCallImpl testCall = new WebCallImpl();
        int newResult = testCall.call(REQUEST);
        assertEquals(RESULT, newResult);
    }

}

