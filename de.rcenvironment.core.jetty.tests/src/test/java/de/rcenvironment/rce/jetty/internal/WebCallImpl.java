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
 * Implemenation of {@link WebCall}.
 * 
 * @author Tobias Menden
 */
@WebService(endpointInterface = "de.rcenvironment.rce.jetty.internal.WebCall", serviceName = "WebCallTest")
public class WebCallImpl implements WebCall {
    
    private static final int ADDITION = 666;
    
    @Override
    public int call(int request) {
        return request + ADDITION;
    }
    
}
