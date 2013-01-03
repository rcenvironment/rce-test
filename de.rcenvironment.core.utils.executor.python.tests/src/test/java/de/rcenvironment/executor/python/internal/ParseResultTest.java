/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Test;

import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.commons.variables.BoundVariable;


/**
 * Coverage.
 *
 * @author Arne Bachmann
 */
public class ParseResultTest {

    /**
     * Just the constructor.
     */
    @Test
    public void test() {
        new ParseResult(new ArrayList<BoundVariable>(), new HashMap<String, String>(), new LinkedList<VariantArray>());
    }
    
}
