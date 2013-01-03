/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.commons;

import java.util.Hashtable;


/**
 * A serializable {@link java.util.Map}.
 *
 * @param <K> Key type
 * @param <V> Value type
 * @author Arne Bachmann
 */
public class SerializableMapImpl<K, V> extends Hashtable<K, V> implements SerializableMap<K, V> {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 8190529562295322999L;

}
