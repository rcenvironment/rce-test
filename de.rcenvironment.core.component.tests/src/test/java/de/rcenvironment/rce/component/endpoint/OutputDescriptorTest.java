/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.endpoint;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.testutils.MockComponentStuffFactory;


/**
 * Test cases for {@link OutputDescriptor}.
 *
 * @author Doreen Seider
 */
public class OutputDescriptorTest {

    private String outputName = "raus hier";
    
    private ComponentInstanceDescriptor compInstanceDesc;
    
    /** Test. */
    @Test
    public void test() {
        compInstanceDesc = MockComponentStuffFactory.createComponentInstanceDescriptor();
        
        OutputDescriptor outputDesc = new OutputDescriptor(compInstanceDesc, outputName);

        assertEquals(outputName, outputDesc.getName());
        assertEquals(compInstanceDesc, outputDesc.getComponentInstanceDescriptor());

    }
    
}
