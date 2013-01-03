/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons;

import java.io.Serializable;
import java.util.Map;

/**
 * A {@link Map} that implements {@link Serializable} (necessary for the typed properties class).
 * 
 * @param <K> type the keys of the {@link Map} are restricted to
 * @param <V> type the values of the {@link Map} are restricted to
 * @author Arne Bachmann
 */
public interface SerializableMap<K, V> extends Map<K, V>, Serializable {

}
