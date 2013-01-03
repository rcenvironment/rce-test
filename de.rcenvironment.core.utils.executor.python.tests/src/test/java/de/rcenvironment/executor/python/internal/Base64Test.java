/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.executor.python.internal;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.is;


/**
 * To check compatibility between python and java libraries.
 *
 * @author Arne Bachmann
 */
public class Base64Test {
    
    /**
     * Some tests.
     */
    @Test
    public void testEncoding() {
        assertThat(Base64.encodeBase64String("abc".getBytes()), is("YWJj\r\n")); // always has a new line
        assertThat(Base64.encodeBase64String("abc\ndef".getBytes()), is("YWJjCmRlZg==\r\n"));
    }
    
    /**
     * Some tests.
     */
    @Test
    public void testDecoding() {
        assertThat(Base64.decodeBase64("YWJjZGVmZ2g="), is("abcdefgh".getBytes()));
        assertThat(Base64.decodeBase64("YWJjZGVmZ2gKMTIz"), is("abcdefgh\n123".getBytes()));
        assertThat(Base64.decodeBase64("YWJjZGVmZ2gKMTIzISIkJSY="), is("abcdefgh\n123!\"$%&".getBytes()));
        assertThat(Base64.decodeBase64("eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4e"
                + "Hh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4e"
                + "Hh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHg="),
            is(("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx").getBytes()));
        
    }

}
