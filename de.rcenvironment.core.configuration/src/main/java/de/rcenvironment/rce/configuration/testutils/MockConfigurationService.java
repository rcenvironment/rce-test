/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.configuration.testutils;

import java.io.File;
import java.util.Map;

import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.configuration.ConfigurationServiceMessageEventListener;

/**
 * Common test/mock implementations of {@link ConfigurationService}. These can be used directly, or
 * can as superclasses for custom mock classes.
 * 
 * Custom mock implementations of {@link ConfigurationService} should use these as superclasses
 * whenever possible to avoid code duplication, and to shield the mock classes from irrelevant API
 * changes.
 * 
 * @author Robert Mischke
 */
public abstract class MockConfigurationService {

    /**
     * A mock implementation of {@link CommunicationService} that throws an exception on every
     * method call. Subclasses for tests should override the methods they expect to be called.
     * 
     * @author Robert Mischke
     */
    public static class ThrowExceptionByDefault implements ConfigurationService {

        private static final String MOCK_INSTANCE_INVOCATION_MESSAGE = "Mock instance invoked";

        @Override
        public void addSubstitutionProperties(String namespace, Map<String, String> properties) {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public <T> T getConfiguration(String identifier, Class<T> clazz) {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public String getAbsolutePath(String identifier, String path) {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public String getPlatformHost() {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public String getPlatformName() {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public boolean getIsWorkflowHost() {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public int getPlatformNumber() {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public String getPlatformHome() {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public String getPlatformTempDir() {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public File getPlatformLogFilesDir() {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public String getConfigurationArea() {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public void addErrorListener(ConfigurationServiceMessageEventListener listener) {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }

        @Override
        public void removeErrorListener(ConfigurationServiceMessageEventListener listener) {
            throw new UnsupportedOperationException(MOCK_INSTANCE_INVOCATION_MESSAGE);
        }
    }
}
