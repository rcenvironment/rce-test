/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;
import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.Registerable;
import de.rcenvironment.rce.component.testutils.MockComponentStuffFactory;
import de.rcenvironment.rce.configuration.testutils.MockConfigurationService;
import de.rcenvironment.rce.notification.DistributedNotificationService;

/**
 * Test cases for {@link ComponentRegistryImpl}.
 * 
 * @author Doreen Seider
 * @author Jens Ruehmkorf
 */
public class ComponentRegistryImplTest {

    private static final String BUNDLE_SYMBOLIC_NAME = "de.rce.comp.id";
    
    private static final String SRC_TEST_RESOURCES_SUPER_DIR = "src/test/resources/superDir";

    private final String compInstId = "wilde ID";

    private final PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("host:9");

    private final de.rcenvironment.rce.component.ComponentContext ctx = EasyMock
        .createNiceMock(de.rcenvironment.rce.component.ComponentContext.class);

    private ComponentRegistryImpl registry;

    private User user;

    private User anotherProxyCertificate;

    private User invalidProxyCertificate;

    private ComponentDescription compDescription;

    private ComponentContext componentContextMock;

    private ServiceReference componentControllerServiceReferenceMock;

    private ServiceReference componentServiceReferenceMock;

    private ComponentFactory componentFactoryMock = EasyMock.createNiceMock(ComponentFactory.class);

    private ComponentFactory componentControllerFactoryMock = EasyMock.createNiceMock(ComponentFactory.class);

    private ComponentInstanceDescriptor compInstDesc = EasyMock.createNiceMock(ComponentInstanceDescriptor.class);

    /** Set up. */
    @Before
    public void setUp() {
        registry = new ComponentRegistryImpl();

        compDescription = MockComponentStuffFactory.createComponentDescription();
        createComponentServiceReferenceMock();
        createComponentControllerServiceReferenceMock();
        createComponentContextMock();
        createUserMock();
        createAnotherUserMock();
        createInvalidUserMock();

        registry.addComponent(componentServiceReferenceMock);
        registry.bindConfigurationService(new DummyConfigurationService());
        registry.bindPlatformService(new DummyPlatformService());
        registry.bindComponentControllerFactory(componentControllerServiceReferenceMock);
        registry.bindDistributedNotificationService(EasyMock.createNiceMock(DistributedNotificationService.class));
        registry.activate(componentContextMock);
        final int delay = 100;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            fail();
        }
        registry.addComponent(componentServiceReferenceMock);

    }

    /** Tear down. */
    @After
    public void tearDown() {
        registry.bindConfigurationService(null);
        registry.bindPlatformService(null);
        registry.removeComponent(EasyMock.createNiceMock(ComponentFactory.class));
        File tmpDir = new File(SRC_TEST_RESOURCES_SUPER_DIR);
        if (tmpDir.exists()) {
            delete(tmpDir);
        }
    }

    private void delete(File file) {
        for (File f : file.listFiles()) {
            delete(f);
        }
        file.delete();
    }

    /** Test. */
    @Test
    public void testGetDescriptions() {

        Collection<ComponentDescription> components = registry.getComponentDescriptions(user);

        checkComponentSet(components);
        
        // invalid certificate
        try {
            registry.getComponentDescriptions(invalidProxyCertificate);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        
        Collection<ComponentDescription> filteredComponents = registry.getComponentDescriptions(user, pi);
        checkComponentSet(filteredComponents);
        
        filteredComponents = registry.getComponentDescriptions(user, PlatformIdentifierFactory.fromHostAndNumberString("hosty:19"));
        assertSame(0, filteredComponents.size());
    }
    
    private void checkComponentSet(Collection<ComponentDescription> components) {
        assertEquals(1, components.size());
        for (ComponentDescription desc : components) {
            assertEquals(MockComponentStuffFactory.COMPONENT_IDENTIFIER, desc.getIdentifier());
            assertEquals(MockComponentStuffFactory.COMPONENT_NAME, desc.getName());
            assertEquals(pi, desc.getPlatform());
            assertNotSame(compDescription, desc);
        }
    }

    /** Test. */
    @Test
    public void testGetDescription() {
        ComponentDescription cd = registry.getComponentDescription(user, MockComponentStuffFactory.COMPONENT_IDENTIFIER);
        assertNotNull(cd);
        assertTrue(cd.getIdentifier().equals(MockComponentStuffFactory.COMPONENT_IDENTIFIER));
        assertTrue(cd.getName().equals(MockComponentStuffFactory.COMPONENT_NAME));
        assertTrue(cd.getPlatform().equals(pi));
        assertNull(cd.getIcon16());
        assertNull(cd.getIcon32());

        // invalid certificate
        try {
            registry.getComponentDescription(invalidProxyCertificate, MockComponentStuffFactory.COMPONENT_IDENTIFIER);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Test.
     * 
     * @throws ComponentException if an error occurs.
     **/
    @Test
    public void testCreateInstance() throws ComponentException {

        ComponentInstanceDescriptor cid = registry.createComponentInstance(user, compDescription,
            ctx, "Charles M. Schulz", false);

        assertNotNull(cid);
        assertEquals(compInstId, cid.getIdentifier());

        // Component not exists
        ComponentDescription unknownCD = EasyMock.createNiceMock(ComponentDescription.class);
        EasyMock.expect(unknownCD.getIdentifier()).andReturn("blubb").anyTimes();
        EasyMock.replay(unknownCD);
        try {
            cid = registry.createComponentInstance(user, unknownCD, ctx, "Peanuts", true);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        // invalid certificate
        try {
            registry.createComponentInstance(invalidProxyCertificate, compDescription, ctx, "Woodstock", false);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        registry.disposeComponentInstance(user, cid.getIdentifier());

    }

    /**
     * Test.
     * 
     * @throws ComponentException if an error occurs.
     **/
    @Test
    public void testDisposeInstance() throws ComponentException {

        // tests with instance identifier:
        ComponentInstanceDescriptor c = registry.createComponentInstance(user, compDescription, ctx, "Marcie", false);
        String id = c.getIdentifier();

        // invalid certificate
        try {
            registry.disposeComponentInstance(invalidProxyCertificate, id);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        registry.disposeComponentInstance(user, id);
        try {
            registry.disposeComponentInstance(user, id);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        // NoRights
        try {
            c = registry.createComponentInstance(user, compDescription, ctx, "Pig Pen", true);
            registry.disposeComponentInstance(anotherProxyCertificate, c.getIdentifier());
            fail();
        } catch (AuthorizationException e) {
            assertTrue(true);
        }

        registry.disposeComponentInstance(user, c.getIdentifier());

        // tests with ComponentInformation:
        c = registry.createComponentInstance(user, compDescription, ctx, "Patty", true);

    }

    /**
     * Test.
     * 
     * @throws ComponentException if an error occurs.
     **/
    @Test
    public void testGetInstanceInformation() throws ComponentException {

        ComponentInstanceDescriptor ci = registry.createComponentInstance(user, compDescription, ctx, "in", true);
        assertEquals(ci, registry.getComponentInstanceDescriptor(user, ci.getIdentifier()));

        // no rights
        try {
            registry.getComponentInstanceDescriptor(anotherProxyCertificate, ci.getIdentifier());
            fail();
        } catch (AuthorizationException e) {
            assertTrue(true);
        }
        // invalid certificate
        try {
            registry.getComponentInstanceDescriptor(invalidProxyCertificate, ci.getIdentifier());
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        registry.disposeComponentInstance(user, ci.getIdentifier());
    }

    /** Test. */
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testActivate() {

        // RCE-Component bundles are installed
        Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn("");
        Dictionary dict = new Hashtable();
        dict.put(ComponentConstants.MANIFEST_ENTRY, Boolean.valueOf(true).toString());
        EasyMock.expect(bundleMock.getHeaders()).andReturn(dict).anyTimes();
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn("");
        EasyMock.replay(bundleMock);

        BundleContext bundleContextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(bundleContextMock.getBundle()).andReturn(bundleMock).anyTimes();
        EasyMock.expect(bundleContextMock.getBundles()).andReturn(new Bundle[] { bundleMock, bundleMock }).anyTimes();
        EasyMock.replay(bundleContextMock);

        ComponentContext anotherComponentContextMock = EasyMock.createNiceMock(ComponentContext.class);
        EasyMock.expect(anotherComponentContextMock.getBundleContext()).andReturn(bundleContextMock).anyTimes();
        EasyMock.replay(anotherComponentContextMock);

        registry.activate(anotherComponentContextMock);

        // non-RCE-Component bundle is installed
        // strict mock in order to throw an exception if bundle.start() will be called,
        // which would be incorrect
        bundleMock = EasyMock.createStrictMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn("");
        dict = new Hashtable();
        dict.put(ComponentConstants.MANIFEST_ENTRY, Boolean.valueOf(false).toString());
        EasyMock.expect(bundleMock.getHeaders()).andReturn(dict).anyTimes();
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn("");
        EasyMock.replay(bundleMock);

        bundleContextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(bundleContextMock.getBundle()).andReturn(bundleMock).anyTimes();
        EasyMock.expect(bundleContextMock.getBundles()).andReturn(new Bundle[] { bundleMock, bundleMock }).anyTimes();
        EasyMock.replay(bundleContextMock);

        anotherComponentContextMock = EasyMock.createNiceMock(ComponentContext.class);
        EasyMock.expect(anotherComponentContextMock.getBundleContext()).andReturn(bundleContextMock).anyTimes();
        EasyMock.replay(anotherComponentContextMock);

        registry.activate(anotherComponentContextMock);

        // non-RCE-Component bundle is installed
        // strict mock in order to throw an exception if bundle.start() will be called,
        // which would be incorrect
        bundleMock = EasyMock.createStrictMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn("");
        dict = new Hashtable();
        EasyMock.expect(bundleMock.getHeaders()).andReturn(dict).anyTimes();
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn("");
        EasyMock.replay(bundleMock);

        bundleContextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(bundleContextMock.getBundle()).andReturn(bundleMock).anyTimes();
        EasyMock.expect(bundleContextMock.getBundles()).andReturn(new Bundle[] { bundleMock, bundleMock }).anyTimes();
        EasyMock.replay(bundleContextMock);

        anotherComponentContextMock = EasyMock.createNiceMock(ComponentContext.class);
        EasyMock.expect(anotherComponentContextMock.getBundleContext()).andReturn(bundleContextMock).anyTimes();
        EasyMock.replay(anotherComponentContextMock);

        registry.activate(anotherComponentContextMock);
    }

    /**
     * Test.
     * 
     * @throws ComponentException if an error occurs.
     **/
    @Test
    public void testIsCreator() throws ComponentException {
        ComponentInstanceDescriptor c = registry.createComponentInstance(user, compDescription, ctx, "life", true);
        assertTrue(registry.isCreator(c.getIdentifier(), user));
        assertFalse(registry.isCreator(c.getIdentifier(), anotherProxyCertificate));

        // invalid certificate
        try {
            registry.isCreator(c.getIdentifier(), invalidProxyCertificate);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    private void createUserMock() {
        user = EasyMock.createNiceMock(User.class);
        EasyMock.expect(user.isValid()).andReturn(true).anyTimes();
        EasyMock.expect(user.same(user)).andReturn(true).anyTimes();
        EasyMock.replay(user);
    }

    private void createInvalidUserMock() {
        invalidProxyCertificate = EasyMock.createNiceMock(User.class);
        EasyMock.expect(invalidProxyCertificate.isValid()).andReturn(false).anyTimes();
        EasyMock.replay(invalidProxyCertificate);
    }

    private void createAnotherUserMock() {
        anotherProxyCertificate = EasyMock.createNiceMock(User.class);
        EasyMock.expect(anotherProxyCertificate.isValid()).andReturn(true).anyTimes();
        EasyMock.replay(anotherProxyCertificate);
    }

    @SuppressWarnings("rawtypes")
    private void createComponentContextMock() {

        String propertyID = "de.rcenvironment.rce.properties.test";
        String propertyValue = "value";
        String icon16 = "file:///1";
        String icon32 = "file:///2";

        componentContextMock = EasyMock.createNiceMock(ComponentContext.class);
        BundleContext bundleContextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(bundleContextMock.getBundles()).andReturn(new Bundle[] {}).anyTimes();

        ComponentInstance componentInstanceMock = EasyMock.createNiceMock(ComponentInstance.class);
        EasyMock.expect(componentInstanceMock.getInstance()).andReturn(new DummyComponent()).anyTimes();

        EasyMock.expect(componentFactoryMock.newInstance((Dictionary) EasyMock.anyObject())).andReturn(componentInstanceMock).anyTimes();

        ComponentInstance componentControllerInstanceMock = EasyMock.createNiceMock(ComponentInstance.class);
        EasyMock.expect(componentControllerInstanceMock.getInstance()).andReturn(new DummyComponentController()).anyTimes();

        EasyMock.expect(componentControllerFactoryMock.newInstance((Dictionary) EasyMock.anyObject()))
            .andReturn(componentControllerInstanceMock).anyTimes();

        ServiceReference serviceReferenceMock = EasyMock.createNiceMock(ServiceReference.class);
        EasyMock.expect(serviceReferenceMock.getPropertyKeys()).andReturn(
            new String[] { propertyID, ComponentConstants.INPUTS_DEF_KEY, ComponentConstants.OUTPUTS_DEF_KEY }).anyTimes();
        EasyMock.expect(serviceReferenceMock.getProperty(propertyID)).andReturn(propertyValue).anyTimes();
        EasyMock.expect(serviceReferenceMock.getProperty(ComponentConstants.COMPONENT_CLASS_KEY))
            .andReturn(MockComponentStuffFactory.COMPONENT_CLASS).anyTimes();
        EasyMock.expect(serviceReferenceMock.getProperty(ComponentConstants.COMPONENT_NAME_KEY))
            .andReturn(MockComponentStuffFactory.COMPONENT_NAME).anyTimes();
        EasyMock.expect(serviceReferenceMock.getProperty(ComponentConstants.ICON_16_KEY))
            .andReturn(icon16).anyTimes();
        EasyMock.expect(serviceReferenceMock.getProperty(ComponentConstants.ICON_32_KEY))
            .andReturn(icon32).anyTimes();

        EasyMock.expect(serviceReferenceMock.getProperty(ComponentConstants.INPUTS_DEF_KEY))
            .andReturn(new String[] { "var1:java.lang.String" + ComponentConstants.METADATA_SEPARATOR + "usage=required" }).anyTimes();

        EasyMock.expect(serviceReferenceMock.getProperty(ComponentConstants.OUTPUTS_DEF_KEY))
            .andReturn(new String[] { "var2:java.lang.String" + ComponentConstants.METADATA_SEPARATOR + "test=tada" }).anyTimes();

        Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock.getResource(icon16)).andReturn(null).anyTimes();
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME).anyTimes();
        EasyMock.expect(bundleMock.getResource(icon32)).andReturn(null).anyTimes();
        EasyMock.expect(serviceReferenceMock.getBundle()).andReturn(bundleMock).anyTimes();
        EasyMock.expect(bundleContextMock.getBundle()).andReturn(bundleMock).anyTimes();

        try {
            EasyMock.expect(bundleContextMock.getService(componentServiceReferenceMock)).andReturn(componentFactoryMock).anyTimes();
            EasyMock.expect(bundleContextMock.getService(componentControllerServiceReferenceMock))
                .andReturn(componentControllerFactoryMock).anyTimes();
            EasyMock.expect(
                bundleContextMock.getAllServiceReferences(EasyMock.eq(Registerable.class.getName()), (String) EasyMock.anyObject()))
                .andReturn(new ServiceReference[] { serviceReferenceMock }).anyTimes();

        } catch (InvalidSyntaxException e) {
            // can not happen
            @SuppressWarnings("unused") int i = 4;
        }

        EasyMock.expect(componentContextMock.getBundleContext()).andReturn(bundleContextMock).anyTimes();

        EasyMock.replay(bundleMock);
        EasyMock.replay(componentInstanceMock);
        EasyMock.replay(componentControllerInstanceMock);
        EasyMock.replay(componentFactoryMock);
        EasyMock.replay(componentControllerFactoryMock);
        EasyMock.replay(serviceReferenceMock);
        EasyMock.replay(bundleContextMock);
        EasyMock.replay(componentContextMock);
    }

    private void createComponentServiceReferenceMock() {
        componentServiceReferenceMock = EasyMock.createNiceMock(ServiceReference.class);
        EasyMock.expect(componentServiceReferenceMock.getProperty(ComponentConstants.COMPONENT_NAME_KEY))
            .andReturn(MockComponentStuffFactory.COMPONENT_NAME).anyTimes();
        EasyMock.expect(componentServiceReferenceMock.getProperty(ComponentConstants.COMPONENT_CLASS_KEY))
            .andReturn(MockComponentStuffFactory.COMPONENT_CLASS).anyTimes();

        EasyMock.replay(componentServiceReferenceMock);
    }

    private void createComponentControllerServiceReferenceMock() {
        componentControllerServiceReferenceMock = EasyMock.createNiceMock(ServiceReference.class);
    }

    /**
     * Test implementation of {@link ConfigurationService}.
     * 
     * @author Doreen Seider
     */
    private class DummyConfigurationService extends MockConfigurationService.ThrowExceptionByDefault {

        @Override
        public String getPlatformTempDir() {
            return SRC_TEST_RESOURCES_SUPER_DIR;
        }
        
        @Override
        public <T> T getConfiguration(String identifier, Class<T> clazz) {
            if (identifier.equals(BUNDLE_SYMBOLIC_NAME) && clazz.equals(ComponentBundleConfiguration.class)) {
                return (T) new ComponentBundleConfiguration();                
            }
            return null;
        }

    }

    /**
     * Test implementation of {@link PlatformService}.
     * 
     * @author Doreen Seider
     */
    private class DummyPlatformService extends PlatformServiceDefaultStub {

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return pi;
        }
    }

    /**
     * Dummy implementation of {@link ComponentController}.
     * 
     * @author Doreen Seider
     */
    public class DummyComponentController extends ComponentControllerImpl {

        private static final long serialVersionUID = -3972713275491942747L;

        @Override
        public ComponentInstanceDescriptor initialize(User newCert, String controllerId, String compClazz, String compName,
            ComponentDescription compDesc, de.rcenvironment.rce.component.ComponentContext compCtx, boolean inputConnected)
            throws ComponentException {
            EasyMock.reset(compInstDesc);
            EasyMock.expect(compInstDesc.getIdentifier()).andReturn(compInstId).anyTimes();
            EasyMock.expect(compInstDesc.getWorkingDirectory()).andReturn("src/test/resources/compDir").anyTimes();
            EasyMock.replay(compInstDesc);
            return compInstDesc;
        }

    }

}
