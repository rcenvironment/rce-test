/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.configuration.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.rce.configuration.ConfigurationServiceMessage;
import de.rcenvironment.rce.configuration.ConfigurationServiceMessageEvent;
import de.rcenvironment.rce.configuration.ConfigurationServiceMessageEventListener;
import de.rcenvironment.rce.configuration.discovery.bootstrap.DiscoveryBootstrapService;
import de.rcenvironment.rce.configuration.discovery.bootstrap.DiscoveryConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TestCases for JSONConfigurationServiceTest.java.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 * @author Robert Mischke
 */
public class JSONConfigurationServiceTest {

    /** Name of the test configuration file. */
    public static final String ID = "de.rcenvironment.rce.configuration.test";

    private static final String OSGI_INSTALL_AREA = "osgi.install.area";

    private static final String CONFIG_DIR = "de.rcenvironment.rce.configuration.dir";


    /** Content of the test configuration file. */
    private static final String CONTENT = "{\n  \"booleanValue\": true\n}";

    /** Default string value. */
    private static final String STRING = "123";

    private static final String BUNDLE_SYMBOLIC_NAME = "de.rcenvironment.rce.configuration";

    /**
     * Common test setup.
     * 
     * @throws IOException on TempFileUtils failure
     */
    @Before
    public void setup() throws IOException {
        TempFileUtils.getDefaultInstance().setDefaultTestRootDir();
    }

    /**
     * Test default behavior, configuration in instance home.
     */
    @Test
    public void testInstance() {
        File tempDir = createTempDir();
        System.setProperty(OSGI_INSTALL_AREA, tempDir.getAbsolutePath() + File.separator);
        File rceDir = new File(tempDir.getAbsoluteFile() + File.separator + "configuration");
        rceDir.mkdirs();
        createTestConfiguration(rceDir, ID, CONTENT);

        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        DummyConfiguration configuration = service.getConfiguration(ID, DummyConfiguration.class);
        assertEquals(configuration.isBooleanValue(), true);
        assertEquals(configuration.getStringValue(), STRING);

        removeTempDir(tempDir);
    }

    /**
     * Test default behavior, configuration in user home.
     */
    @Test
    public void testUserHome() {
        File tempDir = createTempDir();
        System.setProperty("user.home", tempDir.getAbsolutePath());
        File rceDir = new File(tempDir.getAbsoluteFile() + File.separator + ".rce" + File.separator + "configuration");
        rceDir.mkdirs();
        createTestConfiguration(rceDir, ID, CONTENT);

        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        DummyConfiguration configuration = service.getConfiguration(ID, DummyConfiguration.class);
        assertEquals(configuration.isBooleanValue(), true);
        assertEquals(configuration.getStringValue(), STRING);

        removeTempDir(tempDir);
    }

    /**
     * Test configuration directory specified by property.
     */
    @Test
    public void testProperty() {
        File tempDir = createTempDir();
        System.setProperty(CONFIG_DIR, tempDir.getAbsolutePath());
        createTestConfiguration(tempDir, ID, CONTENT);

        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        DummyConfiguration configuration = service.getConfiguration(ID, DummyConfiguration.class);
        assertEquals(configuration.isBooleanValue(), true);
        assertEquals(configuration.getStringValue(), STRING);

        removeTempDir(tempDir);
    }

    /**
     * Test broken configuration, default should be provided.
     */
    @Test
    public void testBroken() {
        File tempDir = createTempDir();
        System.setProperty(OSGI_INSTALL_AREA, tempDir.getAbsolutePath() + File.separator);
        createTestConfiguration(tempDir, ID, "asdkf");

        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        DummyConfiguration configuration = service.getConfiguration(ID, DummyConfiguration.class);
        assertEquals(configuration.isBooleanValue(), false);
        assertEquals(configuration.getStringValue(), STRING);

        removeTempDir(tempDir);
    }

    /**
     * Test missing configuration, default should be provided.
     */
    @Test
    public void testMissing() {
        File tempDir = createTempDir();
        System.setProperty(CONFIG_DIR, tempDir.getAbsolutePath());

        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        DummyConfiguration configuration = service.getConfiguration(ID, DummyConfiguration.class);
        assertEquals(configuration.isBooleanValue(), false);
        assertEquals(configuration.getStringValue(), STRING);

        removeTempDir(tempDir);
    }

    /** Test. */
    @Test
    public void testGetAbsolutePath() {
        File tempDir = createTempDir();

        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        assertEquals(tempDir.getAbsolutePath(), service.getAbsolutePath(ID, tempDir.getAbsolutePath()));

        System.setProperty(CONFIG_DIR, tempDir.getAbsolutePath());

        assertEquals(tempDir.getAbsolutePath() + File.separator + ID + File.separator + tempDir.getName(),
            service.getAbsolutePath(ID, tempDir.getName()));

        removeTempDir(tempDir);
    }

    /** Test. */
    @Test
    public void testGetPlatformHomeForSuccess() {
        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        service.activate(getBundleContextMock());
        ConfigurationConfiguration configuration = service.getConfiguration(ID, ConfigurationConfiguration.class);
        assertEquals(service.getPlatformHome(), configuration.getPlatformHome());
    }

    /** Test. */
    @Test
    public void testGetPlatformTempDirForSuccess() {
        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        service.activate(getBundleContextMock());
        assertTrue(new File(service.getPlatformTempDir()).isDirectory());
        assertTrue(new File(service.getPlatformTempDir()).canWrite());
    }

    /** Test. */
    @Test
    public void testGetPlatformLogDirForSuccess() {
        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        service.activate(getBundleContextMock());
        File platformLogFilesDir = service.getPlatformLogFilesDir();
        // note: this check relies on path naming conventions
        assertTrue(platformLogFilesDir.getAbsolutePath().contains("log"));
        assertTrue(platformLogFilesDir.isDirectory());
        assertTrue(platformLogFilesDir.canWrite());
    }

    /** Test. */
    @Test
    public void testGetHostnameForSuccess() {
        File tempDir = createTempDir();
        System.setProperty(CONFIG_DIR, tempDir.getAbsolutePath());

        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        service.activate(getBundleContextMock());
        ConfigurationConfiguration config = new ConfigurationConfiguration();
        assertEquals(service.getPlatformHost(), config.getHost());

        removeTempDir(tempDir);
    }

    /** Test. */
    @Test
    public void testGetPlatformNumberForSuccess() {
        File tempDir = createTempDir();
        System.setProperty(CONFIG_DIR, tempDir.getAbsolutePath());

        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        service.activate(getBundleContextMock());
        ConfigurationConfiguration config = new ConfigurationConfiguration();
        assertEquals(service.getPlatformNumber(), config.getPlatformNumber());

        removeTempDir(tempDir);
    }

    /** Test. */
    @Test
    public void testGetPlatformNameForSuccess() {
        File tempDir = createTempDir();
        System.setProperty(CONFIG_DIR, tempDir.getAbsolutePath());

        JSONConfigurationService service = createTestInstanceOfConfigurationService();
        service.activate(getBundleContextMock());
        ConfigurationConfiguration config = new ConfigurationConfiguration();
        assertEquals(service.getPlatformName(), config.getPlatformName());

        removeTempDir(tempDir);
    }

    /** Test. */
    @Test
    public void testAddErrorListenerSuccess() {
        final JSONConfigurationService service = new JSONConfigurationService();
        final ConfigurationServiceMessageEventListener listener = EasyMock.createMock(ConfigurationServiceMessageEventListener.class);
        listener.handleConfigurationServiceError((ConfigurationServiceMessageEvent) EasyMock.anyObject());
        EasyMock.replay(listener);
        service.addErrorListener(listener);
        final String messageContentString = "message";
        service.fireErrorEvent(new ConfigurationServiceMessage(messageContentString));
        EasyMock.verify(listener);
    }

    /** Test. */
    @Test
    public void testAddErrorListenerFailure() {
        final JSONConfigurationService service = new JSONConfigurationService();
        try {
            service.addErrorListener(null);
            Assert.fail();
        } catch (NullPointerException ok) {
            ok = null;
        }
    }

    /** Test. */
    @Test
    public void testRemoveErrorListenerSuccess() {
        final JSONConfigurationService service = new JSONConfigurationService();
        final ConfigurationServiceMessageEventListener listener = EasyMock.createMock(ConfigurationServiceMessageEventListener.class);
        listener.handleConfigurationServiceError((ConfigurationServiceMessageEvent) EasyMock.anyObject());
        EasyMock.expectLastCall().times(3);
        EasyMock.replay(listener);
        // register the listener and assert it is registered and the handler gets invoked multiple
        // times without being unregistered
        service.addErrorListener(listener);
        final String messageContentString = "message";
        service.fireErrorEvent(new ConfigurationServiceMessage(messageContentString));
        service.fireErrorEvent(new ConfigurationServiceMessage(messageContentString));
        service.fireErrorEvent(new ConfigurationServiceMessage(messageContentString));
        // remove the listener and make sure it does not get invoked any more
        service.removeErrorListener(listener);
        service.fireErrorEvent(new ConfigurationServiceMessage(messageContentString));
        EasyMock.verify(listener);
    }

    /** Test. */
    @Test
    public void testRemoveErrorListenerFailure() {
        final JSONConfigurationService service = new JSONConfigurationService();
        try {
            service.removeErrorListener(null);
            Assert.fail();
        } catch (NullPointerException ok) {
            ok = null;
        }
    }

    /** Test. */
    @Test
    public void testGetConfigurationParsingErrors() {
        final JSONCS service = new JSONCS();
        /**
         * {@link ConfigurationServiceMessageEventListener} implementation caching the most recent
         * error message.
         */
        class CachingListener implements ConfigurationServiceMessageEventListener {

            private String lastErrorMessage;

            @Override
            public void handleConfigurationServiceError(ConfigurationServiceMessageEvent error) {
                lastErrorMessage = error.getError().getMessage();
            }
        }
        final CachingListener listener = new CachingListener();
        service.addErrorListener(listener);
        // test parsing error
        service.setExceptionToThrow(new JsonParseException("parsing error", null));
        service.getConfiguration("", Object.class);
        Assert.assertTrue(!listener.lastErrorMessage.isEmpty());
        // test mapping error
        service.setExceptionToThrow(new JsonMappingException("mapping error", (JsonLocation) null));
        service.getConfiguration("", Object.class);
        Assert.assertTrue(!listener.lastErrorMessage.isEmpty());
    }

    /** Test. */
    @Test
    public void testGetConfigurationFailure() {
        final JSONConfigurationService service = new JSONConfigurationService();
        /** Test. */
        final class Test {

            private Test() {
                // do nothing
            }
        }
        try {
            service.getConfiguration("test", Test.class);
            Assert.fail();
        } catch (RuntimeException e) {
            if (!(e.getCause() instanceof InstantiationException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Test for addSubstitutionProperties() and getConfiguration().
     * 
     * @throws IOException on I/O errors
     */
    @Test
    public void testGetPropertySubstitution() throws IOException {

        String configFileBasename = "temp.unittest";
        String testNamespace = "testns";

        File tempDir = TempFileUtils.getDefaultInstance().createManagedTempDir();
        System.setProperty(CONFIG_DIR, tempDir.getAbsolutePath());
        File testConfigFile = new File(tempDir, configFileBasename + ".json");
        if (testConfigFile.exists()) {
            Assert.fail("Unexpected state: File " + testConfigFile.getAbsolutePath() + " already exists");
        }

        final JSONConfigurationService service = new JSONConfigurationService();

        DummyConfiguration config;
        Map<String, String> testProperties;

        // check basic property file reading
        FileUtils.writeStringToFile(testConfigFile, "{ \"stringValue\": \"hardcoded\" }");
        config = service.getConfiguration(configFileBasename, DummyConfiguration.class);
        Assert.assertEquals("No-substitution test failed", "hardcoded", config.getStringValue());
        // test property file reading with missing property

        // test property file reading with defined property not containing quotes
        FileUtils.writeStringToFile(testConfigFile, "{ \"stringValue\": \"${testns:unquoted}\" }");
        testProperties = new HashMap<String, String>();
        testProperties.put("unquoted", "unquotedValue");
        service.addSubstitutionProperties(testNamespace, testProperties);
        config = service.getConfiguration(configFileBasename, DummyConfiguration.class);
        Assert.assertEquals("Unquoted value test failed", "unquotedValue", config.getStringValue());

        // test property file reading with defined property containing quotes
        FileUtils.writeStringToFile(testConfigFile, "{ \"stringValue\": ${testns:quoted} }");
        testProperties = new HashMap<String, String>();
        testProperties.put("quoted", "\"quotedValue\"");
        service.addSubstitutionProperties(testNamespace, testProperties);
        config = service.getConfiguration(configFileBasename, DummyConfiguration.class);
        Assert.assertEquals("Quoted value test failed", "quotedValue", config.getStringValue());

        // test with boolean property
        FileUtils.writeStringToFile(testConfigFile, "{ \"booleanValue\": ${testns:boolKey} }");
        testProperties = new HashMap<String, String>();
        testProperties.put("boolKey", "true"); // class default is "false"; no quotes
        service.addSubstitutionProperties(testNamespace, testProperties);
        config = service.getConfiguration(configFileBasename, DummyConfiguration.class);
        Assert.assertEquals("Boolean value test failed", true, config.isBooleanValue());
    }

    /**
     * Creates a new temporary directory.
     * 
     * @return The File object of the directory.
     */
    private File createTempDir() {
        File tempFile;
        try {
            tempFile = File.createTempFile("temp", Long.toString(System.nanoTime()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tempFile.delete();
        tempFile.mkdir();

        return tempFile;
    }

    /**
     * Recursively removes a temporary directory.
     * 
     * @param file The File object of the directory.
     */
    private void removeTempDir(File file) {
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a test instance if {@link JSONConfigurationService} with an injected mock discovery
     * bootstrap service. In this method variant, the mocked discovery service returns an empty map
     * of discovery properties.
     * 
     * @return the created instance
     */
    public JSONConfigurationService createTestInstanceOfConfigurationService() {
        return createTestInstanceOfConfigurationService(new HashMap<String, String>());
    }

    /**
     * Creates a test instance if {@link JSONConfigurationService} with an injected mock discovery
     * bootstrap service. In this method variant, the mocked discovery service returns the provided
     * map of discovery properties to the calling {@link JSONConfigurationService}.
     * 
     * @return the created instance
     */
    private JSONConfigurationService createTestInstanceOfConfigurationService(Map<String, String> substitutionProperties) {
        JSONConfigurationService service = new JSONConfigurationService();
        DiscoveryBootstrapService discoveryBootstrapService = EasyMock.createMock(DiscoveryBootstrapService.class);
        EasyMock.expect(discoveryBootstrapService.getSymbolicBundleName()).andReturn("de.rcenvironment.rce.configuration.discovery");
        EasyMock.expect(discoveryBootstrapService.initializeDiscovery((DiscoveryConfiguration) EasyMock.anyObject())).andReturn(
            substitutionProperties);
        EasyMock.replay(discoveryBootstrapService);
        service.bindDiscoveryBootstrapService(discoveryBootstrapService);
        return service;
    }

    /**
     * Creates a test configuration in the given directory.
     * 
     * @param tempDir Directory to create the configuration in.
     * @param id The id of the test configuration.
     * @param content The content of the test configuration.
     */
    private void createTestConfiguration(File tempDir, String id, String content) {
        File file = new File(tempDir.getAbsolutePath() + File.separator + id + ".json");
        try {
            file.createNewFile();
            FileWriter fstream = new FileWriter(file);
            fstream.write(content);
            fstream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Creates a bundleContextMock.
     * 
     * @return bundleContextMock.
     */
    public BundleContext getBundleContextMock() {
        Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
        EasyMock.expect(bundleMock.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME).anyTimes();
        EasyMock.replay(bundleMock);
        BundleContext bundleContextMock = EasyMock.createNiceMock(BundleContext.class);
        EasyMock.expect(bundleContextMock.getBundle()).andReturn(bundleMock).anyTimes();
        EasyMock.replay(bundleContextMock);
        return bundleContextMock;
    }

    /**
     * {@link JSONConfigurationService that throws parsing exceptions.
     */
    class JSONCS extends JSONConfigurationService {

        private Exception exceptionToThrow;

        private void setExceptionToThrow(final JsonParseException exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
        }

        private void setExceptionToThrow(final JsonMappingException exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
        }

        @Override
        protected <T> T parseConfigurationFile(Class<T> clazz, String filePath) throws IOException, JsonParseException,
            JsonMappingException {
            if (exceptionToThrow instanceof JsonParseException) {
                throw (JsonParseException) exceptionToThrow;
            } else if (exceptionToThrow instanceof JsonMappingException) {
                throw (JsonMappingException) exceptionToThrow;
            } else {
                throw new RuntimeException(exceptionToThrow);
            }
        }

    }

}
