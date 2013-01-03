/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.Output;

/**
 * Test class for the scheduling in the default component.
 * @author Sascha Zur
 */

public class DefaultComponentTest {

  
    
    /**
     * Testcomponent to extend the DefaultComponent without using the needed super.onPrepare.
     * This has to fail.
     *
     * @author Sascha Zur
     */
    private class TestForFailureComponent extends DefaultComponent{

        @Override
        public void onPrepare(ComponentInstanceInformation incCompInstanceInformation) throws ComponentException {
            
        }
    };

    /**
     * TestComponent to extend the DefaultComponent with onPrepare called. 
     * Used for all further tests.
     *
     * @author Sascha Zur
     */
    private class DefaultComponentTestImpl extends DefaultComponent{

        @Override
        public void onPrepare(ComponentInstanceInformation incCompInstanceInformation) throws ComponentException {
            super.onPrepare(incCompInstanceInformation);
        }
    };
    /**
     * TestComponent to extend the DefaultComponent. 
     *
     * @author Sascha Zur
     */
    private class DummyComponentInstanceInformation extends ComponentInstanceInformation{

        private  Map<String, Class<? extends Serializable>> inputDefinitions;
        private Map <String, Map<String, Serializable>> inputMetaData;
        
        public DummyComponentInstanceInformation(String newIdentifier, String newName, String newWorkingDirectory,
            ComponentDescription newComponentDescription, ComponentContext newComponentContext, User newUser, boolean newInputConnected,
            Set<Output> newOutputs) {
            super(newIdentifier, newName, newWorkingDirectory, newComponentDescription, 
                newComponentContext, newUser, newInputConnected, newOutputs);
        }
        
        public DummyComponentInstanceInformation(Input first, Input second, String metaFirst, String metaSecond){
            super(null, null, null, null, null, null, false, new HashSet<Output>());
            inputDefinitions = new HashMap<String, Class<? extends Serializable>>();
            inputDefinitions.put(first.getName(), null);
            inputDefinitions.put(second.getName(), null);
            
            inputMetaData = new HashMap<String,  Map<String, Serializable>>();
            
            Map <String, Serializable> meta1 = new HashMap <String, Serializable>();
            meta1.put(ComponentConstants.METADATAKEY_INPUT_USAGE, metaFirst);
            inputMetaData.put(first.getName(), meta1);
            
            Map <String, Serializable> meta2 = new HashMap <String, Serializable>();
            meta2.put(ComponentConstants.METADATAKEY_INPUT_USAGE, metaSecond);
            inputMetaData.put(second.getName(), meta2);
            
        }
        public Map<String, Class<? extends Serializable>> getInputDefinitions() {
            return inputDefinitions;
        }

        /**
         * @param inputName The name of the {@link Input} to get meta data for.
         * @return the {@link Input}'s meta data.
         */
        public Map<String, Serializable> getInputMetaData(String inputName) {
            return inputMetaData.get(inputName);
        }
        
    };
    
    private TestForFailureComponent testComponentWithoutOnPrepare;
    private DefaultComponentTestImpl testComponentWithOnPrepare;
    private Input first;
    private Input second;
    private Deque<Input> inputValuesFirst;
    private Deque<Input> inputValuesSecond;
    
    /** Before. */
    @Before
    public void setUp(){
        testComponentWithoutOnPrepare = new TestForFailureComponent();
        testComponentWithOnPrepare = new DefaultComponentTestImpl();

        first =  new Input("first", java.lang.Double.class, 5, null, null, 1);
        second = new Input("second", java.lang.Double.class, 5, null, null, 1);
        inputValuesFirst = new LinkedList<Input>();
        inputValuesSecond =  new LinkedList<Input>();
    }
    
    
    /** Test. */
    @Test
    public void testSuperNotCalled(){
        try {
            testComponentWithoutOnPrepare.canRunAfterNewInput(null, null);
            Assert.fail("ComponentExeption expected");
        } catch (ComponentException e){
            assertTrue(true);
        }
    }
    
    /** Test. */
    @Test
    public void testSuperOnPrepareCalled(){
        try {
            testComponentWithOnPrepare.onPrepare(new DummyComponentInstanceInformation(first, second, "", ""));
        } catch (ComponentException e) {
            Assert.fail();
        }
        Assert.assertNotNull(testComponentWithOnPrepare.instInformation); 
        
    }

    /** Test. */
    @Test
    public void testCanRunAfterNewInputFalse(){
        try {
            testComponentWithOnPrepare.onPrepare(new DummyComponentInstanceInformation(first, second, "", ""));
            inputValuesFirst.add(first);
            Map <String, Deque<Input>> inputs = new HashMap<String, Deque<Input>>();
            inputs.put(first.getName(), inputValuesFirst);
            Assert.assertFalse(testComponentWithOnPrepare.canRunAfterNewInput(first, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
    }
    
    /** Test. */
    @Test
    public void testCanRunAfterNewInputTrue(){
        try {
            testComponentWithOnPrepare.onPrepare(new DummyComponentInstanceInformation(first, second, "", ""));
        } catch (ComponentException e1) {
            Assert.fail();
        }
        
        inputValuesFirst.add(first);
      
        inputValuesSecond.add(second);
        
        Map <String, Deque<Input>> inputs = new HashMap<String, Deque<Input>>();
        inputs.put(first.getName(), inputValuesFirst);
        inputs.put(second.getName(), inputValuesSecond);
        
        try {
            Assert.assertTrue(testComponentWithOnPrepare.canRunAfterNewInput(second, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
        
    }
    /** Test. */
    @Test
    public void testCanRunAfterNewInputFalseReq(){
        try {
            testComponentWithOnPrepare.onPrepare(new DummyComponentInstanceInformation(first, second, 
                ComponentConstants.INPUT_USAGE_TYPES[0], ComponentConstants.INPUT_USAGE_TYPES[0]));
            inputValuesFirst.add(first);
            Map <String, Deque<Input>> inputs = new HashMap<String, Deque<Input>>();
            inputs.put(first.getName(), inputValuesFirst);
            Assert.assertFalse(testComponentWithOnPrepare.canRunAfterNewInput(first, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
    }
    
    /** Test. */
    @Test
    public void testCanRunAfterNewInputTrueReq(){
        try {
            testComponentWithOnPrepare.onPrepare(new DummyComponentInstanceInformation(first, second,  
                ComponentConstants.INPUT_USAGE_TYPES[0], ComponentConstants.INPUT_USAGE_TYPES[0]));
        } catch (ComponentException e1) {
            Assert.fail();
        }
        
        inputValuesFirst.add(first);
      
        inputValuesSecond.add(second);
        
        Map <String, Deque<Input>> inputs = new HashMap<String, Deque<Input>>();
        inputs.put(first.getName(), inputValuesFirst);
        inputs.put(second.getName(), inputValuesSecond);
        
        try {
            Assert.assertTrue(testComponentWithOnPrepare.canRunAfterNewInput(second, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
        
    }
    /** Test. */
    @Test
    public void testCanRunAfterNewInputWithInitVars(){
        try {
            testComponentWithOnPrepare.onPrepare(new DummyComponentInstanceInformation(first, second,  
                ComponentConstants.INPUT_USAGE_TYPES[0], ComponentConstants.INPUT_USAGE_TYPES[1]));
        } catch (ComponentException e1) {
            Assert.fail();
        }
        
        inputValuesFirst.add(first);
        inputValuesSecond.add(second);
        
        Map <String, Deque<Input>> inputs = new HashMap<String, Deque<Input>>();
        inputs.put(first.getName(), inputValuesFirst);
        
        try {
            Assert.assertFalse(testComponentWithOnPrepare.canRunAfterNewInput(first, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
               
        inputs.put(second.getName(), inputValuesSecond);
        
        try {
            Assert.assertTrue(testComponentWithOnPrepare.canRunAfterNewInput(second, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
        
        inputs.clear();
        inputs.put(first.getName(), inputValuesFirst);
        try {
            Assert.assertTrue(testComponentWithOnPrepare.canRunAfterNewInput(first, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
    }
    
    /** Test. */
    @Test
    public void testCanRunAfterNewInputWithOptionalVars(){
        try {
            testComponentWithOnPrepare.onPrepare(new DummyComponentInstanceInformation(first, second,  
                ComponentConstants.INPUT_USAGE_TYPES[0], ComponentConstants.INPUT_USAGE_TYPES[2]));
        } catch (ComponentException e1) {
            Assert.fail();
        }
        
        inputValuesFirst.add(first);
        Map <String, Deque<Input>> inputs = new HashMap<String, Deque<Input>>();
        inputs.put(first.getName(), inputValuesFirst);
        
        try {
            Assert.assertTrue(testComponentWithOnPrepare.canRunAfterNewInput(first, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
        // Should also run a second time.
        try {
            Assert.assertTrue(testComponentWithOnPrepare.canRunAfterNewInput(first, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
       
        inputs.clear();
        inputValuesSecond.add(second);
        inputs.put(second.getName(), inputValuesSecond);
        
        try {
            Assert.assertFalse(testComponentWithOnPrepare.canRunAfterNewInput(second, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
        
        inputs.put(first.getName(), inputValuesFirst);

        try {
            Assert.assertTrue(testComponentWithOnPrepare.canRunAfterNewInput(first, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
    }
    
    /** Test. */
    @Test
    public void testCanRunAfterNewInputWithOptAndInit(){
        try {
            testComponentWithOnPrepare.onPrepare(new DummyComponentInstanceInformation(first, second,  
                ComponentConstants.INPUT_USAGE_TYPES[1], ComponentConstants.INPUT_USAGE_TYPES[2]));
        } catch (ComponentException e1) {
            Assert.fail();
        }
        
        inputValuesFirst.add(first);
        Map <String, Deque<Input>> inputs = new HashMap<String, Deque<Input>>();
        inputs.put(first.getName(), inputValuesFirst);
        try {
            Assert.assertTrue(testComponentWithOnPrepare.canRunAfterNewInput(first, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }

        inputs.clear();
        inputValuesSecond.add(second);
        inputs.put(second.getName(), inputValuesSecond);
        try {
            Assert.assertTrue(testComponentWithOnPrepare.canRunAfterNewInput(second, inputs));
        } catch (ComponentException e) {
            Assert.fail();
        }
    }
    
    
    /** After. */
    @After
    public void tearDown(){
        testComponentWithoutOnPrepare = null;
        testComponentWithOnPrepare = null;
    }

}
