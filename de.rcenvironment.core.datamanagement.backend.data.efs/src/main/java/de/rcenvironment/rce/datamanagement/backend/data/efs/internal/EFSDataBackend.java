/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.backend.data.efs.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.datamanagement.backend.DataBackend;

/**
 * IEFS implementation of {@link DataBackend}.
 * 
 * @author Sandra Schroedter
 * @author Juergen Klein
 */
public class EFSDataBackend implements DataBackend {

    /** Supported scheme. */
    public static final String SCHEMA_EFS = "efs";

    private static final Log LOGGER = LogFactory.getLog(EFSDataBackend.class);

    private static final String SCHEME_URI_COMPLETION = "://";

    private static final String FILE_NAME = "file";

    private static final String SEPARATOR = "-";

    private static final String SLASH = "/";

    private static final String ZIPFORMAT = ".gz";

    private static final String DATA = "data";

    private static Pattern parentPattern;

    private static Pattern uriPattern;

    private static boolean saveAsZip = true;

    private EFSDataBackendConfiguration configuration;

    private ConfigurationService configurationService;

    private EncapsulatedEFSService encapsulatedEFSService;



    static {

        /**
         * String defining the pattern for the parent directory.
         */
        final String patternStrParent = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
        /**
         * String defining the pattern for an URI.
         */
        final String patternStrURI = SLASH + patternStrParent + SLASH + "file" + SEPARATOR + "[0-9]*";
        /**
         * Compiled pattern for the parent directory.
         */
        parentPattern = Pattern.compile(patternStrParent);
        /**
         * Compiled pattern for an URI.
         */
        uriPattern = Pattern.compile(patternStrURI);

    }

    protected void activate(BundleContext context) {
        configuration = configurationService.getConfiguration(context.getBundle().getSymbolicName(), EFSDataBackendConfiguration.class);
        saveAsZip = configuration.getUseGZipFormat();
        if (configuration.getEfsStorage().equals("")) {
            configuration.setEfsStorage(configurationService.getPlatformHome() + File.separator + DATA);
        }
    }

    protected void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }

    protected void bindEncapsulatedEFSService(EncapsulatedEFSService newEncapsulatedEFService) {
        encapsulatedEFSService = newEncapsulatedEFService;
    }

    @Override
    public boolean delete(URI uri) {

        boolean deleted = false;

        if (!isURIValid(uri)) {
            throw new IllegalArgumentException("Given URI representing a file to delete is not valid: " + uri);
        }

        try {
            File fileToDelete = new File(getFileStorageRoot().getAbsolutePath() + new File(uri.getRawPath()).getPath());
            if (!fileToDelete.exists()){
                fileToDelete = new File(getFileStorageRoot().getAbsolutePath() + new File(uri.getRawPath()).getPath() + ZIPFORMAT);
            }
            if (!fileToDelete.exists()) {
                LOGGER.warn("Given URI representing a file to delete could not be resolved to an existing path in the file store: "
                        + fileToDelete.getAbsolutePath());
            } else {
                IFileStore fileStore = encapsulatedEFSService.getStore(fileToDelete.toURI());
                fileStore.delete(EFS.NONE, null);
                deleted = true;

                // remove file store parent if there are no more revisions stored
                IFileStore parent = fileStore.getParent();
                if (isParentValid(parent.getName())) {
                    if (parent.childNames(EFS.NONE, null).length == 0) {
                        parent.delete(EFS.NONE, null);
                    }
                }
            } 
        } catch (CoreException e) {
            throw new RuntimeException("File with given URI could not be deleted: " + uri, e);
        }

        return deleted;
    }

    @Override
    public InputStream get(URI uri) {
        IFileStore fileStore = null;

        if (!isURIValid(uri)) {
            throw new IllegalArgumentException("Given URI representing a file to get is not valid: " + uri);
        }

        try {
            boolean isZipped = false;
            File fileToGet = new File(getFileStorageRoot().getAbsolutePath() + new File(uri.getRawPath()).getPath());
            if (!fileToGet.exists()){
                fileToGet = new File(getFileStorageRoot().getAbsolutePath() + new File(uri.getRawPath()).getPath()  + ZIPFORMAT);
                isZipped = true;
            }
            fileStore = encapsulatedEFSService.getStore(fileToGet.toURI());

            if (isZipped){
                return new GZIPInputStream(fileStore.openInputStream(EFS.NONE, null));
            } else {
                return fileStore.openInputStream(EFS.NONE, null);
            }
        } catch (CoreException e) {
            throw new RuntimeException("File with given URI could not be found: " + uri, e);
        } catch (IOException e) {
            throw new RuntimeException("File with given URI could not be found: " + uri, e);
        }
    }

    @Override
    public URI suggestLocation(UUID guid, int revisionNumber) {

        URI newUri;
        try {
            newUri = new URI(SCHEMA_EFS + SCHEME_URI_COMPLETION + SLASH + guid.toString() + SLASH + FILE_NAME + SEPARATOR + revisionNumber);
        } catch (URISyntaxException e) {
            // should never get here
            throw new IllegalArgumentException("Creating URI failed.", e);
        }
        return newUri;
    }

    @Override
    public long put(URI uri, Object object) {

        if (!isURIValid(uri)) {
            throw new IllegalArgumentException("Given URI representing the location to put a file to is not valid: " + uri);
        }

        long writtenBytes = 0;

        if (object instanceof InputStream) {
            InputStream inputStream = (InputStream) object;
            GZIPOutputStream fileGZipOutputStream = null;
            OutputStream fileOutputStream = null;
            IFileStore fileStore = null;
            try {
                File fileToSave;
                IFileStore parent = null;
                if (saveAsZip){
                    fileToSave = new File(getFileStorageRoot().getAbsolutePath() + new File(uri.getRawPath()).getPath() + ZIPFORMAT);
                } else {
                    fileToSave =  new File(getFileStorageRoot().getAbsolutePath() + new File(uri.getRawPath()).getPath());
                  
                }
                fileStore = encapsulatedEFSService.getStore(fileToSave.toURI());
                parent = fileStore.getParent();
                if (parent != null && isParentValid(parent.getName())) {
                    parent.mkdir(0, null);
                }
                if (saveAsZip) {
                    fileGZipOutputStream = new GZIPOutputStream(new FileOutputStream(fileToSave));
                } else {
                    fileOutputStream = fileStore.openOutputStream(EFS.NONE, null);
                }
            } catch (CoreException e) {
                throw new RuntimeException("File with given desired URI could not be written: " + uri, e);
            } catch (IOException e) {
                Logger.getAnonymousLogger().info(e.getMessage());
            }

            try {
                final int minusOne = -1;
                final int bufferSize = 256 * 1024;
                byte[] buffer = new byte[bufferSize];
                int n = 0;
                while (minusOne != (n = inputStream.read(buffer))) {
                    if (!saveAsZip){                   
                        fileOutputStream.write(buffer, 0, n);
                    } else {
                        fileGZipOutputStream.write(buffer, 0, n);
                        fileGZipOutputStream.flush();
                    }
                    writtenBytes += n;
                }

            } catch (IOException e) {
                try {
                    fileStore.delete(EFS.NONE, null);
                    IFileStore parent = fileStore.getParent();
                    if (isParentValid(parent.getName())) {
                        // delete directory if it is empty
                        if (parent.childNames(EFS.NONE, null).length == 0) {
                            parent.delete(EFS.NONE, null);
                        }
                    }
                } catch (CoreException e2) {
                    LOGGER.error("File with given URI for which writing failed could not be deleted: " + uri);
                }
                throw new RuntimeException("File with given desired URI could not be written: " + uri, e);
            } finally {
                try {
                    if (!saveAsZip){
                        fileOutputStream.close();
                    } else {
                        fileGZipOutputStream.flush();
                        fileGZipOutputStream.finish();
                        fileGZipOutputStream.close();
                    }
                } catch (IOException e2) {
                    LOGGER.error("EFS output stream for given URI could not be closed: " + uri, e2);
                }
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    LOGGER.error("Input stream for given URI could not be closed: " + uri, e2);
                }
            }
        } else {
            throw new IllegalArgumentException("Given object to put is not an instance of InputStream: " + object);
        }
        return writtenBytes;
    }

    /**
     * Checks if the given name resembles a valid URI for the parent directory of a persisted file.
     * 
     * @param name
     *            Name to be checked.
     * @return <code>true</code> if name resembles a valid parent directory, <code>false</code> otherwise.
     */
    private boolean isParentValid(String name) {
        // must be like f8f3fe28-7970-4f5d-a7ea-8f611f6aa83c
        String stringToTest = name;
        Matcher matcher = parentPattern.matcher(stringToTest);
        boolean isValue = matcher.matches();
        return isValue;
    }

    /**
     * Checks if the given URI resembles a valid URI for the persisted file.
     * 
     * @param uri
     *            URI to be checked.
     * @return <code>true</code> if URI is valid, <code>false</code> otherwise.
     */
    private boolean isURIValid(URI uri) {
        // must be like /f8f3fe28-7970-4f5d-a7ea-8f611f6aa83c/file-1
        String stringToTest = uri.getPath();
        Matcher matcher = uriPattern.matcher(stringToTest);
        boolean isValue = matcher.matches();
        return isValue;
    }

    private File getFileStorageRoot() {
        return new File(configuration.getEfsStorage()).getAbsoluteFile();
    }
}
