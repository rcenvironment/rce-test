/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.simplewrapper.commons;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test case for {@link ConfigurationValueConverter}.
 * 
 * @author Christian Weiss
 */
public class PropertyValueConverterTest {

    /** Test. */
    @Test
    public void test() {
        final byte[] contentBytes = new byte[] { -128, -1, 0, 1, 127 };
        final String contentString = ConfigurationValueConverter.executableDirectoryContent(contentBytes);
        final byte[] contentBytes2 = ConfigurationValueConverter.executableDirectoryContent(contentString);
        Assert.assertEquals(contentBytes.length, contentBytes2.length);
        for (int index = 0; index < contentBytes.length; ++index) {
            Assert.assertEquals(contentBytes[index], contentBytes2[index]);
        }
    }

}
