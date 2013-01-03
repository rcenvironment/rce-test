/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.start.common.validation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

import de.rcenvironment.core.start.common.Platform;
import de.rcenvironment.core.start.common.validation.internal.PlatformValidatorsRegistry;

/**
 * A manager class that manages the validation of the RCE platform thru the registered {@link PlatformValidator}s.
 *
 * @author Christian Weiss
 */
public class PlatformValidationManager {
    
    private PlatformValidatorsRegistry validatorsRegistry;
    /**
     * Returns the {@link PlatformValidatorsRegistry}.
     * 
     * @return the {@link PlatformValidatorsRegistry}
     */
    protected synchronized PlatformValidatorsRegistry getValidatorsRegistry() {
        if (validatorsRegistry == null) {
            validatorsRegistry = PlatformValidatorsRegistry.getDefaultInstance();
        }
        return validatorsRegistry;
    }
    
    /**
     * Sets the {@link PlatformValidatorsRegistry}.
     * 
     * @param validatorsRegistry the {@link PlatformValidatorsRegistry}
     */
    public void setValidatorsRegistry(PlatformValidatorsRegistry validatorsRegistry) {
        if (this.validatorsRegistry != null) {
            throw new IllegalStateException();
        }
        this.validatorsRegistry = validatorsRegistry;
    }

    /**
     * Validates the RCE platform.
     * 
     * @return the state of the RCE platform
     */
    public boolean validate() {
        final List<PlatformMessage> messages = new LinkedList<PlatformMessage>();
        final List<PlatformValidator> validators = getValidatorsRegistry().getValidators();
        for (final PlatformValidator validator : validators) {
            try {
                final Collection<PlatformMessage> validationMessages = validator.validatePlatform();
                messages.addAll(validationMessages);
            } catch (RuntimeException ex) {
                messages.add(new PlatformMessage(
                        PlatformMessage.Type.ERROR,
                        "de.rcenvironment.rce.gui",
                        String.format(
                                "The execution of the validator '%s' caused an exception ('%s').",
                                validator.getClass().getName(),
                                ex.getLocalizedMessage())));
            }
        }
        if (messages.size() > 0) {
            // the last encountered error is cached, as only the last error
            // needs to be displayed in BLOCK style
            PlatformMessage lastError = null;
            boolean hasError = false;
            for (final PlatformMessage error : messages) {
                if (lastError != null) {
                    handleError(lastError, StatusManager.SHOW);
                }
                if (error.getType() == PlatformMessage.Type.ERROR) {
                    hasError = true;
                }
                lastError = error;
            }
            handleError(lastError, StatusManager.BLOCK);
            return !hasError;
        }
        return true;
    }

    protected void handleError(final PlatformMessage error, final int style) {
        final String errorMessageLabel = String.format("%s: %s", error.getType(), error.getMessage());
        final IStatus status = new Status(Status.ERROR, error.getBundleSymbolicName(),
                errorMessageLabel);
        if (Platform.isHeadless()) {
            System.err.println(errorMessageLabel);
        } else {
            StatusManager.getManager().handle(status, style);
        }
    }

}
