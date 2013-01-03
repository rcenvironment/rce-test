/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.validators.internal;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.rcenvironment.core.start.common.validation.PlatformMessage;
import de.rcenvironment.core.start.common.validation.PlatformValidator;
import de.rcenvironment.rce.configuration.ConfigurationService;


/**
 * Validates RCE platform home directory. Check if it is writable.
 * 
 * @author Christian Weiss
 *
 */
public class DirectoryValidator implements PlatformValidator {
    
    private static ConfigurationService configurationService;
    
    private static CountDownLatch configurationServiceLatch = new CountDownLatch(1);
    
    @Deprecated
    public DirectoryValidator() {
        // do nothing
    }
    
    protected void bindConfigurationService(final ConfigurationService newConfigurationService) {
        DirectoryValidator.configurationService = newConfigurationService;
        configurationServiceLatch.countDown();
    }

    @Override
    public Collection<PlatformMessage> validatePlatform() {
        final Collection<PlatformMessage> result = new LinkedList<PlatformMessage>();
        final String rceDictoryPath = getRceDirectoryPath();
        if (rceDictoryPath == null) {
            result.add(new PlatformMessage(PlatformMessage.Type.ERROR,
                    ValidatorsBundleActivator.bundleSymbolicName,
                    Messages.directoryNoConfigurationService + rceDictoryPath));
        } else {
            final File rceDirectory = new File(rceDictoryPath);
            if (!rceDirectory.exists() || !rceDirectory.isDirectory()) {
                result.add(new PlatformMessage(PlatformMessage.Type.ERROR,
                        ValidatorsBundleActivator.bundleSymbolicName, Messages.directoryRceFolderDoesNotExist));
            } else if (!rceDirectory.canRead() || !rceDirectory.canWrite()) {
                result.add(new PlatformMessage(PlatformMessage.Type.ERROR,
                        ValidatorsBundleActivator.bundleSymbolicName,
                        Messages.directoryRceFolderNotReadWriteAble + rceDictoryPath));
            }
        }
        return result;
    }
    
    protected String getRceDirectoryPath() {
        final ConfigurationService boundConfigurationService = getConfigurationService();
        final String result;
        if (boundConfigurationService != null) {
            result = boundConfigurationService.getPlatformHome();
        } else {
            result = null;
        }
        return result;
    }

    /*
     * Please note: There is not unit test for this method, because too much OSGi
     * API mocking would be needed for that little bit of code. It is tested
     * well, because it runs every time RCE starts up.
     */
    protected ConfigurationService getConfigurationService() {
        try {
            DirectoryValidator.configurationServiceLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return DirectoryValidator.configurationService;
    }

}
