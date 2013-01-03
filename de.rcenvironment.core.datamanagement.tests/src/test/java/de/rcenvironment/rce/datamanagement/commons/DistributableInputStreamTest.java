/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement.commons;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;

/**
 * Test cases for {@link DistributableInputStream}.
 * 
 * seid_do: removed remote test, because it was not testable due to internal classes of communication bundle which needs to
 * be initialized before test run
 * 
 * @author Doreen Seider
 */
public class DistributableInputStreamTest {

    private InputStream inputStream;
    
    private Integer read = 7;
    
    private byte[] bytes;
    
    private int off;
    
    private int len;
    
    private int n;
    
    private long skipped = 9;
    
    private User cert = EasyMock.createNiceMock(User.class);
    
    private DataReference dataRef;
    
    private Integer revisionNo;
    
    private UUID uuid = UUID.randomUUID();
    
    private PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("horst:1");
    
    /** Set up.
     * @throws Exception if an error occurred.
     */
    @Before
    public void setUp() throws Exception {
        bytes = new byte[len];
        inputStream = EasyMock.createNiceMock(InputStream.class);
        EasyMock.expect(inputStream.read()).andReturn(read).anyTimes();
        EasyMock.expect(inputStream.read(bytes)).andReturn(read).anyTimes();
        EasyMock.expect(inputStream.read(bytes, off, len)).andReturn(read).anyTimes();
        EasyMock.expect(inputStream.skip(n)).andReturn(skipped).anyTimes();
        EasyMock.replay(inputStream);
        
        dataRef = new DataReference(DataReferenceType.fileObject, uuid, pi);
    }
    
    /** Test.
     * @throws Exception if an error occurred.
     */
    @Test
    public void testLocal() throws Exception {
        DistributableInputStream dis = new DistributableInputStream(cert, dataRef, revisionNo, inputStream);
        
        assertEquals(read.intValue(), dis.read());
        assertEquals(read.intValue(), dis.read(bytes));
        assertEquals(read.intValue(), dis.read(bytes, off, len));
        assertEquals(skipped, dis.skip(n));
        dis.close();
    }
}
