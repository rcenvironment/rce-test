/*
 * Copyright (C) 2006-2014 DLR, Germany, 2006-2010 Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.notification;

import java.util.Date;

import junit.framework.TestCase;
import de.rcenvironment.core.communication.common.NodeIdentifier;

/**
 * Test cases for the class {@link NotificationHeader}.
 * 
 * @author Andre Nurzenski
 * @author Doreen Seider
 */
public class NotificationHeaderTest extends TestCase {

    /** Time this thread is suspended. */
    private static final int SLEEP = 100;

    private NotificationHeader myNotificationHeader = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myNotificationHeader = new NotificationHeader(NotificationTestConstants.NOTIFICATION_ID,
            NotificationTestConstants.NOTIFICATION_EDITION, NotificationTestConstants.LOCALHOST);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        myNotificationHeader = null;
    }

    /*
     * #################### Test for success ####################
     */

    /**
     * Test if the method can be called.
     */
    public final void testGetTimestampForSuccess() {
        myNotificationHeader.getTimestamp();
    }

    /**
     * Test if the method can be called.
     */
    public final void testGetPublisherNameForSuccess() {
        myNotificationHeader.getNotificationIdentifier();
    }

    /**
     * Test if the method can be called.
     */
    public final void testGetPublisherPlatformForSuccess() {
        myNotificationHeader.getPublishPlatform();
    }

    /*
     * #################### Test for failure ####################
     */

    /*
     * #################### Test for sanity ####################
     */

    /**
     * Test for the correct timestamp.
     * 
     * @throws InterruptedException if an exception occurs.
     */
    public final void testGetTimestampForSanity() throws InterruptedException {
        Date timestamp = myNotificationHeader.getTimestamp();
        Thread.sleep(SLEEP);

        assertNotNull(timestamp);
        assertTrue(timestamp.before(new Date()));
    }

    /**
     * Test for the correct publisher.
     */
    public final void testGetPublisherNameForSanity() {
        String name = myNotificationHeader.getNotificationIdentifier();

        assertNotNull(name);
        assertEquals(name, NotificationTestConstants.NOTIFICATION_ID);
    }

    /**
     * Test for the correct publisher.
     */
    public final void testGetPublisherPlatformForSanity() {
        NodeIdentifier platform = myNotificationHeader.getPublishPlatform();

        assertNotNull(platform);
        assertEquals(platform, NotificationTestConstants.LOCALHOST);
    }

    /**
     * Test if two equal headers are announced as equal.
     */
    public final void testEqualsForSanity() {
        NotificationHeader header = new NotificationHeader(NotificationTestConstants.NOTIFICATION_ID,
            NotificationTestConstants.NOTIFICATION_EDITION, NotificationTestConstants.LOCALHOST);
        assertTrue(myNotificationHeader.equals(header));
        header = new NotificationHeader("myPublisherName",
            NotificationTestConstants.NOTIFICATION_EDITION, NotificationTestConstants.LOCALHOST);
        assertFalse(myNotificationHeader.equals(header));
        header = new NotificationHeader(NotificationTestConstants.NOTIFICATION_ID,
            NotificationTestConstants.NOTIFICATION_EDITION, NotificationTestConstants.REMOTEHOST);
        assertFalse(myNotificationHeader.equals(header));
    }

    /**
     * Test if two equal headers are announced as equal.
     */
    public final void testHashCodeForSanity() {
        assertEquals(myNotificationHeader.toString().hashCode(), myNotificationHeader.hashCode());
        NotificationHeader header = new NotificationHeader(NotificationTestConstants.NOTIFICATION_ID,
            NotificationTestConstants.NOTIFICATION_EDITION, NotificationTestConstants.LOCALHOST);
        assertEquals(myNotificationHeader.toString().hashCode(), header.hashCode());
    }

    /**
     * Test if the header is correctly represented as a string.
     */
    public final void testToStringForSanity() {
        String expected = myNotificationHeader.getNotificationIdentifier() + "[" + myNotificationHeader.getNumber() + "]"
            + "@" + myNotificationHeader.getPublishPlatform() + " - " + myNotificationHeader.getTimestamp();
        String actual = myNotificationHeader.toString();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    /**
     * Test if two headers are correctly compared.
     * 
     * @throws Exception if an error occur.
     */
    public final void testCompareToForSanity() throws Exception {

        final int lower = -1;
        final int greater = 1;
        final int equal = 0;
        NotificationHeader newerNotification = new NotificationHeader(NotificationTestConstants.NOTIFICATION_ID,
            NotificationTestConstants.NOTIFICATION_EDITION + greater, NotificationTestConstants.LOCALHOST);
        assertEquals(lower, myNotificationHeader.compareTo(newerNotification));
        assertEquals(greater, newerNotification.compareTo(myNotificationHeader));
        assertEquals(equal, myNotificationHeader.compareTo(myNotificationHeader));
    }

}