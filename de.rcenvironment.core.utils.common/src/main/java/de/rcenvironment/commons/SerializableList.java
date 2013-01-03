/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons;

import java.io.Serializable;
import java.util.List;

/**
 * A {@link List} that implements {@link Serializable} (necessary for the typed properties class).
 * 
 * @param <E> type the entries of the {@link List} are restricted to
 * @author Arne Bachmann
 */
public interface SerializableList<E> extends List<E>, Serializable {

}
