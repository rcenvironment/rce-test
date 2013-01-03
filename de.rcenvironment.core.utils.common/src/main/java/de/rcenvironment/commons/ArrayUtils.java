/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.rcenvironment.commons.variables.TypedValue;


/**
 * Helper class for SQL and Excel components, TODO found no better place for it.
 *
 * @author Arne Bachmann
 */
public final class ArrayUtils {
    
    /**
     * Hiding constructor.
     */
    private ArrayUtils() { }

    /**
     * Concats a String array to one single String by separating the Strings  with the given separator.
     * This was moved from TypedValue class, since it doesn't belong there.
     * 
     * @param array String array to concat
     * @param separator separator to use
     * @return separated list of array content
     */
    public static String arrayToString(final TypedValue[][] array, final String separator) {
        if (array == null) {
            return new String();
        }
        StringBuffer result = new StringBuffer();
        for (int row = 0; row < array.length; row++) {
            for (int col = 0; col < array[row].length; col++) {
                result.append(array[row][col].getStringValue());
                if (row != (array.length - 1) || col != (array[row].length) - 1) {// not last element
                    result.append(separator);
                }
            }
        }
        return result.toString();
    }

    /**
     * Converts an array to a {@link Set}.
     * 
     * @param <T> the type
     * @param array the array to convert
     * @return the {@link Set}
     */
    public static <T> Set<T> toSet(T[] array) {
        final Set<T> result = new HashSet<T>();
        for (final T element : array) {
            result.add(element);
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Converts an array to a {@link List}.
     * 
     * @param <T> the type
     * @param array the array to convert
     * @return the {@link List}
     */
    public static <T> List<T> toList(T[] array) {
        final List<T> result = new ArrayList<T>(array.length);
        for (final T element : array) {
            result.add(element);
        }
        return result;
    }

}
