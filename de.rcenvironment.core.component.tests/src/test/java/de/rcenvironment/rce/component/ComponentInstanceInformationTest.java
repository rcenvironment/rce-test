/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.endpoint.Output;
import de.rcenvironment.rce.component.endpoint.OutputDescriptor;
import de.rcenvironment.rce.component.testutils.MockComponentStuffFactory;
import de.rcenvironment.rce.notification.DistributedNotificationService;

/**
 * Test cases for {@link ComponentInstanceInformation}.
 *
 * @author Doreen Seider
 */
public class ComponentInstanceInformationTest extends TestCase {
    
    private ComponentDescription cd;
    
    private ComponentContext compCtx;
    
    private User cert;
    
    private final String ciId = "eierwaermer";
    
    private final String ciName = "schwangere auster";
    
    private final String ciWorkDir = "telespargel";
    
    private final boolean inputConnected = true;
    
    private Set<Output> outputs;
    
    private final String outputName = "kommode";
    
    private final String configKey = "nuttenbrosche";
    
    private final String inputName = "alex";
    
    private final String inputMetaDataKey = "st. walter";
    
    private final String outputMetaDataKey = "rache des papstes";

    /** Set up. */
    @Before
    public void setUp() {
                
        cd = MockComponentStuffFactory.createComponentDescription();
        cd.setInputMetaData(inputName, inputMetaDataKey, "schrippe");
        cd.setOutputMetaData(outputName, outputMetaDataKey, "broiler");
        
        compCtx = new DummyComponentContext();
        cert = EasyMock.createNiceMock(User.class);
        
        outputs = new HashSet<Output>();
        ComponentInstanceDescriptor compInstanceDesc = MockComponentStuffFactory.createComponentInstanceDescriptor();
        OutputDescriptor outputDesc = new OutputDescriptor(compInstanceDesc, outputName);
        Output output = new Output(outputDesc, String.class, EasyMock.createNiceMock(DistributedNotificationService.class));
        outputs.add(output);
    }
    
    /** Test. */
    @Test
    public void testDelegateStuff() {
        ComponentInstanceInformation ci = new ComponentInstanceInformation(ciId, ciName, ciWorkDir,
            cd, compCtx, cert, inputConnected, outputs);
        
        assertEquals(ciId, ci.getIdentifier());
        assertEquals(ciName, ci.getName());
        assertEquals(ciWorkDir, ci.getWorkingDirectory());
        assertEquals(cert, ci.getProxyCertificate());
        assertTrue(ci.isInputConnected());
        
        assertEquals(cd.getIdentifier(), ci.getComponentIdentifier());
        assertEquals(cd.getName(), ci.getComponentName());
        assertEquals(cd.getPlatform(), ci.getPlatform());
        assertEquals(cd.getInputDefinitions(), ci.getInputDefinitions());
        assertEquals(cd.getOutputDefinitions(), ci.getOutputDefinitions());
        assertEquals(cd.getConfigurationDefinitions(), ci.getConfigurationDefinitions());
        assertEquals(cd.getConfiguration().get(configKey), ci.getConfigurationValue(configKey));
        assertEquals(cd.getInputMetaData(inputName), ci.getInputMetaData(inputName));
        assertEquals(cd.getOutputMetaData(outputName), ci.getOutputMetaData(outputName));
        
        assertEquals(compCtx.getIdentifier(), ci.getComponentContextIdentifier());
        assertEquals(compCtx.getName(), ci.getComponentContextName());
        assertEquals(compCtx.getControllerPlatform(), ci.getComponentContextControllerPlatform());
        assertEquals(compCtx.getInvolvedPlatforms(), ci.getPlatformsInvolvedInComponentContext());
        
        assertNotNull(ci.getOutput(outputName));
        assertNull(ci.getOutput("jwd"));
    }
    
    /**
     * Test implementation of {@link ComponentContext}.
     * @author Doreen Seider
     */
    public class DummyComponentContext implements ComponentContext {

        @Override
        public String getIdentifier() {
            return "waschmaschine";
        }

        @Override
        public String getName() {
            return "suppenschuessel";
        }

        @Override
        public PlatformIdentifier getControllerPlatform() {
            return PlatformIdentifierFactory.fromHostAndNumberString("wasserklops:1");
        }
        
        @Override
        public PlatformIdentifier getDefaultStoragePlatform() {
            return getControllerPlatform();
        }

        @Override
        public Set<PlatformIdentifier> getInvolvedPlatforms() {
            Set<PlatformIdentifier> pis = new HashSet<PlatformIdentifier>();
            pis.add(PlatformIdentifierFactory.fromHostAndNumberString("retourkutsche:1814"));
            pis.add(PlatformIdentifierFactory.fromHostAndNumberString("hungerharke:1969"));
            return pis;
        }
        
    }
}
