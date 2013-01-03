/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.component.ComponentDescription;

/**
 * Utility functions to access {@link WorkflowNode}s.
 * 
 * @author Christian Weiss
 */
public final class WorkflowNodeUtil {

    private WorkflowNodeUtil() {
        // do nothing
    }

    /**
     * Returns, whether the specified {@link WorkflowNode} has inputs.
     * 
     * @param workflowNode the {@link WorkflowNode}
     * @return true, the specified {@link WorkflowNode} has inputs
     */
    public static boolean hasInputs(final WorkflowNode workflowNode) {
        final ComponentDescription compDescription = workflowNode.getComponentDescription();
        return !compDescription.getInputDefinitions().isEmpty();
    }

    /**
     * Returns, whether the specified {@link WorkflowNode} has outputs.
     * 
     * @param workflowNode the {@link WorkflowNode}
     * @return true, if the specified {@link WorkflowNode} has outputs
     */
    public static boolean hasOutputs(final WorkflowNode workflowNode) {
        final ComponentDescription compDescription = workflowNode.getComponentDescription();
        return !compDescription.getOutputDefinitions().isEmpty();
    }

    /**
     * Returns, whether the specified {@link WorkflowNode} has inputs of the specified type.
     * 
     * @param workflowNode the {@link WorkflowNode}
     * @param type the desired type of inputs
     * @return true, if the specified {@link WorkflowNode} has inputs of the specified type
     */
    public static boolean hasInputs(final WorkflowNode workflowNode, final Class<? extends Serializable> type) {
        return !getInputs(workflowNode, type).isEmpty();
    }

    /**
     * Returns, whether the specified {@link WorkflowNode} has outputs of the specified type.
     * 
     * @param workflowNode the {@link WorkflowNode}
     * @param type the desired type of outputs
     * @return true, if the specified {@link WorkflowNode} has outputs of the specified type
     */
    public static boolean hasOutputs(final WorkflowNode workflowNode, final Class<? extends Serializable> type) {
        return !getOutputs(workflowNode, type).isEmpty();
    }

    /**
     * Returns all inputs of the specified {@link WorkflowNode}.
     * 
     * @param workflowNode the {@link WorkflowNode}
     * @return all inputs of the specified {@link WorkflowNode}
     */
    public static Map<String, Class<? extends Serializable>> getInputs(final WorkflowNode workflowNode) {
        final ComponentDescription compDescription = workflowNode.getComponentDescription();
        return Collections.unmodifiableMap(compDescription.getInputDefinitions());
    }

    /**
     * Returns the inputs of the specified {@link WorkflowNode} having the specified type.
     * 
     * @param <T> the desired type of inputs
     * @param workflowNode the {@link WorkflowNode}
     * @param type the desired type of inputs
     * @return the inputs of the specified {@link WorkflowNode} having the specified type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> Map<String, Class<? extends T>> getInputs(final WorkflowNode workflowNode, final Class<T> type) {
        final ComponentDescription compDescription = workflowNode.getComponentDescription();
        final Map<String, Class<? extends T>> result = new HashMap<String, Class<? extends T>>();
        final Map<String, Class<? extends Serializable>> inputDefinitions = compDescription.getInputDefinitions();
        for (final String inputName : inputDefinitions.keySet()) {
            final Class<? extends Serializable> inputType = inputDefinitions.get(inputName);
            if (type.isAssignableFrom(inputType)) {
                result.put(inputName, (Class<T>) inputType);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns all outputs of the specified {@link WorkflowNode}.
     * 
     * @param workflowNode the {@link WorkflowNode}
     * @return all outputs of the specified {@link WorkflowNode}
     */
    public static Map<String, Class<? extends Serializable>> getOutputs(final WorkflowNode workflowNode) {
        final ComponentDescription compDescription = workflowNode.getComponentDescription();
        return Collections.unmodifiableMap(compDescription.getOutputDefinitions());
    }

    /**
     * Returns the outputs of the specified {@link WorkflowNode} having the specified type.
     * 
     * @param <T> the desired type of outputs
     * @param workflowNode the {@link WorkflowNode}
     * @param type the desired type of outputs
     * @return the outputs of the specified {@link WorkflowNode} having the specified type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> Map<String, Class<? extends T>> getOutputs(final WorkflowNode workflowNode,
        final Class<T> type) {
        final ComponentDescription compDescription = workflowNode.getComponentDescription();
        final Map<String, Class<? extends T>> result = new HashMap<String, Class<? extends T>>();
        final Map<String, Class<? extends Serializable>> outputDefinitions = compDescription.getOutputDefinitions();
        for (final String inputName : outputDefinitions.keySet()) {
            final Class<? extends Serializable> inputType = outputDefinitions.get(inputName);
            if (type.isAssignableFrom(inputType)) {
                result.put(inputName, (Class<T>) inputType);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns, whether the specified {@link WorkflowNode} has a property with the specified key.
     * 
     * @param workflowNode the {@link WorkflowNode}
     * @param key the key of the property
     * @return the type of the property
     */
    public static boolean hasProperty(final WorkflowNode workflowNode, final String key) {
        final boolean result = workflowNode.getComponentDescription().getConfigurationDefinitions().keySet().contains(key);
        return result;
    }

    /**
     * Returns the property type of the specified property of the specified {@link WorkflowNode}.
     * 
     * @param workflowNode the {@link WorkflowNode}
     * @param key the key of the property
     * @return the type of the property
     */
    public static Class<? extends Serializable> getPropertyType(final WorkflowNode workflowNode, final String key) {
        final Class<? extends Serializable> result = workflowNode.getComponentDescription().getConfigurationDefinitions().get(key);
        return result;
    }

    /**
     * Returns, whether the specified property of the specified
     * {@link ReadableComponentInstanceConfiguration} is set.
     * 
     * @param workflowNode the {@link ReadableComponentInstanceConfiguration}
     * @param key the key of the property
     * @return true, if the property is set
     */
    public static boolean isPropertySet(final ReadableComponentInstanceConfiguration workflowNode, final String key) {
        final boolean result = getProperty(workflowNode, key) != null;
        return result;
    }

    /**
     * Returns the value of the specified property of the specified
     * {@link ReadableComponentInstanceConfiguration}.
     * 
     * @param workflowNode the {@link ReadableComponentInstanceConfiguration}
     * @param key the key of the property
     * @return the value of the property
     */
    public static Serializable getProperty(final ReadableComponentInstanceConfiguration workflowNode, final String key) {
        final Serializable value = workflowNode.getProperty(key);
        return value;
    }

    /**
     * Returns the value of the specified type of the specified property of the specified
     * {@link ReadableComponentInstanceConfiguration}.
     * 
     * @param <T> the desired type of the property value
     * @param workflowNode the {@link ReadableComponentInstanceConfiguration}
     * @param key the key of the property
     * @param clazz the desired type of the return value
     * @return the value of the property
     */
    public static <T extends Serializable> T getProperty(final ReadableComponentInstanceConfiguration workflowNode, final String key,
        final Class<T> clazz) {
        return getProperty(workflowNode, key, clazz, null);
    }

    /**
     * Returns the value of the specified type of the specified property of the specified
     * {@link ReadableComponentInstanceConfiguration}.
     * 
     * @param <T> the desired type of the property value
     * @param workflowNode the {@link ReadableComponentInstanceConfiguration}
     * @param key the key of the property
     * @param clazz the desired type of the return value
     * @param defaultValue the default value to return if the property is not set
     * @return the value of the property or the default value
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T getProperty(final ReadableComponentInstanceConfiguration workflowNode, final String key,
        final Class<T> clazz, final T defaultValue) {
        final T result;
        final Serializable value = getProperty(workflowNode, key);
        if (value == null) {
            result = defaultValue;
        } else if (clazz.isAssignableFrom(value.getClass())) {
            result = (T) value;
        } else if (Enum.class.isAssignableFrom(clazz)) {
            /*
             * TODO @misc_ro: The IllegalArgumentException was intended. This code returns null even
             * if a (wrong) value is set. So it returns null in 2 cases (no value at all and wrong
             * value).
             * 
             * A better solution would be to introduce a new RuntimeException type (e.g.
             * ValueConversionException) that can be caught if uncertain about the values. A
             * 'Replace (uncertain) Error Code with Exception' Refactoring.
             */
            @SuppressWarnings("rawtypes") final Class<? extends Enum> enumClazz = (Class<? extends Enum>) clazz;
            // temporary holder to allow exception handling while "result" is final
            T tempResult;
            try {
                tempResult = (T) Enum.valueOf(enumClazz, value.toString());
            } catch (IllegalArgumentException e) {
                // unrecognized String for the given Enum class; log warning and return "null"
                final Log logger = LogFactory.getLog(WorkflowNodeUtil.class);
                logger.warn("Unrecognized Enum value for '" + enumClazz + "': " + value);
                tempResult = null;
            }
            result = tempResult;
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
