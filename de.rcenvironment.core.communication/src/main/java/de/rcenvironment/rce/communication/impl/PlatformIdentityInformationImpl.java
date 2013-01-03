/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.impl;

import de.rcenvironment.rce.communication.PlatformIdentityInformation;

/**
 * Default immutable {@link PlatformIdentityInformation} implementation.
 * 
 * @author Robert Mischke
 */
public class PlatformIdentityInformationImpl implements PlatformIdentityInformation, Cloneable {

    private static final long serialVersionUID = -7628040480675636721L;

    private final String persistentId;

    private final String encodedPublicKey;

    private final String displayName;

    private boolean isWorkflowHost;

    public PlatformIdentityInformationImpl(String persistentId, String encodedPublicKey, String displayName, boolean isWorkflowHost) {
        this.persistentId = persistentId;
        this.encodedPublicKey = encodedPublicKey;
        this.displayName = displayName;
        this.isWorkflowHost = isWorkflowHost;
    }

    @Override
    public String getPersistentNodeId() {
        return persistentId;
    }

    @Override
    public String getEncodedPublicKey() {
        return encodedPublicKey;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean getIsWorkflowHost() {
        return isWorkflowHost;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
