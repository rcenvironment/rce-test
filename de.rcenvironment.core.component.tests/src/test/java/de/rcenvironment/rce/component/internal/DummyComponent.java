/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.internal;

import java.util.Deque;
import java.util.Map;

import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.testutils.MockComponent;


/**
 * Dummy implementation of {@link Component}.
 * 
 * @author Doreen Seider
 */
public class DummyComponent extends MockComponent.Default {

    @Override
    public boolean canRunAfterNewInput(Input newInput, Map<String, Deque<Input>> inputValues) {
        return true;
    }

}
