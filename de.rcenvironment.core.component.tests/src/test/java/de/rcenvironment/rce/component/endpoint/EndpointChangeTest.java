/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.endpoint;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Test;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;


/**
 * Test cases for {@link EndpointChange}.
 *
 * @author Doreen Seider
 */
public class EndpointChangeTest {

    /** Test. */
    @Test
    public void test() {
        EndpointChange.Type type = EndpointChange.Type.Add;
        EndpointNature epNature = EndpointNature.Input;
        String epName = "rotkaeppchen";
        String epType = String.class.getCanonicalName();
        String formerEpName = "boese wolf";
        String formerEpType = Integer.class.getCanonicalName();
        ComponentDescription compDesc = EasyMock.createNiceMock(ComponentDescription.class);
        
        EndpointChange epChange = new EndpointChange(type, epNature, epName, epType, formerEpName, formerEpType, compDesc);
        
        assertEquals(type, epChange.getType());
        assertEquals(epNature, epChange.getEndpointNature());
        assertEquals(epName, epChange.getEndpointName());
        assertEquals(epType, epChange.getEndpointType());
        assertEquals(formerEpName, epChange.getFormerEndpointName());
        assertEquals(formerEpType, epChange.getFormerEndpointType());
        assertEquals(compDesc, epChange.getComponentDescription());
    }
}
