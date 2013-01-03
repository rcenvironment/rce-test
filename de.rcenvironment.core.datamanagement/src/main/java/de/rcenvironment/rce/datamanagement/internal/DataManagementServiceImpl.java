/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.datamanagement.DataManagementService;
import de.rcenvironment.rce.datamanagement.DistributedFileDataService;
import de.rcenvironment.rce.datamanagement.DistributedQueryService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * Default implementation of {@link DataManagementService}.
 * 
 * @author Robert Mischke
 */
public class DataManagementServiceImpl implements DataManagementService {

    private static final String REFERENCE_NOT_FOUND_MESSAGE = "No such data entry (id='%s').";

    private DistributedFileDataService fileDataService;

    private DistributedQueryService queryService;

    @Override
    public String createReferenceFromLocalFile(User certificate, File file, MetaDataSet additionalMetaData) throws IOException,
            AuthorizationException {
        // delegate with "null" as platform
        return createReferenceFromLocalFile(certificate, file, additionalMetaData, null);
    }

    @Override
    public String createReferenceFromLocalFile(User certificate, File file, MetaDataSet additionalMetaData,
            PlatformIdentifier platformIdentifier) throws IOException, AuthorizationException {
        if (additionalMetaData == null) {
            additionalMetaData = new MetaDataSet();
        }

        InputStream inputStream = new FileInputStream(file);
        try {
            DataReference dataRef = fileDataService. // break line here for Checkstyle
            newReferenceFromStream(certificate, inputStream, additionalMetaData, platformIdentifier);
            return dataRef.getIdentifier().toString();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see de.rcenvironment.rce.datamanagement.DataManagementService#createReferenceFromObject(de.rcenvironment.rce.authentication.User,
     *      java.io.Serializable, de.rcenvironment.rce.datamanagement.commons.MetaDataSet,
     *      de.rcenvironment.rce.communication.PlatformIdentifier)
     */
    @Override
    public String createReferenceFromString(User user, String object, MetaDataSet additionalMetaData, // CheckStyle
            PlatformIdentifier platformIdentifier) throws IOException, AuthorizationException {
        if (additionalMetaData == null) {
            additionalMetaData = new MetaDataSet();
        }

        InputStream inputStream = IOUtils.toInputStream(object);
        try {
            DataReference dataRef = fileDataService. // break line here for Checkstyle
            newReferenceFromStream(user, inputStream, additionalMetaData, platformIdentifier);
            return dataRef.getIdentifier().toString();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public void copyReferenceToLocalFile(User user, String reference, File targetFile) throws IOException, AuthorizationException {
        copyReferenceToLocalFile(user, reference, targetFile, (PlatformIdentifier) null);
    }

    @Override
    public void copyReferenceToLocalFile(User user, String reference, File targetFile, // CheckStyle
            PlatformIdentifier platformIdentifier) throws IOException, AuthorizationException {

        UUID uuid = UUID.fromString(reference);
        DataReference dataRef;
        if (platformIdentifier == null) {
            dataRef = queryService.getReference(user, uuid);
        } else {
            dataRef = queryService.getReference(user, uuid, platformIdentifier);
        }
        if (dataRef == null) {
            throw new FileNotFoundException(String.format(REFERENCE_NOT_FOUND_MESSAGE, reference));
        }
        InputStream dataMgmtStream = fileDataService.getStreamFromDataReference(user, dataRef, DataReference.HEAD_REVISION);
        try {
            FileUtils.copyInputStreamToFile(dataMgmtStream, targetFile);
        } finally {
            IOUtils.closeQuietly(dataMgmtStream);
        }
    }

    @Override
    public void copyReferenceToLocalFile(User user, String reference, File targetFile, // CheckStyle
            Collection<PlatformIdentifier> platforms) throws IOException, AuthorizationException {

        final UUID uuid = UUID.fromString(reference);
        DataReference dataRef;
        if ((platforms == null) || (platforms.size() == 0)) {
            dataRef = queryService.getReference(user, uuid);
        } else {
            dataRef = queryService.getReference(user, uuid, platforms);
        }
        if (dataRef == null) {
            throw new FileNotFoundException(String.format(REFERENCE_NOT_FOUND_MESSAGE, reference));
        }
        InputStream dataMgmtStream = fileDataService.getStreamFromDataReference(user, dataRef, DataReference.HEAD_REVISION);
        try {
            FileUtils.copyInputStreamToFile(dataMgmtStream, targetFile);
        } finally {
            IOUtils.closeQuietly(dataMgmtStream);
        }
    }

    @Override
    public String retrieveStringFromReference(User user, String reference, Collection<PlatformIdentifier> platforms) throws IOException,
            AuthorizationException {
        // TODO extract reference to stream resolution into separate method?
        final UUID uuid = UUID.fromString(reference);
        DataReference dataRef;
        if ((platforms == null) || (platforms.size() == 0)) {
            dataRef = queryService.getReference(user, uuid);
        } else {
            dataRef = queryService.getReference(user, uuid, platforms);
        }
        if (dataRef == null) {
            throw new FileNotFoundException(String.format(REFERENCE_NOT_FOUND_MESSAGE, reference));
        }
        InputStream dataMgmtStream = fileDataService.getStreamFromDataReference(user, dataRef, DataReference.HEAD_REVISION);
        try {
            return IOUtils.toString(dataMgmtStream);
        } finally {
            IOUtils.closeQuietly(dataMgmtStream);
        }
    }

    /**
     * OSGi-DS setter.
     * 
     * @param fileDataService
     *            The fileDataService to set.
     */
    protected void bindFileDataService(DistributedFileDataService newValue) {
        this.fileDataService = newValue;
    }

    /**
     * OSGi-DS setter.
     * 
     * @param queryService
     *            The queryService to set.
     */
    protected void bindQueryService(DistributedQueryService newValue) {
        this.queryService = newValue;
    }

}
