/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.validators.internal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.osgi.service.component.ComponentContext;

import de.rcenvironment.core.start.common.validation.PlatformMessage;
import de.rcenvironment.core.start.common.validation.PlatformValidator;
import de.rcenvironment.rce.configuration.ConfigurationServiceMessage;
import de.rcenvironment.rce.configuration.ConfigurationServiceMessageEvent;
import de.rcenvironment.rce.configuration.ConfigurationServiceMessageEventListener;


/**
 * Validator which gets notified about parsing errors in the
 * {@link de.rcenvironment.rce.configuration.ConfigurationService} and stores
 * those error messages to report them when the RCE validation is run.
 * 
 * @author Christian Weiss
 * 
 */
public class ConfigurationServiceParsingValidator implements
        PlatformValidator, ConfigurationServiceMessageEventListener {

    private static final List<ConfigurationServiceMessageEvent> ERROR_EVENTS = new LinkedList<ConfigurationServiceMessageEvent>();

    @Deprecated
    public ConfigurationServiceParsingValidator() {
        // do nothing
    }

    protected void activate(final ComponentContext context) {
        // do nothing
    }

    @Override
    public void handleConfigurationServiceError(
            final ConfigurationServiceMessageEvent error) {
        ERROR_EVENTS.add(error);
    }
    
    /* default */ void clear() {
        ERROR_EVENTS.clear();
    }

    @Override
    public Collection<PlatformMessage> validatePlatform() {
        final Collection<PlatformMessage> result = new LinkedList<PlatformMessage>();
        for (final ConfigurationServiceMessageEvent errorEvent : ERROR_EVENTS) {
            final ConfigurationServiceMessage error = errorEvent.getError();
            result.add(new PlatformMessage(PlatformMessage.Type.WARNING,
                    ValidatorsBundleActivator.bundleSymbolicName, error.getMessage()));
        }
        return result;
    }

}
