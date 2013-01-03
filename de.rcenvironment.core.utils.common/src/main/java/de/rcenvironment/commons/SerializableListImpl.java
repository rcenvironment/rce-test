/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.commons;

import java.util.ArrayList;


/**
 * A serializable {@link java.util.List}.
 *
 * @param <E> The list type
 * @author Arne Bachmann
 */
public class SerializableListImpl<E> extends ArrayList<E> implements SerializableList<E> {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 6456745608180891272L;

}
