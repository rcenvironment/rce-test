/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link TempFileUtils}.
 * 
 * @author Robert Mischke
 */
public class TempFileUtilsTest {

    private TempFileUtils testInstance;

    /**
     * Creates the test instance.
     * 
     */
    @Before
    public void setUp() {
        testInstance = new TempFileUtils();
    }

    /**
     * Basic Test: Checks that createManagedTempDir returns different directories on repeated calls.
     * 
     * @throws IOException on I/O errors
     */
    @Test
    public void testCreateManagedTempDir() throws IOException {
        File dir1 = testInstance.createManagedTempDir();
        File dir2 = testInstance.createManagedTempDir();
        Assert.assertTrue(dir1.isDirectory());
        Assert.assertTrue(dir2.isDirectory());
        Assert.assertFalse(dir1.getAbsolutePath().equals(dir2.getAbsolutePath()));
    }

    /**
     * Checks that createTempFileWithFixedFilename returns different files on repeated calls, and
     * that the expected filename is met.
     * 
     * @throws IOException on I/O errors
     */
    @Test
    public void testCreateTempFileWithFixedFilename() throws IOException {
        String filename = "fixedNameTest.ext";
        File file1 = testInstance.createTempFileWithFixedFilename(filename);
        File file2 = testInstance.createTempFileWithFixedFilename(filename);
        Assert.assertTrue(file1.isFile());
        Assert.assertTrue(file2.isFile());
        Assert.assertFalse(file1.getAbsolutePath().equals(file2.getAbsolutePath()));
        Assert.assertTrue(file1.getName().equals(filename));
        Assert.assertTrue(file2.getName().equals(filename));
    }

/**
     * Test for {@link TempFileUtils#disposeManagedTempDirOrFile(File).
     * 
     * @throws IOException on internal test errors
     */
    @Test
    public void testDisposeManagedTempDirOrFile() throws IOException {
        File dir1 = testInstance.createManagedTempDir();
        File dir2 = testInstance.createManagedTempDir("123asd()_-");
        File file1 = testInstance.createTempFileFromPattern("dummy*file.txt");

        // should succeed
        testInstance.disposeManagedTempDirOrFile(dir1);

        // test deleting some other temp file or directory; should fail
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            Assert.assertNotNull(tempDir);
            testInstance.disposeManagedTempDirOrFile(new File(tempDir, "deleteme.txt"));
            Assert.fail("Exception expected");
        } catch (IOException e) {
            // expected: an exception text about the root directory mismatch
            Assert.assertTrue(e.getMessage().contains("root"));
        }

        // should succeed
        testInstance.disposeManagedTempDirOrFile(dir2);
        
        // test deleting a file (instead of a directory)
        testInstance.disposeManagedTempDirOrFile(file1);
    }
}
