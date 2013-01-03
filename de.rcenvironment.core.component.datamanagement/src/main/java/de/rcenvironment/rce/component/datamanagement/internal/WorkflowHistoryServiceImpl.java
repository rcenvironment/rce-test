/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.datamanagement.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.datamanagement.history.HistoryMetaDataKeys;
import de.rcenvironment.rce.datamanagement.SimpleFileDataService;
import de.rcenvironment.rce.datamanagement.commons.MetaData;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * Implements history-related methods.
 * 
 * TODO merge with {@link ComponentDataManagementService}?
 * 
 * @author Robert Mischke
 */
public class WorkflowHistoryServiceImpl {

    private static final MetaData METADATA_CLASS_NAME = new MetaData(
            HistoryMetaDataKeys.HISTORY_OBJECT_CLASS_NAME, true, true);

    private static final MetaData METADATA_USER_INFO_TEXT = new MetaData(
            HistoryMetaDataKeys.HISTORY_USER_INFO_TEXT, true, true);

    private static final MetaData METADATA_HISTORY_TIMESTAMP = new MetaData(
            HistoryMetaDataKeys.HISTORY_TIMESTAMP, true, true);

    /**
     * The proxy certificate from the component.
     */
    private User certificate = null;

    /**
     * FileDataService for storing/loading resources to the data management.
     */
    private SimpleFileDataService dataService = null;

    /**
     * Default constructor that tries to acquire all data management services automatically.
     * 
     * @param certificate the certificate to acquire the data management services with
     */
    public WorkflowHistoryServiceImpl(User certificate) {
        this.certificate = certificate;
        dataService = new SimpleFileDataService(certificate);
    }

    /**
     * Creates a history data point.
     * 
     * TODO better doc; merge with {@link ComponentDataManagementService}?
     * 
     * @param historyData the history data to store
     * @param userInfoText the user information text to associate; usually used as display title
     * @param componentInformation the {@link ComponentInstanceInformation} to read metadata and the
     *        proxy certificate from
     * @throws IOException on a data management or I/O error
     */
    public void addHistoryDataPoint(Serializable historyData,
            String userInfoText, ComponentInstanceInformation componentInformation) throws IOException {

        MetaDataSet mds = new MetaDataSet();

        ComponentDataManagementUtil.setComponentMetaData(mds,
                componentInformation);

        setHistoryMetaData(mds, historyData, userInfoText);

        // convert Serializable -> InputStream;
        // as all streams are memory-only, cleanup is left to GC
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baOutputStream);
        oos.writeObject(historyData);
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(
                baOutputStream.toByteArray());

        // create reference
        dataService.newReferenceFromStream(baInputStream, mds, componentInformation.getDefaultStoragePlatform());
    }

    private static void setHistoryMetaData(MetaDataSet mds,
            Serializable historyData, String userInfoText) {
        mds.setValue(METADATA_CLASS_NAME, historyData.getClass().getCanonicalName());
        mds.setValue(METADATA_USER_INFO_TEXT, userInfoText);
        mds.setValue(METADATA_HISTORY_TIMESTAMP, Long.toString(System.currentTimeMillis()));
    }
}
