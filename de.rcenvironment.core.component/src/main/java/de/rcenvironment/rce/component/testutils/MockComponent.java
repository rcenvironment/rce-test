/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.testutils;


import java.util.Deque;
import java.util.Map;

import de.rcenvironment.rce.component.Component;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.endpoint.Input;

/**
 * Common test/mock implementations of {@link Component}. These can be used directly, or
 * can as super classes for custom mock classes.
 * 
 * Custom mock implementations of {@link Component} should use these as superclasses
 * whenever possible to avoid code duplication, and to shield the mock classes from irrelevant API
 * changes.
 * 
 * @author Doreen Seider
 */
public abstract class MockComponent {

    /**
     * A mock implementation of {@link Component} that throws an exception on every
     * method call. Subclasses for tests should override the methods they expect to be called.
     * 
     * @author Doreen Seider
     */
    public static class Default implements Component {

        @Override
        public void onPrepare(ComponentInstanceInformation compInstanceInformation) throws ComponentException {}

        @Override
        public boolean canRunAfterNewInput(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
            return false;
        }

        @Override
        public boolean canRunAfterRun(Input lastInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
            return false;
        }
        
        @Override
        public boolean runInitial(boolean inputsConnected) throws ComponentException {
            return true;
        }

        @Override
        public boolean runStep(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
            return true;
        }

        @Override
        public void onCancel() {}

        @Override
        public void onDispose() {}

        @Override
        public void onFinish() {}
        
    }
}
