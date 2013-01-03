/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.StringUtils;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Class providing utility methods used for handling component properties.
 * 
 * @author Doreen Seider
 */
public final class ComponentUtils {
    /** Regex expression for all placeholder.  */
    public static final String PLACEHOLDER_REGEX = "\\$\\{((\\w*)(\\.))?((\\*)(\\.))?(\\w*)\\}";
    /*
     * Note for the Regex:
     * A placeholder has the form ${ATTRIBUTE1.ATTRIBUTE2.NAME}
     * where the attributes are optional. For getting the groups of a regex match, you have:
     * group 2 : ATTRIBUTE1
     * group 5 : ATTRIBUTE2
     * group 7 : NAME
     *  where group 2 and 5 can be null if there is no attribute.
     *  
     */
    /** Constant. */
    public static final int ATTRIBUTE1 = 2;
    /** Constant. */
    public static final int ATTRIBUTE2 = 5;
    /** Constant. */
    public static final int PLACEHOLDERNAME = 7;
    /** Constant. */
    public static final String GLOBALATTRIBUTE = "global";
    /** Constant. */
    public static final String ENCODEDATTRIBUTE = "*";
    
    private static final String PARSING_CONFIGURATION_VALUE_FAILED = "Parsing configuration value failed: ";
    
    private static final Log LOGGER = LogFactory.getLog(ComponentUtils.class);

    private ComponentUtils() {}

    /**
     * Parses a {@link String} array formated as input, output, or configuration property into a
     * {@link String}-{@link Class}-{@link Map}.
     * 
     * @param property The property array to convert.
     * @return the converted {@link Map}.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Class<? extends Serializable>> parsePropertyForConfigTypes(String[] property) {

        Map<String, Class<? extends Serializable>> propertyMap = new HashMap<String, Class<? extends Serializable>>();

        if (property != null) {

            for (String propertyEntry : property) {
                try {
                    String[] configurationPair = StringUtils.split(propertyEntry);
                    propertyMap.put(StringUtils.unescapeSeparator(configurationPair[0]),
                        (Class<? extends Serializable>) Class.forName(StringUtils.unescapeSeparator(configurationPair[1])));
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("A type within a component property value is not a valid class: "
                        + propertyEntry, e);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Invalid syntax within a component property value of a Component: "
                        + property, e);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("A type within a component property value is not serializable: "
                        + property, e);
                }
            }
        }
        return propertyMap;
    }

    /**
     * Devides the given properties in the front and metadata section.
     * 
     * @param divideData : the properties to divide.
     * @return Object[] : the divided strings.
     */
    public static Object[] divideProperty(String[] divideData) {

        Map<String, String> inputMetaData = new HashMap<String, String>();
        for (int i = 0 ; i < divideData.length; i++){
            if (divideData[i].contains(ComponentConstants.METADATA_SEPARATOR) 
                && divideData[i].indexOf(ComponentConstants.METADATA_SEPARATOR) != divideData[i].length() - 1){
                String[] splitted = StringUtils.split(divideData[i], ComponentConstants.METADATA_SEPARATOR);
                divideData[i] = splitted[0];
                if (splitted.length > 1){
                    inputMetaData.put(StringUtils.split(splitted[0])[0], splitted[1]);
                } else {
                    inputMetaData = null;
                }
            }
        }
        return new Object[]{divideData, inputMetaData};
    }
    /**
     * Parses a {@link String} array formated as input, output, or configuration property into a
     * {@link String}-{@link Class}-{@link Map}.
     * 
     * @param property The property array to convert.
     * @return the converted {@link Map}.
     */
    public static Map<String, Map<String, Serializable>> parsePropertyForMetaTypes(Map<String, String> property) {

        Map<String, Map<String, Serializable>> propertyMap = new HashMap<String, Map<String, Serializable>>();

        if (property != null) {

            for (String propertyEntry : property.keySet()) {
                try {
                    String[] configurations = StringUtils.split(property.get(propertyEntry), ":");
                    for (String metaData : configurations){
                        String[] splitted = StringUtils.split(metaData, ComponentConstants.METADATA_VALUE_SEPARATOR);
                        Map<String, Serializable> entry = new HashMap<String, Serializable>();
                        entry.put(splitted[0], splitted[1]);
                        propertyMap.put(propertyEntry, entry);
                    }

                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("A type within a component property value is not serializable: "
                        + property, e);
                }
            }
        }
        return propertyMap;
    }

    /**
     * Parses the placeholder Attributes.
     * @param strings : list of attributes
     * @return Attributes
     */
    public static Map<String, Map<String, String>> parsePlaceholderAttributes(String[] strings) {
        Map <String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        if (strings != null){
            for (String placeholderAttributes : strings){
                String[] content = placeholderAttributes.split(ComponentConstants.METADATA_SEPARATOR);
                Map<String, String> attributesResult = new HashMap<String, String>();
                if (content.length > 1) {
                    String[] attributes = content[1].split(":");
                    for (String attribut : attributes){
                        String[] splitted = attribut.split("=");
                        attributesResult.put(splitted[0], splitted[1]);  
                        
                    }
                }
                result.put(content[0], attributesResult);
            }
            
        }
        return result;
    }


    /**
     * Converts a given configuration, which values are exclusively defined as {@link String}
     * objects into the object which is expected and defined in the configuration definition.
     * 
     * @param configDef The configuration definition with the expected types.
     * @param config The configuration to convert.
     * @return the converted configuration {@link Map}.
     */
    public static Map<String, Serializable> convertConfigurationValues(Map<String, Class<? extends Serializable>> configDef,
        Map<String, String> config) {

        Map<String, Serializable> convertedConfig = new HashMap<String, Serializable>();

        for (String configKey : config.keySet()) {
            Object defaultValue = config.get(configKey);

            if (defaultValue == null) {
                convertedConfig.put(StringUtils.unescapeSeparator(configKey), null);
            } else {
                Class<? extends Serializable> type = configDef.get(configKey);

                // attention: this type check is done in
                // de.rcenvironment.rce.gui.workflow.editor.properties.ComponentPropertySource
                // as well. if a supported data type is added or removed, so it is needed to
                // do this in the other class as well
                if (type == Integer.class) {
                    try {
                        convertedConfig.put(StringUtils.unescapeSeparator(configKey), Integer.parseInt((String) defaultValue));
                    } catch (NumberFormatException e) {
                        config.put(configKey, null);
                        LOGGER.error(PARSING_CONFIGURATION_VALUE_FAILED + configKey
                            + StringUtils.SEPARATOR + type + StringUtils.SEPARATOR + defaultValue);
                    }

                } else if (type == Double.class) {
                    try {
                        convertedConfig.put(StringUtils.unescapeSeparator(configKey), Double.parseDouble((String) defaultValue));
                    } catch (NumberFormatException e) {
                        config.put(configKey, null);
                        LOGGER.error(PARSING_CONFIGURATION_VALUE_FAILED + configKey
                            + StringUtils.SEPARATOR + type + StringUtils.SEPARATOR + defaultValue);
                    }

                } else if (type == Long.class) {
                    try {
                        convertedConfig.put(StringUtils.unescapeSeparator(configKey), Long.parseLong((String) defaultValue));
                    } catch (NumberFormatException e) {
                        config.put(configKey, null);
                        LOGGER.error(PARSING_CONFIGURATION_VALUE_FAILED + configKey
                            + StringUtils.SEPARATOR + type + StringUtils.SEPARATOR + defaultValue);
                    }

                } else if (type == Boolean.class) {
                    convertedConfig.put(StringUtils.unescapeSeparator(configKey), Boolean.parseBoolean((String) defaultValue));

                } else if (type == String.class) {
                    convertedConfig.put(StringUtils.unescapeSeparator(configKey), StringUtils.unescapeSeparator((String) defaultValue));

                } else {
                    convertedConfig.put(StringUtils.unescapeSeparator(configKey), null);
                    LOGGER.error("Configuration type not supported: " + configKey
                        + StringUtils.SEPARATOR + type + StringUtils.SEPARATOR + defaultValue);
                }
            }
        }
        return convertedConfig;
    }

    /**
     * Parses a configuration given by a {@link String} array to the same configuration given as a {@link Map}.
     * 
     * @param property The configuration to parse.
     * @return the configuration as {@link Map}.
     */
    public static Map<String, String> parsePropertyForConfigValues(String[] property) {
        Map<String, String> parsedProperty = new HashMap<String, String>();

        if (property != null) {
            for (String propertyEntry : property) {
                String[] propertyEntryParts = StringUtils.split(propertyEntry);
                if (propertyEntryParts.length == 2) {
                    parsedProperty.put(StringUtils.unescapeSeparator(propertyEntryParts[0]), null);
                } else if (propertyEntryParts.length > 2) {
                    String propertyValue = propertyEntry.substring(propertyEntryParts[0].length()
                        + propertyEntryParts[1].length() + 2);
                    parsedProperty.put(StringUtils.unescapeSeparator(propertyEntryParts[0]), StringUtils.unescapeSeparator(propertyValue));
                }
            }            
        }
        return parsedProperty;
    }

    /**
     * Returns a list of platforms the component is installed on.
     * 
     * @param componentId Identifier of the component.
     * @param componentDescriptions given list of available {@link ComponentDescription}s
     * @return List of platform the component is installed on.
     */
    public static List<PlatformIdentifier> getPlatformsForComponent(List<ComponentDescription> componentDescriptions, String componentId) {
        List<PlatformIdentifier> identifiers = new ArrayList<PlatformIdentifier>();
        for (ComponentDescription desc : componentDescriptions) {
            if (desc.getIdentifier().equals(componentId)) {
                identifiers.add(desc.getPlatform());
            }
        }
        return identifiers;
    }

    /**
     * Returns whether the given component is available on the given platform.
     * 
     * @param componentId Identifier of the component.
     * @param platformId PlatformIdentifier.
     * @param componentDescriptions given list of available {@link ComponentDescription}s
     * @return Whether the given component is available on the given platform.
     */
    public static boolean hasComponent(List<ComponentDescription> componentDescriptions, String componentId,
        PlatformIdentifier platformId) {
        for (ComponentDescription desc : componentDescriptions) {
            if (desc.getIdentifier().equals(componentId) && desc.getPlatform().equals(platformId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Eliminates duplicate {@link ComponentDescription}s in given list.
     * 
     * @param componentDescriptions given list of available {@link ComponentDescription}s
     * @return Whether the given component is available on the given platform.
     */
    public static List<ComponentDescription> eliminateDuplicates(List<ComponentDescription> componentDescriptions) {
        List<ComponentDescription> descriptions = new ArrayList<ComponentDescription>();

        // eliminate duplicates
        for (ComponentDescription desc : componentDescriptions) {
            boolean contained = false;
            for (ComponentDescription containedDesc : descriptions) {
                if (desc.getIdentifier().equals(containedDesc.getIdentifier())) {
                    contained = true;
                    continue;
                }
            }
            if (!contained) {
                descriptions.add(desc.clone());
            }
        }

        return descriptions;
    }

    /**
     * Returns a placeholder {@link ComponentDescription} used if actual component of .wf file is not available.
     * @param placeholderName name to use for placeholder component
     * @return placeholder {@link ComponentDescription}
     */
    public static ComponentDescription getPlaceholderComponentDescription(String placeholderName) {
        Map<String, Class<? extends Serializable>> inputsDef = new HashMap<String, Class<? extends Serializable>>();
        Map<String, Class<? extends Serializable>> outputsDef = new HashMap<String, Class<? extends Serializable>>();
        Map<String, Map<String, Serializable>> inputMetaDefs = new HashMap<String, Map<String, Serializable>>();
        Map<String, Map<String, Serializable>> outputMetaDefs = new HashMap<String, Map<String, Serializable>>();

        Map<String, Class<? extends Serializable>> configDefs = new HashMap<String, Class<? extends Serializable>>();
        Map<String, Serializable> defaultConfig = new HashMap<String, Serializable>();

        Map<String, Map<String, String>> placeholderAttributes = new HashMap<String, Map<String, String>>();

        DeclarativeComponentDescription dcd = new DeclarativeComponentDescription(
            ComponentConstants.PLACEHOLDER_COMPONENT_IDENTIFIER_CLASS + placeholderName,
            placeholderName, ComponentConstants.COMPONENT_GROUP_UNKNOWN, ComponentConstants.COMPONENT_VERSION_UNKNOWN,
            inputsDef, outputsDef, inputMetaDefs, outputMetaDefs, configDefs, placeholderAttributes, defaultConfig, 
            readPlaceholderIcon16(), readPlaceholderIcon32());
        return new ComponentDescription(dcd);
    }

    private static byte[] readPlaceholderIcon16() {
        return readPlaceholderIcon("/resources/icons/component16.gif");
    }

    private static byte[] readPlaceholderIcon32() {
        return readPlaceholderIcon("/resources/icons/component32.gif");
    }

    private static byte[] readPlaceholderIcon(String resourceName) {

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream stream = ComponentUtils.class.getResourceAsStream(resourceName);
            if (stream != null) {
                while (true) {
                    int r = stream.read();
                    if (r < 0) {
                        break;
                    }
                    bos.write(r);
                }
                return bos.toByteArray();
            } else {
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("Cannot read icon for placeholder component");
            return null;
        }

    }

    /**
     * Checks whether the given property is a placeholder.
     * @param proptertyToTest : the property
     * @return true, if it is a placeholder
     */
    public static boolean isPlaceholder(String proptertyToTest){
        if (proptertyToTest != null){
            return proptertyToTest.matches(ComponentUtils.PLACEHOLDER_REGEX);
        }
        return false;
    }
    
    /**
     * Checks whether the given placeholder is a global placeholder. 
     * @param placeholder :
     * @return true if it is
     */
    public static boolean isGlobalPlaceholder(String placeholder){
        Matcher matcherOfPlaceholder = getMatcherForPlaceholder(placeholder);
        return (matcherOfPlaceholder.group(ATTRIBUTE1) != null 
            && (matcherOfPlaceholder.group(ATTRIBUTE1).equals(GLOBALATTRIBUTE) 
                ||  (matcherOfPlaceholder.group(ATTRIBUTE2) != null && matcherOfPlaceholder.group(ATTRIBUTE2).equals(GLOBALATTRIBUTE))));
    }
    /**
     * Creates a @link {@link Matcher} for the given placeholder.
     * @param placeholder : 
     * @return :
     */
    public static Matcher getMatcherForPlaceholder(String placeholder){
        Pattern pattern = Pattern.compile(ComponentUtils.PLACEHOLDER_REGEX);  
        Matcher matcher = pattern.matcher(placeholder);  
        matcher.find();
        return matcher;
    }
}
