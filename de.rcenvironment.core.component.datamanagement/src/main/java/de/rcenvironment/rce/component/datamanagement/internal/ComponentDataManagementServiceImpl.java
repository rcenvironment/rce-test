/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.datamanagement.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.datamanagement.ComponentDataManagementService;
import de.rcenvironment.rce.datamanagement.DataManagementService;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * Implementation of {@link ComponentDataManagemenService}.
 * @author Sascha Zur
 */
public class ComponentDataManagementServiceImpl implements ComponentDataManagementService{

    private static DataManagementService dataManagementService;

    /**
     * Only used for OSGi DS.
     */
    @Deprecated
    public ComponentDataManagementServiceImpl() {}
    
    @Override
    public String createTaggedReferenceFromLocalFile(ComponentInstanceInformation instanceInformation, 
        User user, File file, String filename) throws AuthorizationException, IOException{
        MetaDataSet mds = new MetaDataSet();

        ComponentDataManagementUtil.setComponentMetaData(mds, instanceInformation);

        // if filename parameter is not null, tag the reference with a filename
        if (filename != null) {
            String effectiveFilename;
            if (filename == ComponentDataManagementService.SAME_FILENAME) {
                // "magic" constant: use the filename of the provided file
                effectiveFilename = file.getName();
            } else {
                // otherwise, use provided custom filename
                effectiveFilename = filename;
            }
            ComponentDataManagementUtil.setAssociatedFilename(mds, effectiveFilename);
        }

        return dataManagementService.createReferenceFromLocalFile(user, file, mds, instanceInformation.getDefaultStoragePlatform());
    }

    @Override
    public String createTaggedReferenceFromString(ComponentInstanceInformation instanceInformation, 
        User user, String object) throws IOException {
        MetaDataSet mds = new MetaDataSet();

        ComponentDataManagementUtil.setComponentMetaData(mds, instanceInformation);

        return dataManagementService.createReferenceFromString(user, object, mds, instanceInformation.getDefaultStoragePlatform());
    }

    @Override
    @Deprecated
    public void copyReferenceToLocalFile(User user, String reference, File targetFile)
        throws IOException {
        dataManagementService.copyReferenceToLocalFile(user, reference, targetFile);
    }

    @Override
    public void copyReferenceToLocalFile(User user, String reference, File targetFile, Collection<PlatformIdentifier> platforms) 
        throws IOException {
        
        final Iterator<PlatformIdentifier> iter = platforms.iterator();
        while (iter.hasNext()) {
            try {
                dataManagementService.copyReferenceToLocalFile(user, reference, targetFile, (PlatformIdentifier) iter.next());
            } catch (final FileNotFoundException e) {
                // most of the time, this will fail (catching on a regular basis is not good
                // practice, but works for now)
                int x = 0;
                x = x + 1;
            }
        }
    }

    @Override
    // TODO check: why just a single platform and not a collection? -- misc_ro
    // could be a usecase where you know the platform, in direct file transfer, P2P scenarios
    public void copyReferenceToLocalFile(User user, String reference, File targetFile, PlatformIdentifier platform) throws IOException {
        dataManagementService.copyReferenceToLocalFile(user, reference, targetFile, platform);
    }
    @Override
    public String retrieveStringFromReference(User user, String reference, Collection<PlatformIdentifier> platforms) throws IOException {
        return dataManagementService.retrieveStringFromReference(user, reference, platforms);
    }

    @Override
    public void addHistoryDataPoint(ComponentInstanceInformation instanceInformation, User user, 
        Serializable historyData, String userInfoText) throws IOException {
        // TODO temporary bridge code
        new WorkflowHistoryServiceImpl(user).addHistoryDataPoint(historyData, userInfoText, instanceInformation);
    }


    protected void bindDataManagementService(DataManagementService newDataManagementService) {
        dataManagementService = newDataManagementService;
    }

    protected void unbindDataManagementService(DataManagementService newDataManagementService) {
        dataManagementService = null;
    }
}
