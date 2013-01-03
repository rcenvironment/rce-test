/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons;

/**
 * Provides common utility methods for {@link Comparator} writing.
 * 
 * @author Robert Mischke
 */
public abstract class ComparatorUtils {

    /**
     * A constant for returning a positive integer in {@link Comparator#compare(Object, Object)}.
     * Usually needed to satisfy Checkstyle.
     */
    public static final int POSITIVE_INT = 1;

    /**
     * A constant for returning a negative integer in {@link Comparator#compare(Object, Object)}.
     * Usually needed to satisfy Checkstyle.
     */
    public static final int NEGATIVE_INT = -1;

    private ComparatorUtils() {}

    /**
     * Implements {@link Comparator#compare(Object, Object)} for integers.
     * 
     * @param val1 value 1
     * @param val2 value 2
     * @return see {@link Comparator#compare(Object, Object)}
     */
    public static int compareInt(int val1, int val2) {
        int diff = val1 - val2;
        // Note: this is rather verbose as Checkstyle forbids the conditional operator ("a?b:c")
        if (diff > 0) {
            return POSITIVE_INT;
        }
        if (diff < 0) {
            return NEGATIVE_INT;
        }
        return 0;
    }

    /**
     * Implements {@link Comparator#compare(Object, Object)} for integers.
     * 
     * @param val1 value 1
     * @param val2 value 2
     * @return see {@link Comparator#compare(Object, Object)}
     */
    public static int compareLong(long val1, long val2) {
        long diff = val1 - val2;
        // Note: this is rather verbose as Checkstyle forbids the conditional operator ("a?b:c")
        if (diff > 0) {
            return POSITIVE_INT;
        }
        if (diff < 0) {
            return NEGATIVE_INT;
        }
        return 0;
    }
}
