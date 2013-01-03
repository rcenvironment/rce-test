/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.jetty.internal;

import javax.jws.WebService;

/**
 * Dummy Web service interface for tests.
 * 
 * @author Tobias Menden
 */
@WebService
public interface WebCall {

    /**
     * Method of the Web service.
     * 
     * @param request The request parameter.
     * @return response The response parameter.
     */
    int call(int request);
}
