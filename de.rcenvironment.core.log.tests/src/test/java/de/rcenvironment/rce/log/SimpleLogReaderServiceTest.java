/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;

/**
 * Test cases for {@link SimpleLogReaderService}.
 *
 * @author Doreen Seider
 */
public class SimpleLogReaderServiceTest {

    private final String removed = "removed";
    private final String added = "added";
    private final PlatformIdentifier pi = PlatformIdentifierFactory.fromHostAndNumberString("localhost:1");
    private final SerializableLogEntry logEntry = EasyMock.createNiceMock(SerializableLogEntry.class);
    private final SerializableLogListener logListener = new SerializableLogListener() {

        private static final long serialVersionUID = 1L;

        @Override
        public void logged(SerializableLogEntry entry) {}

        @Override
        public Class<? extends Serializable> getInterface() {
            return SerializableLogListener.class;
        }
    };
    
    private SimpleLogReaderService logReader;
    
    /** Set up method. */
    @Before
    public void setUp() {
        logReader = new SimpleLogReaderService();
        logReader.bindDistributedLogReaderService(new DummyDistributedLogReaderService());
    }
    
    /** Test. */
    @Test
    public void testAddListener() {
        try {
            logReader.addLogListener(logListener, pi);
            fail();
        } catch (RuntimeException e) {
            assertTrue(added.equals(e.getMessage()));
        }
    }

    /** Test. */
    @Test
    public void testGetLog() {
        List<SerializableLogEntry> entries = logReader.getLog(pi);
        assertEquals(logEntry, entries.get(0));
        assertEquals(1, entries.size());
    }

    /** Test. */
    @Test
    public void testRemoveListener() {
        try {
            logReader.removeLogListener(logListener, pi);
            fail();
        } catch (RuntimeException e) {
            assertTrue(removed.equals(e.getMessage()));
        }
    }

    /** Test. */
    @Test
    public void serviceIfServicesAreStatic() {
        new SimpleLogReaderService().getLog(pi);
    }
    
    /** Test. */
    @Test(expected = IllegalStateException.class)
    public void serviceIsGone() {
        logReader.unbindDistributedLogReaderService(null);
        logReader.getLog(pi);
    }

    /**
     * Test {@link DistributedLogReaderService} implementation.
     * @author Doreen Seider
     */
    private class DummyDistributedLogReaderService implements DistributedLogReaderService {

        @Override
        public void addLogListener(SerializableLogListener l, PlatformIdentifier platformIdentifier) {
            if (l.equals(logListener) && platformIdentifier.equals(pi)) {
                throw new RuntimeException(added);                
            }
        }

        @SuppressWarnings("serial")
        @Override
        public List<SerializableLogEntry> getLog(PlatformIdentifier platformIdentifier) {
            return new LinkedList<SerializableLogEntry>() {{ add(logEntry); }};
        }

        @Override
        public void removeLogListener(SerializableLogListener l, PlatformIdentifier platformIdentifier) {
            if (l.equals(logListener) && platformIdentifier.equals(pi)) {
                throw new RuntimeException(removed);                
            }
        }
        
    }
}
