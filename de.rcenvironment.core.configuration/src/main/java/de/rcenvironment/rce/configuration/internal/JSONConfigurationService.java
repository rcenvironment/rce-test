/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.configuration.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.BundleContext;

import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.configuration.ConfigurationServiceMessage;
import de.rcenvironment.rce.configuration.ConfigurationServiceMessageEvent;
import de.rcenvironment.rce.configuration.ConfigurationServiceMessageEventListener;
import de.rcenvironment.rce.configuration.discovery.bootstrap.DiscoveryBootstrapService;
import de.rcenvironment.rce.configuration.discovery.bootstrap.DiscoveryConfiguration;

/**
 * Implementation of the {@link ConfigurationService} using JSON as file format.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 * @author Robert Mischke
 * @author Christian Weiß
 */
public class JSONConfigurationService implements ConfigurationService {

    private static final Log LOGGER = LogFactory.getLog(JSONConfigurationService.class);

    private static final long TIME = System.currentTimeMillis();

    /** Reusable JSON mapper object. */
    private ObjectMapper mapper = new ObjectMapper();

    private ConfigurationConfiguration configuration;

    private File logDir;

    private List<ConfigurationServiceMessageEventListener> errorListeners =
        new LinkedList<ConfigurationServiceMessageEventListener>();

    /**
     * The merged map of key-value replacements; the namespace qualifier is merged into the map keys
     * by the format "<namespace>:<plain key value>".
     */
    private Map<String, String> substitutionProperties = new HashMap<String, String>();

    private DiscoveryBootstrapService discoveryBootstrapService;

    protected void activate(BundleContext context) {
        // allow comments in JSON files
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        // get bootstrap configuration; no substitution values available yet
        LOGGER.info("Loading initial configuration");
        String configurationBundleName = context.getBundle().getSymbolicName();
        configuration = getConfiguration(configurationBundleName, ConfigurationConfiguration.class);

        // add "platformProperties" as substitution values with a "platform:" prefix
        this.addSubstitutionProperties("platform", configuration.getPlatformProperties());

        // load the discovery configuration (may use "platform:" substitutions internally);
        // bootstrapping this from the "outside" is necessary to prevent a cyclic dependency
        // while making sure the discovery properties are injected before other bundles activate
        LOGGER.info("Initializing discovery");
        String discoveryBundleName = discoveryBootstrapService.getSymbolicBundleName();
        DiscoveryConfiguration discoveryConfiguration = getConfiguration(discoveryBundleName, DiscoveryConfiguration.class);

        // initialize discovery; may start a local discovery server and/or query existing servers
        Map<String, String> discoveryProperties = discoveryBootstrapService.initializeDiscovery(discoveryConfiguration);

        // register the properties learned from discovery (if any) under the "discovery" namespace
        this.addSubstitutionProperties("discovery", discoveryProperties);
    }

    @Override
    public void addSubstitutionProperties(String namespace, Map<String, String> properties) {
        if (namespace == null || namespace.isEmpty()) {
            throw new IllegalArgumentException("Namespace must not be null");
        }
        // copy all entries with the namespace and a colon as an added key prefix
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            substitutionProperties.put(namespace + ":" + entry.getKey(), entry.getValue());
        }
    }

    @Override
    public <T> T getConfiguration(String identifier, Class<T> clazz) {
        T configObject = null;
        String filePath = getConfigurationArea() + File.separator + identifier + ".json";
        String errorMessage = null;
        Throwable exception = null;
        try {
            configObject = parseConfigurationFile(clazz, filePath);
        } catch (JsonParseException e) {
            errorMessage = Messages.bind(Messages.parsingError, filePath);
            exception = e;
        } catch (JsonMappingException e) {
            errorMessage = Messages.bind(Messages.mappingError, filePath);
            exception = e;
        } catch (IOException e) {
            errorMessage = Messages.bind(Messages.couldNotOpen, filePath);
            exception = e;
        }
        // broadcast error if parsing failed
        if (errorMessage != null) {
            LOGGER.info(errorMessage, exception);
            final ConfigurationServiceMessage error = new ConfigurationServiceMessage(errorMessage);
            fireErrorEvent(error);
        }
        // create a new configuration instance if parsing did not succeed
        if (configObject == null) {
            try {
                configObject = clazz.newInstance();
            } catch (InstantiationException e) {
                LOGGER.error(e);
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                LOGGER.error(e);
                throw new RuntimeException(e);
            }
        }
        return configObject;
    }

    protected <T> T parseConfigurationFile(Class<T> clazz, String filePath) throws IOException, JsonParseException, JsonMappingException {
        T configObject;
        final File configFile = new File(filePath);
        if (configFile.exists()) {
            String fileContent = FileUtils.readFileToString(configFile);
            fileContent = performSubstitutions(fileContent, configFile);
            if (fileContent.equals("")){
                return null;
            }
            configObject = mapper.readValue(fileContent, clazz);
            return configObject;
        } else {
            return null;
        }
    }

    private String performSubstitutions(String input, File originFile) {
        // shortcut if no substitution is configured
        if (substitutionProperties.isEmpty()) {
            return input;
        }
        // construct pattern to detect "${namespace:key}" and extract the "namespace:key" part
        Pattern pattern = Pattern.compile("\\$\\{(\\w+:\\w+)\\}");
        // note: the Matcher class enforces use of StringBuffer (instead of StringBuilder)
        StringBuffer buffer = new StringBuffer(input.length());
        // perform substitution
        Matcher m = pattern.matcher(input);
        while (m.find()) {
            String key = m.group(1);
            String value = substitutionProperties.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Missing configuration value for \"" + key + "\" in file "
                    + originFile.getAbsolutePath());
            }
            m.appendReplacement(buffer, value);
        }
        m.appendTail(buffer);
        return buffer.toString();
    }

    @Override
    public String getAbsolutePath(String identifier, String path) {
        final String absolutePath;
        if (new File(path).isAbsolute()) {
            absolutePath = path;
        } else {
            absolutePath = getConfigurationArea() + File.separator + identifier + File.separator + path;
        }
        return absolutePath;
    }

    @Override
    public String getPlatformHome() {
        return configuration.getPlatformHome();
    }

    @Override
    public String getPlatformHost() {
        return configuration.getHost();
    }

    @Override
    public int getPlatformNumber() {
        return configuration.getPlatformNumber();
    }

    @Override
    public String getPlatformName() {
        return configuration.getPlatformName();
    }

    @Override
    public boolean getIsWorkflowHost() {
        return configuration.getIsWorkflowHost();
    }
    
    @Override
    public String getPlatformTempDir() {
        final String tempDir = configuration.getPlatformTempDir() + File.separator + TIME;
        // ensure that the directory exists
        // TODO could be improved by doing it once on startup
        // TODO review: use TempFileUtils instead? -- misc_ro
        File tempFile = new File(tempDir);
        tempFile.mkdirs();
        tempFile.deleteOnExit();
        return tempDir;
    }

    @Override
    public synchronized File getPlatformLogFilesDir() {
        if (logDir == null) {
            // hard-coded for now: "log" directory inside the platform home dir
            logDir = new File(configuration.getPlatformHome(), "log");
            // ensure that the directory exists
            logDir.mkdirs();
        }
        return logDir;
    }

    @Override
    public String getConfigurationArea() {
        String path = System.getProperty("de.rcenvironment.rce.configuration.dir", "");
        if (!new File(path).exists()) {
            path = System.getProperty("user.home") + File.separator + ".rce" + File.separator + "configuration";
            if (!new File(path).exists()) {
                path = System.getProperty("osgi.install.area").replace("file:", "") + "configuration";
            }
        }
        return path;
    }

    @Override
    public void addErrorListener(ConfigurationServiceMessageEventListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        errorListeners.add(listener);
        LOGGER.info(String.format("Added instance of type '%s' to the configuration service error listeners.", listener.getClass()));
    }

    @Override
    public void removeErrorListener(ConfigurationServiceMessageEventListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        errorListeners.remove(listener);
        LOGGER.info(String.format("Removed instance of type '%s' to the configuration service error listeners.", listener.getClass()));
    }

    protected void fireErrorEvent(final ConfigurationServiceMessage error) {
        final ConfigurationServiceMessageEvent event = new ConfigurationServiceMessageEvent(this, error);
        RuntimeException exception = null;
        for (final ConfigurationServiceMessageEventListener listener : errorListeners) {
            try {
                listener.handleConfigurationServiceError(event);
            } catch (RuntimeException e) {
                // only cache first exception
                if (exception == null) {
                    exception = e;
                }
            }
        }
        // re-throw first exception
        if (exception != null) {
            throw exception;
        }
    }

    protected void bindDiscoveryBootstrapService(DiscoveryBootstrapService newService) {
        this.discoveryBootstrapService = newService;
    }

}
