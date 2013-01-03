/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.commons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test cases for {@link StringUtils}.
 *
 * @author Doreen Seider
 */
public class StringUtilsTest {

    /** Test. */
    @Test
    public void testEscaping() {
        final String rawString = "holter" + StringUtils.SEPARATOR + "diePolter";
        final String escapedString = "holter" + StringUtils.ESCAPE_CHARACTER + StringUtils.SEPARATOR + "diePolter";
        
        assertEquals(escapedString, StringUtils.escapeSeparator(rawString));
        assertEquals(rawString, StringUtils.unescapeSeparator(escapedString));
        
    }
    
    /** Test. */
    @Test
    public void testSplit() {
        String stringToSplit = "ka" + StringUtils.ESCAPE_CHARACTER + StringUtils.SEPARATOR + "Bumm" + StringUtils.SEPARATOR + "puffPeng";
        
        String[] splittedString = StringUtils.split(stringToSplit);
        assertEquals(2, splittedString.length);
        assertEquals("ka" + StringUtils.ESCAPE_CHARACTER + StringUtils.SEPARATOR + "Bumm", splittedString[0]);
        assertEquals("puffPeng", splittedString[1]);
    }
    
    /** Test. */
    @Test
    public void testConcat() {
        String[] parts = new String[] { "la", "le", "l:u"};
        
        String result = StringUtils.concat(parts);
        String escChar = StringUtils.SEPARATOR;
        assertEquals("la" + escChar + "le" + escChar + "l\\:u", result);
        
    }
    
}
