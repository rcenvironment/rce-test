/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.component.executor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import de.rcenvironment.commons.TempFileUtils;

/**
 * Contains files to upload and provides convenient methods to access them.
 * @author Doreen Seider
 */
public final class FilesToUpload {
    
    private static final String PARSING_ERROR_MESSAGE = "Parsing file to upload failed";
    
    private static final String GENERATING_ERROR_MESSAGE = "Generating file to upload string failed";
    
    private static ObjectMapper mapper = new ObjectMapper();

    private Map<String, byte[]> files;
    
    private FilesToUpload(Map<String, byte[]> files) throws IOException {
        this.files = files;
    }
    
    /**
     * Creates a {@link FilesToUpload} object.
     * @param string JSON string representing serialized {@link FilesToUpload} object
     * @return created {@link FilesToUpload}
     */
    public static FilesToUpload valueAs(String string) {
        try {
            Map<String, byte[]> files = mapper.readValue(string, new TypeReference<Map<String, byte[]>>() { });
            return new FilesToUpload(files);
        } catch (JsonParseException e) {
            throw new RuntimeException(PARSING_ERROR_MESSAGE, e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(PARSING_ERROR_MESSAGE, e);
        } catch (IOException e) {
            throw new RuntimeException(PARSING_ERROR_MESSAGE, e);
        }
    }
    
    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(files);
        } catch (JsonGenerationException e) {
            throw new RuntimeException(GENERATING_ERROR_MESSAGE, e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(GENERATING_ERROR_MESSAGE, e);
        } catch (IOException e) {
            throw new RuntimeException(GENERATING_ERROR_MESSAGE, e);
        }
    }
    
    public List<String> getFileNames() {
        return new ArrayList<String>(files.keySet());
    }
    
    /**
     * Adds files, which should be uploaded.
     * @param filesToAdd files to add
     * @throws IOException on error
     */
    public void addFiles(File[] filesToAdd) throws IOException {
        for (File file : filesToAdd) {
            this.files.put(file.getName(), FileUtils.readFileToByteArray(file));
        }
    }
    
    /**
     * Removes files, which should not be uploaded anymore.
     * @param filenamesToRemove name of files to remove
     */
    public void removeFiles(String[] filenamesToRemove) {
        for (String filename : filenamesToRemove) {
            files.remove(filename);
        }
    }
    
    /**
     * @return list of files which should currently be uploaded.
     * @throws IOException on error
     */
    public List<File> retrieveFiles() throws IOException {
        List<File> fileList = new ArrayList<File>();
        
        for (String filename : files.keySet()) {
            File file = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(filename);
            FileUtils.writeByteArrayToFile(file, files.get(filename));
            fileList.add(file);
        }
        return fileList;
    }
}
