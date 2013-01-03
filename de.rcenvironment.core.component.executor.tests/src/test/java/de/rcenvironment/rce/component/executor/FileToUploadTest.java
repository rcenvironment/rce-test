/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.core.component.executor.FilesToUpload;

/**
 * Test cases for {@link FilesToUpload}.
 * 
 * @author Doreen Seider
 */
public class FileToUploadTest {

    private FilesToUpload filesToUpload;
    
    private String fileName1 = "file name 1";
    
    private String fileName2 = "file name 2";
    
    private File file1;
    
    private File file2;
    
    /**
     * Set up.
     * @throws IOException on error.
     */
    @Before
    public void setUp() throws IOException {
        file1 = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(fileName1);
        file2 = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(fileName2);
    }
    
    /**
     * Test. 
     * @throws IOException on error
     **/
    @Test
    public void test() throws IOException {
        filesToUpload = FilesToUpload.valueAs("{}");
        filesToUpload.addFiles(new File[] { file1, file2 });
        
        List<File> files = filesToUpload.retrieveFiles();
        assertEquals(2, files.size());
        
        List<String> fileNames = filesToUpload.getFileNames();
        assertEquals(2, fileNames.size());
        assertTrue(fileNames.contains(fileName1));
        assertTrue(fileNames.contains(fileName2));
        
        filesToUpload.removeFiles(new String[] { fileName1 });
        
        files = filesToUpload.retrieveFiles();
        assertEquals(1, files.size());

        fileNames = filesToUpload.getFileNames();
        assertEquals(1, fileNames.size());
        assertTrue(fileNames.contains(fileName2));
        
        filesToUpload.addFiles(new File[] { file1 });
        
        String stringRepresentation = filesToUpload.toString();
        
        FilesToUpload filesToUpload2 = FilesToUpload.valueAs(stringRepresentation);
        
        files = filesToUpload2.retrieveFiles();
        assertEquals(2, files.size());
        
        fileNames = filesToUpload2.getFileNames();
        assertEquals(2, fileNames.size());
        assertTrue(fileNames.contains(fileName1));
        assertTrue(fileNames.contains(fileName2));
    }

}
