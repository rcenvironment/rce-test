/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.DeclarativeComponentDescription;

/**
 * Utils class for the workflow tests.
 * 
 * @author Sascha Zur
 *
 */
public final class WorkflowTestUtils {
    /** Constant.*/
    public static final String WFID = "Charles M.";
    /** Constant.*/
    public static final String KEY = "Key";
    /** Constant.*/
    public static final String CD_IDENTIFIER = DummyComponent.class.getCanonicalName()
            + "_Dummy";
    /** Constant.*/
    public static final String OUTPUT_NAME = "ah";
    /** Constant.*/
    public static final String NODE2_NAME = "Noch krassere Component";
    /** Constant.*/
    public static final String INPUT_NAME = "86";
    /** Constant.*/
    public static final String PROP_MAP_ID = "trallaaa";
    /** Constant.*/
    public static final String PLACEHOLDERNAME = "${testPlaceholder}";
    /** Constant.*/
    public static final String GLOBAL_PLACEHOLDERNAME = "${global.testPlaceholder2}";
    /** Constant.*/
    public static final String ENCRYPTED_PLACEHOLDERNAME = "${*.testPlaceholder3}";
    /**
     * Satisfy checkstyle.
     */
    @Deprecated
    private WorkflowTestUtils() {

    }
    /**
     * Creates a workflow description for testing.
     * @return dummy wf description
     * @throws Exception :
     */
    protected static WorkflowDescription createWorkflowDescription(){
        WorkflowDescription wd = new WorkflowDescription(WFID);
        wd.setName("Schultz");
        wd.setTargetPlatform(PlatformIdentifierFactory.fromNodeId("snoopy"));

        String version = "2.0";
        Map<String, Class<? extends Serializable>> inputDefs = new HashMap<String, Class<? extends Serializable>>();
        Map<String, Class<? extends Serializable>> outputDefs = new HashMap<String, Class<? extends Serializable>>();
        outputDefs.put("output11", String.class);
        outputDefs.put("output12", String.class);
        Map<String, Class<? extends Serializable>> configDefs = new HashMap<String, Class<? extends Serializable>>();
        configDefs.put(KEY, String.class);
        configDefs.put("key2", Boolean.class);
        configDefs.put("key3", Integer.class);
        configDefs.put("2key", String.class);
        configDefs.put("3key", String.class);
        configDefs.put("4key", String.class);
        Map<String, Serializable> defaultConfig = new HashMap<String, Serializable>();
        defaultConfig.put(KEY, new String(""));
        defaultConfig.put("key3", new Integer(1));
        defaultConfig.put("2key", PLACEHOLDERNAME);
        defaultConfig.put("3key", GLOBAL_PLACEHOLDERNAME);
        defaultConfig.put("4key", ENCRYPTED_PLACEHOLDERNAME);
        byte[] icon16 = new byte[10];
        byte[] icon32 = new byte[0];

        DeclarativeComponentDescription declarativeCD1 = new DeclarativeComponentDescription(CD_IDENTIFIER, "Component One",
                "group 13", version, inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);

        ComponentDescription cd1 = new ComponentDescription(declarativeCD1);
        cd1.setPlatform(PlatformIdentifierFactory.fromHostAndNumberString("linus:3"));

        WorkflowNode node1 = new WorkflowNode(cd1);
        node1.setLocation(5, 4);
        node1.setName("Krasse Component");
        wd.addWorkflowNode(node1);

        version = "7.0";
        inputDefs = new HashMap<String, Class<? extends Serializable>>();
        outputDefs.put("input21", String.class);
        outputDefs.put("input22", String.class);
        outputDefs = new HashMap<String, Class<? extends Serializable>>();
        configDefs = new HashMap<String, Class<? extends Serializable>>();
        configDefs.put(KEY, Boolean.class);

        defaultConfig = new HashMap<String, Serializable>();
        defaultConfig.put(KEY, Boolean.valueOf(true));

        DeclarativeComponentDescription declarativeCD2 = new DeclarativeComponentDescription(CD_IDENTIFIER, "Component One",
                "group 13", version, inputDefs, outputDefs, null, null, configDefs, null, defaultConfig, icon16, icon32);

        ComponentDescription cd2 = new ComponentDescription(declarativeCD2);
        cd2.setPlatform(PlatformIdentifierFactory.fromHostAndNumberString("woodstock:3"));
        cd2.addInput(INPUT_NAME, "java.lang.Integer");
        cd2.addInput("hm", "java.lang.String");
        cd2.addOutput(OUTPUT_NAME, "java.lang.String");
        cd2.setInputMetaData(INPUT_NAME, "zahl", "true");
        cd2.setInputMetaData(INPUT_NAME, "primzahl", "false");
        cd2.setOutputMetaData(OUTPUT_NAME, "wissen", "true");

        Map<String, Serializable> config = new HashMap<String, Serializable>();
        config.put(KEY, Boolean.valueOf(false));
        cd2.addConfiguration(PROP_MAP_ID, config);

        WorkflowNode node2 = new WorkflowNode(cd2);
        node2.setLocation(8, 1);
        node2.setName(NODE2_NAME);

        node2.setPropertyMapId(PROP_MAP_ID);
        wd.addWorkflowNode(node2);

        Connection connection1 = new Connection(node1, "output11", node2, "input21");
        wd.addConnection(connection1);

        Connection connection2 = new Connection(node1, "output12", node2, "input22");
        wd.addConnection(connection2);

        return wd;
    }
}
