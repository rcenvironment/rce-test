/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.testutils;

import java.util.Set;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentityInformation;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.communication.internal.CommunicationConfiguration;

/**
 * Default stub for {@link PlatformService}. All methods with a return value respond with the
 * default field value for this type (null, 0, false, '\u0000', ...).
 * 
 * This class (and subclasses of it) is intended for cases where an instance of
 * {@link PlatformService} is required to set up the test, but where the exact calls to this
 * instance are not relevant. If they are relevant and should be tested, create a mock instance
 * instead (for example, with the EasyMock library).
 * 
 * @author Robert Mischke
 */
public class PlatformServiceDefaultStub implements PlatformService {

    @Override
    public PlatformIdentifier getPlatformIdentifier() {
        return null;
    }

    @Override
    public PlatformIdentifier getLegacyPlatformIdentifier() {
        return null;
    }

    @Override
    public PlatformIdentifier getPersistentIdPlatformIdentifier() {
        return null;
    }

    @Override
    public PlatformIdentityInformation getIdentityInformation() {
        return null;
    }

    @Override
    public boolean isLocalPlatform(PlatformIdentifier platformIdentifier) {
        return false;
    }

    @Override
    public Set<PlatformIdentifier> getRemotePlatforms() {
        return null;
    }

    @Override
    public String getServiceBindAddress() {
        return null;
    }

    @Override
    public CommunicationConfiguration getConfiguration() {
        return null;
    }

}
