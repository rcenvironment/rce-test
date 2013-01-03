/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.optimizer.commons;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class holding one set of values for an optimizer output.
 * @author Sascha Zur
 */
public class OptimizerResultSet implements Serializable {

    private static final long serialVersionUID = -5549958046464158432L;

    private final Map<String, Serializable> values = new HashMap<String, Serializable>();

    private String component;
    
    public OptimizerResultSet(final Map<String, Serializable> values, String component) {
        this.values.putAll(values);
        this.setComponent(component);
    }

    /**
     * @param key the key of the value to get.
     * @return the value.
     */
    public Serializable getValue(final String key) {
        return values.get(key);
    }

    /**
     * @param <T> type super class.
     * @param key the key of the value to get.
     * @param clazz type of value.
     * @return the value.
     * @throws ClassCastException if cast fails.
     */
    public <T extends Serializable> T getValue(final String key, Class<T> clazz) throws ClassCastException {
        final Serializable value = values.get(key);
        
        if (value != null && !clazz.isAssignableFrom(value.getClass())) {
            throw new ClassCastException();
        }
        if (value != null) {
            return clazz.cast(value);
        } else {
            return null;
        }
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    
    public Map<String, Serializable> getValues() {
        return values;
    }

}
