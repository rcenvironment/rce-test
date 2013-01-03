/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.endpoint;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Manages meta data for endpoints.
 *
 * @author Doreen Seider
 */
public class EndpointMetaDataManager implements Serializable {

    private static final long serialVersionUID = 4411509485449954384L;

    private Map<String, Map<String, Serializable>> endpointMetaData = new HashMap<String, Map<String, Serializable>>();

    public EndpointMetaDataManager(Map<String, Map<String, Serializable>> newMetaData){
        if (newMetaData != null){
            endpointMetaData = newMetaData;
        }
    }

    /**
     * @param endpointName the name of the affected endpoint.
     * @return a map containing meta data keys and its values.
     */
    public Map<String, Serializable> getEndpointMetaData(String endpointName) {
        Map<String, Serializable> metaData;
        if (endpointMetaData.containsKey(endpointName)) {
            metaData = Collections.unmodifiableMap(endpointMetaData.get(endpointName));
        } else {
            metaData = new HashMap<String, Serializable>();
        }
        return metaData;
    }

    /**
     * @param endpointName the name of the affected endpoint.
     * @param metaDataKey the meta data key to set.
     * @param metaDataValue the meta data value to set.
     */
    public void setEndpointMetaData(String endpointName, String metaDataKey, Serializable metaDataValue) {
        if (endpointMetaData.containsKey(endpointName)) {
            endpointMetaData.get(endpointName).put(metaDataKey, metaDataValue);
        } else {
            Map<String, Serializable> metaData = new HashMap<String, Serializable>();
            metaData.put(metaDataKey, metaDataValue);
            endpointMetaData.put(endpointName, metaData);            
        }
    }
}
