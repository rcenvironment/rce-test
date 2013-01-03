/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.callback.internal;

/**
 * Test callback object.
 * 
 * @author Doreen Seider
 */
public class DummyObject implements DummyInterface {

    private static final long serialVersionUID = 1L;

    @Override
    public String method() {
        return "method called";
    }

    @Override
    public void callbackMethod() {
        throw new RuntimeException("callbackMethod called");
    }

    @Override
    public Class<?> getInterface() {
        return DummyInterface.class;
    }

}
