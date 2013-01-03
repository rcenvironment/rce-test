/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.configuration.PersistentSettingsService;

/**
 * Testclass for the placeholder.
 * 
 * @author Sascha Zur
 * 
 */
public class WorkflowExecutionPlaceholderHelperTest {

    private WorkflowExecutionPlaceholderHelper weph;
    private String placeholder1 = "${myPlaceholder1}";
    private String placeholder2 = "${*.myPlaceholder2}";
    private String placeholder3 = "${global.myPlaceholder3}";
    private String placeholder4 = "${global.*.myPlaceholder4}";

    private String componentID1 = "de.rcenvironment.components.testcomp.TestComp";
    private String componentID2 = "de.rcenvironment.components.testcomp.TestComp2";
    private String componentUUID1 = "i am an UUID";

    private String dot = ".";

    private WorkflowDescription wd = WorkflowTestUtils.createWorkflowDescription();

    private final String globalPlaceholderTestValue = "tada";
    private final String placeholderTestValue = "tada2";
    /** Init. */
    @Before
    public void init() {
        WorkflowExecutionPlaceholderHelper
        .setPersistentSettingsService(new DummyPersistentSettingsService());
        weph = WorkflowExecutionPlaceholderHelper
                .createPlaceholderDescriptionFromWorkflowDescription(wd);
    }

    /** Test. */
    @Test
    public void testGetNameOfPlaceholder() {
        Assert.assertEquals("myPlaceholder1",
                WorkflowExecutionPlaceholderHelper
                .getNameOfPlaceholder(placeholder1));
        Assert.assertEquals("myPlaceholder2",
                WorkflowExecutionPlaceholderHelper
                .getNameOfPlaceholder(placeholder2));
        Assert.assertEquals("myPlaceholder3",
                WorkflowExecutionPlaceholderHelper
                .getNameOfPlaceholder(placeholder3));
        Assert.assertEquals("myPlaceholder4",
                WorkflowExecutionPlaceholderHelper
                .getNameOfPlaceholder(placeholder4));
    }

    /** Test. */
    @Test
    public void testIsEncryptedPlaceholder() {
        weph.addPlaceholder(placeholder1, componentID1, componentUUID1);
        weph.addPlaceholder(placeholder2, componentID1, componentUUID1);
        weph.addPlaceholder(placeholder3, componentID1, componentUUID1);
        weph.addPlaceholder(placeholder4, componentID1, componentUUID1);
        Assert.assertFalse(weph.isEncryptedPlaceholder(componentID1
                + dot
                + WorkflowExecutionPlaceholderHelper
                .getNameOfPlaceholder(placeholder1)));
        Assert.assertTrue(weph.isEncryptedPlaceholder(componentID1
                + dot
                + WorkflowExecutionPlaceholderHelper
                .getNameOfPlaceholder(placeholder2)));
        Assert.assertFalse(weph.isEncryptedPlaceholder(componentID1
                + dot
                + WorkflowExecutionPlaceholderHelper
                .getNameOfPlaceholder(placeholder3)));
        Assert.assertTrue(weph.isEncryptedPlaceholder(componentID1
                + dot
                + WorkflowExecutionPlaceholderHelper
                .getNameOfPlaceholder(placeholder4)));
    }

    /** Test. */
    @Test
    public void testPlaceholderGetter() {
        weph.addPlaceholder(placeholder1, componentID1, componentUUID1);
        Assert.assertTrue(weph
                .getIdentifiersOfPlaceholderContainingComponents().contains(
                        componentID1));
        Assert.assertFalse(weph
                .getIdentifiersOfPlaceholderContainingComponents().contains(
                        componentID2));
        Assert.assertTrue(weph.getPlaceholderNameSetOfComponentInstance(
                componentUUID1).contains(
                        WorkflowExecutionPlaceholderHelper
                        .getNameOfPlaceholder(placeholder1)));
        weph.addPlaceholder(placeholder2, componentID1, componentUUID1);
        Assert.assertTrue(weph.getIdentifiersOfPlaceholderContainingComponents().contains(
                componentID1));
        Assert.assertFalse(weph.getIdentifiersOfPlaceholderContainingComponents().contains(
                componentID2));
        Assert.assertTrue(weph.getPlaceholderNameSetOfComponentInstance(
                componentUUID1).contains(
                        WorkflowExecutionPlaceholderHelper
                        .getNameOfPlaceholder(placeholder2)));

        weph.addPlaceholder(placeholder3, componentID1, null);
        Assert.assertTrue(weph
                .getIdentifiersOfPlaceholderContainingComponents().contains(
                        componentID1));
        Assert.assertFalse(weph
                .getIdentifiersOfPlaceholderContainingComponents().contains(
                        componentID2));
        Assert.assertFalse(weph.getPlaceholderNameSetOfComponentInstance(
                componentUUID1).contains(
                        WorkflowExecutionPlaceholderHelper
                        .getNameOfPlaceholder(placeholder3)));
        Assert.assertTrue(weph.getPlaceholderNameSetOfComponentID(componentID1)
                .contains(
                        WorkflowExecutionPlaceholderHelper
                        .getNameOfPlaceholder(placeholder3)));

        Assert.assertTrue(weph.getPlaceholderNamesOfComponentInstance(
                componentUUID1).contains(placeholder1));
        Assert.assertFalse(weph.getPlaceholderNamesOfComponentInstance(
                componentUUID1).contains(placeholder3));
        Assert.assertFalse(weph.getPlaceholderNamesOfComponentInstance(
                componentUUID1).contains(placeholder4));

        Assert.assertFalse(weph.getPlaceholderOfComponent(componentID1)
                .contains(placeholder1));
        Assert.assertTrue(weph.getPlaceholderOfComponent(componentID1)
                .contains(placeholder3));
        Assert.assertFalse(weph.getPlaceholderOfComponent(componentID1)
                .contains(placeholder4));

        Assert.assertNull(weph.getPlaceholderNameSetOfComponentID(null));

        Assert.assertNotNull(weph.getComponentInstances(componentID1));
        Assert.assertNull(weph.getComponentInstances(componentID2));
    }


    /** Test. */
    @Test
    public void testSetPlaceholderValue(){
        weph.setGlobalPlaceholderValue(WorkflowTestUtils.GLOBAL_PLACEHOLDERNAME,
                WorkflowTestUtils.CD_IDENTIFIER, globalPlaceholderTestValue, WorkflowTestUtils.WFID, true);
        weph.setPlaceholderValue(WorkflowTestUtils.PLACEHOLDERNAME, WorkflowTestUtils.CD_IDENTIFIER,
                wd.getWorkflowNodes().iterator().next().getIdentifier(), 
                placeholderTestValue, WorkflowTestUtils.WFID, true);
        weph.setPlaceholderValue(WorkflowTestUtils.GLOBAL_PLACEHOLDERNAME, WorkflowTestUtils.CD_IDENTIFIER,
                wd.getWorkflowNodes().iterator().next().getIdentifier(), 
                globalPlaceholderTestValue, WorkflowTestUtils.WFID, true);
    }

    /** Test. */
    @Test
    public void testAllGetPlaceholderValues(){
        weph.setGlobalPlaceholderValue(WorkflowTestUtils.GLOBAL_PLACEHOLDERNAME,
                WorkflowTestUtils.CD_IDENTIFIER, globalPlaceholderTestValue, WorkflowTestUtils.WFID, true);
        weph.setPlaceholderValue(WorkflowTestUtils.PLACEHOLDERNAME, WorkflowTestUtils.CD_IDENTIFIER,
                wd.getWorkflowNodes().iterator().next().getIdentifier(), 
                placeholderTestValue, WorkflowTestUtils.WFID, true);
        Assert.assertEquals(placeholderTestValue,
                weph.getValueByPlaceholderName(WorkflowExecutionPlaceholderHelper.getNameOfPlaceholder(WorkflowTestUtils.PLACEHOLDERNAME), 
                        wd.getWorkflowNodes().iterator().next().getIdentifier()));
        Assert.assertEquals(globalPlaceholderTestValue, weph.getGlobalValueByPlaceholderName(
                WorkflowExecutionPlaceholderHelper.getNameOfPlaceholder(WorkflowTestUtils.GLOBAL_PLACEHOLDERNAME),
                WorkflowTestUtils.CD_IDENTIFIER));
        Assert.assertEquals(globalPlaceholderTestValue,
                weph.getValueByPlaceholder(WorkflowTestUtils.GLOBAL_PLACEHOLDERNAME, 
                        WorkflowTestUtils.CD_IDENTIFIER));
        Assert.assertEquals(placeholderTestValue,
                weph.getValueByPlaceholder(WorkflowTestUtils.PLACEHOLDERNAME, 
                        WorkflowTestUtils.CD_IDENTIFIER, 
                        wd.getWorkflowNodes().iterator().next().getIdentifier()));
    }
    /** Test. */
    @Test
    public void testHistoryMethods(){
        weph.saveHistory();
        weph.setGlobalPlaceholderValue(WorkflowTestUtils.GLOBAL_PLACEHOLDERNAME,
                WorkflowTestUtils.CD_IDENTIFIER, globalPlaceholderTestValue, WorkflowTestUtils.WFID, true);
        weph.setPlaceholderValue(WorkflowTestUtils.PLACEHOLDERNAME, WorkflowTestUtils.CD_IDENTIFIER,
                wd.getWorkflowNodes().iterator().next().getIdentifier(), 
                placeholderTestValue, WorkflowTestUtils.WFID, true);
        weph.saveHistory();
        weph.setGlobalPlaceholderValue(WorkflowTestUtils.GLOBAL_PLACEHOLDERNAME,
                WorkflowTestUtils.CD_IDENTIFIER, globalPlaceholderTestValue + "4", WorkflowTestUtils.WFID, true);
        weph.setPlaceholderValue(WorkflowTestUtils.PLACEHOLDERNAME, WorkflowTestUtils.CD_IDENTIFIER,
                wd.getWorkflowNodes().iterator().next().getIdentifier(), 
                placeholderTestValue + "3", WorkflowTestUtils.WFID, true);
        weph.saveHistory();
        String cid = wd.getWorkflowNodes().iterator().next().getIdentifier();
        Assert.assertEquals(true, weph.getInstancePlaceholderHistory(
                WorkflowExecutionPlaceholderHelper.getNameOfPlaceholder(WorkflowTestUtils.PLACEHOLDERNAME), cid).length == 2);
        Assert.assertEquals(true, weph.getComponentPlaceholderHistory(
                WorkflowExecutionPlaceholderHelper.getNameOfPlaceholder(WorkflowTestUtils.GLOBAL_PLACEHOLDERNAME), 
                WorkflowTestUtils.CD_IDENTIFIER, WorkflowTestUtils.WFID).length == 2);
        weph.deletePlaceholderHistory(cid, WorkflowExecutionPlaceholderHelper.getNameOfPlaceholder(WorkflowTestUtils.PLACEHOLDERNAME));
        Assert.assertEquals(globalPlaceholderTestValue, weph.getOtherPlaceholderHistoryValues(
                WorkflowExecutionPlaceholderHelper.getNameOfPlaceholder(WorkflowTestUtils.GLOBAL_PLACEHOLDERNAME))[0]);
        Assert.assertEquals(globalPlaceholderTestValue + "4", weph.getValueFromOtherComponentInWorkflow(
                WorkflowExecutionPlaceholderHelper.getNameOfPlaceholder(WorkflowTestUtils.GLOBAL_PLACEHOLDERNAME), 
                WorkflowTestUtils.CD_IDENTIFIER));

    }
    /** Test. */
    @Test
    public void testAllGetter(){
        Assert.assertNotNull(weph.getComponentTypeHistory());
        Assert.assertNotNull(weph.getComponentInstanceHistory());
        Assert.assertNotNull(weph.getComponentInstancePlaceholders());
        Assert.assertNotNull(weph.getComponentTypePlaceholders());
        Assert.assertNotNull(weph.getComponentInstancesOfType());
        Assert.assertNotNull(weph.getEncryptedPlaceholder());
        Assert.assertNull(weph.getValueFromOtherComponentInWorkflow(WorkflowTestUtils.PLACEHOLDERNAME, componentID1));
        Assert.assertEquals(true, weph.getOtherPlaceholderHistoryValues(WorkflowTestUtils.PLACEHOLDERNAME).length == 0);
        Assert.assertNotNull(weph.getPlaceholderOfComponentType(WorkflowTestUtils.CD_IDENTIFIER));
        Assert.assertNotNull(weph.getPlaceholderOfComponentInstance(wd.getWorkflowNodes().iterator().next().getIdentifier()));
    }  
 

    /**
     * Dummy class for testing.
     */
    private class DummyPersistentSettingsService implements
        PersistentSettingsService {
        private Map<String, List<String>> tmpMap;

        @Override
        public void saveStringValue(String key, String value) {
        }

        @Override
        public String readStringValue(String key) {
            return null;
        }

        @Override
        public void delete(String key) {

        }

        @Override
        public Map<String, List<String>> readMapWithStringList(String filename) {
            if (tmpMap == null){
                return new HashMap<String, List<String>>();
            } else {
                return tmpMap;
            }
        }

        @Override
        public void saveMapWithStringList(Map<String, List<String>> map,
                String filename) {
            Assert.assertNotNull(map);
            tmpMap = map;

        }

        @Override
        public void saveStringValue(String key, String value, String filename) {

        }

        @Override
        public String readStringValue(String key, String filename) {
            return null;
        }

        public void delete(String key, String filename) {

        }

    }
}
