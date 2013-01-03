/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.communication.testutils.PlatformServiceDefaultStub;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.testutils.MockDistributedComponentRegistry;

/**
 * Test cases for {@link WorkflowDescriptionPersistenceHandler}.
 * 
 * @author Doreen Seider
 */
public class WorkflowDescriptionPersistenceHandlerTest {


    /**
     * Writes a {@link ComponentDescription} and reads the same one and compares both.
     * 
     * @throws Exception if an error during writing or reading occurs.
     */
    @Test
    public void testWriteAndReadWorkflowDescription() throws Exception {
        WorkflowDescription wdToFile = WorkflowTestUtils.createWorkflowDescription();
        WorkflowDescriptionPersistenceHandler handler = new WorkflowDescriptionPersistenceHandler();
        ByteArrayOutputStream bos = handler.writeWorkflowDescriptionToStream(wdToFile);

        handler = new WorkflowDescriptionPersistenceHandler();
        handler.bindDistributedComponentRegistry(new DummyDistributedComponentRegistry());
        handler.bindPlatformService(new DummyPlatformService());
        WorkflowDescription wdFromFile = handler.readWorkflowDescriptionFromStream(new ByteArrayInputStream(bos.toByteArray()),
                EasyMock.createNiceMock(User.class));

        // compare both descriptions

        assertEquals(wdToFile.getIdentifier(), wdFromFile.getIdentifier());
        assertEquals(wdToFile.getName(), wdFromFile.getName());
        assertEquals(wdToFile.getTargetPlatform(), wdFromFile.getTargetPlatform());
        assertEquals(wdToFile.getWorkflowNodes().size(), wdFromFile.getWorkflowNodes().size());
        assertEquals(wdToFile.getConnections().size(), wdFromFile.getConnections().size());

        int i = 0;
        for (WorkflowNode nodeToFile : wdToFile.getWorkflowNodes()) {
            for (WorkflowNode nodeFromFile : wdToFile.getWorkflowNodes()) {
                if (nodeToFile.getIdentifier().equals(nodeFromFile.getIdentifier())) {
                    i++;
                    assertEquals(nodeToFile.getComponentDescription().getIdentifier(),
                            nodeFromFile.getComponentDescription().getIdentifier());
                    assertEquals(nodeToFile.getProperty(WorkflowTestUtils.KEY), 
                            nodeFromFile.getProperty(WorkflowTestUtils.KEY));
                    assertEquals(nodeToFile.getComponentDescription().getConfiguration().size(),
                            nodeFromFile.getComponentDescription().getConfiguration().size());
                    assertEquals(nodeToFile.getName(), nodeFromFile.getName());
                    assertEquals(nodeToFile.getX(), nodeFromFile.getX());
                    assertEquals(nodeToFile.getY(), nodeFromFile.getY());

                    if (nodeFromFile.getName().equals(WorkflowTestUtils.NODE2_NAME)) {
                        assertEquals(nodeToFile.getComponentDescription().
                                getConfiguration(WorkflowTestUtils.PROP_MAP_ID).size(),
                                nodeFromFile.getComponentDescription().
                                getConfiguration(WorkflowTestUtils.PROP_MAP_ID).size());
                        nodeFromFile.setPropertyMapId(WorkflowTestUtils.PROP_MAP_ID);
                        nodeToFile.setPropertyMapId(WorkflowTestUtils.PROP_MAP_ID);
                        assertEquals(nodeToFile.getProperty(WorkflowTestUtils.KEY), 
                                nodeFromFile.getProperty(WorkflowTestUtils.KEY));
                    }
                }
            }
        }
        assertEquals(2, i);

        i = 0;
        for (Connection connectionToFile : wdToFile.getConnections()) {
            for (Connection connectionFromFile : wdFromFile.getConnections()) {
                if (connectionToFile.getInput().equals(connectionFromFile.getInput())
                        && connectionToFile.getOutput().equals(connectionFromFile.getOutput())
                        && connectionToFile.getSource().getIdentifier().equals(connectionFromFile.getSource().getIdentifier())
                        && connectionToFile.getTarget().getIdentifier().equals(connectionFromFile.getTarget().getIdentifier())) {
                    i++;

                }
            }
        }
        assertEquals(2, i);
    }

    /**
     * Writes a {@link ComponentDescription} and reads the same one and compares both.
     * 
     * @throws Exception if an error during writing or reading occurs.
     */
    @Test
    public void testWriteAndReadWorkflowDescriptionUsingCustomStream() throws Exception {
        WorkflowDescription wdToFile = WorkflowTestUtils.createWorkflowDescription();
        WorkflowDescriptionPersistenceHandler handler = new WorkflowDescriptionPersistenceHandler();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        handler.writeWorkflowDescriptionToStream(wdToFile, bos);

        handler = new WorkflowDescriptionPersistenceHandler();
        handler.bindDistributedComponentRegistry(new DummyDistributedComponentRegistry());
        handler.bindPlatformService(new DummyPlatformService());
        WorkflowDescription wdFromFile = handler.readWorkflowDescriptionFromStream(new ByteArrayInputStream(bos.toByteArray()),
                EasyMock.createNiceMock(User.class));

        // check number

        assertEquals(1, handler.readWorkflowVersionNumer(new ByteArrayInputStream(bos.toByteArray())));

        // compare both descriptions

        assertEquals(wdToFile.getIdentifier(), wdFromFile.getIdentifier());
        assertEquals(wdToFile.getName(), wdFromFile.getName());
        assertEquals(wdToFile.getTargetPlatform(), wdFromFile.getTargetPlatform());
        assertEquals(wdToFile.getWorkflowNodes().size(), wdFromFile.getWorkflowNodes().size());
        assertEquals(wdToFile.getConnections().size(), wdFromFile.getConnections().size());

        int i = 0;
        for (WorkflowNode nodeToFile : wdToFile.getWorkflowNodes()) {
            for (WorkflowNode nodeFromFile : wdToFile.getWorkflowNodes()) {
                if (nodeToFile.getIdentifier().equals(nodeFromFile.getIdentifier())) {
                    i++;
                    assertEquals(nodeToFile.getComponentDescription().getIdentifier(),
                            nodeFromFile.getComponentDescription().getIdentifier());
                    assertEquals(nodeToFile.getProperty(WorkflowTestUtils.KEY), 
                            nodeFromFile.getProperty(WorkflowTestUtils.KEY));
                    assertEquals(nodeToFile.getComponentDescription().getConfiguration().size(),
                            nodeFromFile.getComponentDescription().getConfiguration().size());
                    assertEquals(nodeToFile.getName(), nodeFromFile.getName());
                    assertEquals(nodeToFile.getX(), nodeFromFile.getX());
                    assertEquals(nodeToFile.getY(), nodeFromFile.getY());
                }
            }
        }
        assertEquals(2, i);

        i = 0;
        for (Connection connectionToFile : wdToFile.getConnections()) {
            for (Connection connectionFromFile : wdFromFile.getConnections()) {
                if (connectionToFile.getInput().equals(connectionFromFile.getInput())
                        && connectionToFile.getOutput().equals(connectionFromFile.getOutput())
                        && connectionToFile.getSource().getIdentifier().equals(connectionFromFile.getSource().getIdentifier())
                        && connectionToFile.getTarget().getIdentifier().equals(connectionFromFile.getTarget().getIdentifier())) {
                    i++;

                }
            }
        }
        assertEquals(2, i);
    }

    /**
     * Test.
     * 
     * @throws Exception if an error during reading occurs.
     */
    @Test
    public void testReadWorkflowVersionUsingCustomStream() throws Exception {
        WorkflowDescription wdToFile = WorkflowTestUtils.createWorkflowDescription();
        WorkflowDescriptionPersistenceHandler handler = new WorkflowDescriptionPersistenceHandler();

        final int defaultVersion = 1;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        handler.writeWorkflowDescriptionToStream(wdToFile, bos);
        assertEquals(defaultVersion, handler.readWorkflowVersionNumer(new ByteArrayInputStream(bos.toByteArray())));

        final int version = 3;
        wdToFile.setWorkflowVersion(version);
        bos = new ByteArrayOutputStream();
        handler.writeWorkflowDescriptionToStream(wdToFile, bos);
        assertEquals(version, handler.readWorkflowVersionNumer(new ByteArrayInputStream(bos.toByteArray())));
    }

    //    private WorkflowDescription createWorkflowDescription() throws Exception {
    //        WorkflowDescription wd = new WorkflowDescription("Charles M.");
    //        wd.setName("Schultz");
    //        wd.setTargetPlatform(PlatformIdentifierFactory.fromHostAndNumber("snoopy", 7));
    //
    //        String version = "2.0";
    //        Map<String, Class<? extends Serializable>> inputDefs = new HashMap<String, Class<? extends Serializable>>();
    //        Map<String, Class<? extends Serializable>> outputDefs = new HashMap<String, Class<? extends Serializable>>();
    //        outputDefs.put("output11", String.class);
    //        outputDefs.put("output12", String.class);
    //        Map<String, Class<? extends Serializable>> configDefs = new HashMap<String, Class<? extends Serializable>>();
    //        configDefs.put(WorkflowTestUtils.KEY, String.class);
    //        configDefs.put("key2", Boolean.class);
    //        configDefs.put("key3", Integer.class);
    //        Map<String, Serializable> defaultConfig = new HashMap<String, Serializable>();
    //        defaultConfig.put(WorkflowTestUtils.KEY, new String("aha"));
    //        defaultConfig.put("key3", new Integer(1));
    //
    //        byte[] icon16 = new byte[10];
    //        byte[] icon32 = new byte[0];
    //
    //        DeclarativeComponentDescription declarativeCD1 = new DeclarativeComponentDescription(
    //                WorkflowTestUtils.CD_IDENTIFIER, "Component One",
    //            "group 13", version, inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);
    //
    //        ComponentDescription cd1 = new ComponentDescription(declarativeCD1);
    //        cd1.setPlatform(PlatformIdentifierFactory.fromHostAndNumberString("linus:3"));
    //
    //        WorkflowNode node1 = new WorkflowNode(cd1);
    //        node1.setLocation(5, 4);
    //        node1.setName("Krasse Component");
    //        wd.addWorkflowNode(node1);
    //
    //        version = "7.0";
    //        inputDefs = new HashMap<String, Class<? extends Serializable>>();
    //        outputDefs.put("input21", String.class);
    //        outputDefs.put("input22", String.class);
    //        outputDefs = new HashMap<String, Class<? extends Serializable>>();
    //        configDefs = new HashMap<String, Class<? extends Serializable>>();
    //        configDefs.put(KEY, Boolean.class);
    //        configDefs.put("2key", String.class);
    //        defaultConfig = new HashMap<String, Serializable>();
    //        defaultConfig.put(KEY, Boolean.valueOf(true));
    //
    //        DeclarativeComponentDescription declarativeCD2 = new DeclarativeComponentDescription(CD_IDENTIFIER, "Component One",
    //            "group 13", version, inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);
    //
    //        ComponentDescription cd2 = new ComponentDescription(declarativeCD2);
    //        cd2.setPlatform(PlatformIdentifierFactory.fromHostAndNumberString("woodstock:3"));
    //        cd2.addInput(INPUT_NAME, "java.lang.Integer");
    //        cd2.addInput("hm", "java.lang.String");
    //        cd2.addOutput(OUTPUT_NAME, "java.lang.String");
    //        cd2.setInputMetaData(INPUT_NAME, "zahl", "true");
    //        cd2.setInputMetaData(INPUT_NAME, "primzahl", "false");
    //        cd2.setOutputMetaData(OUTPUT_NAME, "wissen", "true");
    //
    //        Map<String, Serializable> config = new HashMap<String, Serializable>();
    //        config.put(KEY, Boolean.valueOf(false));
    //        cd2.addConfiguration(PROP_MAP_ID, config);
    //
    //        WorkflowNode node2 = new WorkflowNode(cd2);
    //        node2.setLocation(8, 1);
    //        node2.setName(NODE2_NAME);
    //
    //        node2.setPropertyMapId(PROP_MAP_ID);
    //        wd.addWorkflowNode(node2);
    //
    //        Connection connection1 = new Connection(node1, "output11", node2, "input21");
    //        wd.addConnection(connection1);
    //
    //        Connection connection2 = new Connection(node1, "output12", node2, "input22");
    //        wd.addConnection(connection2);
    //
    //        return wd;
    //    }

    /**
     * Test implementation of {@link DistributedComponentRegistry}.
     * 
     * @author Doreen Seider
     */
    protected class DummyDistributedComponentRegistry extends MockDistributedComponentRegistry {

        @SuppressWarnings("serial")
        @Override
        public List<ComponentDescription> getAllComponentDescriptions(User certificate, boolean forceRefresh) {

            if (!forceRefresh) {
                final ComponentDescription cd1 = EasyMock.createNiceMock(ComponentDescription.class);
                EasyMock.expect(cd1.getIdentifier()).andReturn(WorkflowTestUtils.CD_IDENTIFIER).anyTimes();
                EasyMock.expect(cd1.getConfiguration()).andReturn(new HashMap<String, Serializable>()).anyTimes();
                EasyMock.expect(cd1.getInputMetaData(WorkflowTestUtils.INPUT_NAME))
                    .andReturn(new HashMap<String, Serializable>()).anyTimes();
                EasyMock.expect(cd1.getOutputMetaData(WorkflowTestUtils.OUTPUT_NAME))
                    .andReturn(new HashMap<String, Serializable>()).anyTimes();
                EasyMock.replay(cd1);

                final ComponentDescription cd2 = EasyMock.createNiceMock(ComponentDescription.class);
                EasyMock.expect(cd2.getIdentifier()).andReturn(WorkflowTestUtils.CD_IDENTIFIER).anyTimes();
                EasyMock.expect(cd2.getConfiguration()).andReturn(new HashMap<String, Serializable>()).anyTimes();
                EasyMock.expect(cd2.getOutputMetaData(WorkflowTestUtils.OUTPUT_NAME)).
                    andReturn(new HashMap<String, Serializable>()).anyTimes();
                EasyMock.replay(cd2);
                
                final ComponentDescription cd3 = EasyMock.createNiceMock(ComponentDescription.class);
                EasyMock.expect(cd3.getIdentifier()).andReturn("de.rcenvironment.rce.components.python.PythonComponent_Python").anyTimes();
                EasyMock.expect(cd3.getInputDefinitions())
                    .andReturn(new HashMap<String, Class<? extends Serializable>>()).anyTimes();
                EasyMock.expect(cd3.getOutputDefinitions())
                    .andReturn(new HashMap<String, Class<? extends Serializable>>()).anyTimes();
                Map<String, Serializable> config = new HashMap<String, Serializable>();
                config.put("pythonInstallation", "python");
                EasyMock.expect(cd3.getConfiguration()).andReturn(config).anyTimes();
                EasyMock.expect(cd3.getConfiguration(ComponentDescription.DEFAULT_CONFIG_ID))
                    .andReturn(config).anyTimes();
                EasyMock.expect(cd3.getConfigurationId()).andReturn(ComponentDescription.DEFAULT_CONFIG_ID).anyTimes();
                EasyMock.expect(cd3.getConfigurationIds()).andReturn(new ArrayList<String>()).anyTimes();
                EasyMock.expect(cd3.getDynamicInputDefinitions())
                    .andReturn(new HashMap<String, Class<? extends Serializable>>()).anyTimes();
                EasyMock.expect(cd3.getDynamicOutputDefinitions())
                    .andReturn(new HashMap<String, Class<? extends Serializable>>()).anyTimes();
                EasyMock.replay(cd3);
                
                return new ArrayList<ComponentDescription>() {

                    {
                        add(cd1);
                        add(cd2);
                        add(cd3);
                    }
                };
            }

            return null;
        }

    }

    /**
     * Test implementation of {@link PlatformService}.
     * 
     * @author Doreen Seider
     */
    protected class DummyPlatformService extends PlatformServiceDefaultStub {

        @Override
        public PlatformIdentifier getPlatformIdentifier() {
            return PlatformIdentifierFactory.fromHostAndNumberString("localhost:1");
        }

    }
}
