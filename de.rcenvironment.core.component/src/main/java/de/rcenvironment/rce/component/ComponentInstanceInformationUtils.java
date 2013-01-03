/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Provides convenient methods to get information out of the {@link ComponentInstanceInformation}.
 * 
 * @author Doreen Seider
 */
public final class ComponentInstanceInformationUtils {

    protected static final String VARIABLE_PATTERN_STRING = "^.*(\\$\\{((?:[a-zA-Z0-9]+\\:)?[-_a-zA-Z0-9]*(?:\\[[0-9]+\\])*)\\}).*$";
    
    protected static final Pattern VARIABLE_PATTERN = Pattern.compile(VARIABLE_PATTERN_STRING, Pattern.DOTALL);
    
    private ComponentInstanceInformationUtils() {}
    
    /**
     * @param instInformation {@link ComponentInstanceInformation} to use
     * @return <code>true</code> if inputs are defined, otherwise <code>false</code>
     */
    public static boolean hasInputs(ComponentInstanceInformation instInformation) {
        return instInformation.getInputDefinitions().size() > 0;
    }

    /**
     * @param type type to get input definitions for
     * @param <T> data type
     * @param instInformation {@link ComponentInstanceInformation} to use
     * @return input definitions of given type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> Map<String, Class<? extends T>> getInputs(final Class<T> type,
        ComponentInstanceInformation instInformation) {
        final Map<String, Class<? extends T>> result = new HashMap<String, Class<? extends T>>();
        final Map<String, Class<? extends Serializable>> inputDefinitions = instInformation.getInputDefinitions();
        for (final String inputName : inputDefinitions.keySet()) {
            final Class<? extends Serializable> inputType = inputDefinitions.get(inputName);
            if (type == null || type.isAssignableFrom(inputType)) {
                result.put(inputName, (Class<T>) inputType);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * @param type type to get output definitions for
     * @param <T> data type 
     * @param instInformation {@link ComponentInstanceInformation} to use
     * @return output definitions of given type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> Map<String, Class<? extends T>> getOutputs(final Class<T> type,
        ComponentInstanceInformation instInformation) {
        final Map<String, Class<? extends T>> result = new HashMap<String, Class<? extends T>>();
        final Map<String, Class<? extends Serializable>> outputDefinitions = instInformation.getOutputDefinitions();
        for (final String inputName : outputDefinitions.keySet()) {
            final Class<? extends Serializable> inputType = outputDefinitions.get(inputName);
            if (type == null || type.isAssignableFrom(inputType)) {
                result.put(inputName, (Class<T>) inputType);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * @param key configuration key
     * @param clazz expected configuration type (class)
     * @param <T> expected configuration type (class)
     * @param instInformation {@link ComponentInstanceInformation} to use
     * @return configuration value
     */
    public static <T extends Serializable> T getConfigurationValue(final String key, final Class<T> clazz,
        ComponentInstanceInformation instInformation) {
        return getConfigurationValue(key, clazz, null, instInformation);
    }

    /**
     * @param key configuration key
     * @param clazz expected configuration type (class)
     * @param <T> expected configuration type (class)
     * @param defaultValue default value to use if no value is set
     * @param instInformation {@link ComponentInstanceInformation} to use
     * @return configuration value
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T getConfigurationValue(final String key, final Class<T> clazz, final T defaultValue,
        ComponentInstanceInformation instInformation) {
        final T result;
        final Serializable value = instInformation.getConfigurationValue(key);
        if (value == null) {
            result = defaultValue;
        } else if (clazz.isAssignableFrom(value.getClass())) {
            result = (T) value;
        } else if (Enum.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("rawtypes") final Class<? extends Enum> enumClazz = (Class<? extends Enum>) clazz;
            result = (T) Enum.valueOf(enumClazz, value.toString());
        } else {
            try {
                final Constructor<T> constructor = clazz.getConstructor(String.class);
                result = constructor.newInstance(value);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
    
}
