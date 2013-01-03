/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.datamanagement.stateful;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.datamanagement.ComponentDataManagementService;

/**
 * The default "simple" version of the {@link StatefulComponentDataManagementService}. Clients can
 * simply instantiate this class and use it without configuring OSGi declarative service metadata
 * themselves.
 * 
 * @author Robert Mischke
 * @author Sascha Zur
 * 
 */
public class SimpleComponentDataManagementService implements StatefulComponentDataManagementService {

    private static ComponentDataManagementService componentDataManagementService;
    /**
     * The user from the component.
     */
    private User user = null;

    private ComponentInstanceInformation componentInformation;

    /**
     * Only used for OSGi DS.
     */
    @Deprecated
    public SimpleComponentDataManagementService() {}

    /**
     * Default constructor that tries to acquire all data management services automatically.
     * 
     * @throws AuthenticationException if the {@link Session} proxy certificate could not be
     *         acquired
     */
    public SimpleComponentDataManagementService(ComponentInstanceInformation componentInformation) {
        this.componentInformation = componentInformation;
        this.user = componentInformation.getProxyCertificate();
    }

    @Override
    @Deprecated
    public void copyReferenceToLocalFile(String reference, File targetFile) throws IOException {
        componentDataManagementService.copyReferenceToLocalFile(user, reference, targetFile);
    }

    @Override
    public void copyReferenceToLocalFile(String reference, File targetFile, Collection<PlatformIdentifier> platforms) throws IOException {
        componentDataManagementService.copyReferenceToLocalFile(user, reference, targetFile, platforms);
    }

    @Override
    public void copyReferenceToLocalFile(String reference, File targetFile, PlatformIdentifier platform) throws IOException {
        componentDataManagementService.copyReferenceToLocalFile(user, reference, targetFile, platform);
    }

    @Override
    public String retrieveStringFromReference(String reference, Collection<PlatformIdentifier> platforms) throws IOException {
        return componentDataManagementService.retrieveStringFromReference(user, reference, platforms);
    }

    @Override
    public String createTaggedReferenceFromLocalFile(File file, String filename) throws IOException {
        return componentDataManagementService.createTaggedReferenceFromLocalFile(componentInformation, user, file, filename);
    }

    @Override
    public String createTaggedReferenceFromString(String object) throws IOException {
        return componentDataManagementService.createTaggedReferenceFromString(componentInformation, user, object);
    }

    @Override
    public void addHistoryDataPoint(Serializable historyData, String userInfoText) throws IOException {
        componentDataManagementService.addHistoryDataPoint(componentInformation, user, historyData, userInfoText);
    }

    protected void bindComponentDataManagementService(ComponentDataManagementService newComponentDataManagementService) {
        componentDataManagementService = newComponentDataManagementService;
    }

    protected void unbindComponentDataManagementService(ComponentDataManagementService newDataManagementService) {
        componentDataManagementService = null;
    }

}
