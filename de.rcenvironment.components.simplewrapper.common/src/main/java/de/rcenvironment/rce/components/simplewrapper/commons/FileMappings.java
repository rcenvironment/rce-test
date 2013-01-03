/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.simplewrapper.commons;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * Mapping description from File Inputs/Outputs to paths in a local directory.
 * 
 * @author Christian Weiss
 */
public class FileMappings {

    private final List<String> directions = new LinkedList<String>();

    private final List<String> names = new LinkedList<String>();

    private final List<String> paths = new LinkedList<String>();

    /**
     * Puts.
     * 
     * @param direction the direction
     * @param name the name
     * @param path the path
     */
    public void put(final String direction, final String name, final String path) {
        boolean set = false;
        for (int index = 0; index < directions.size(); ++index) {
            if (directions.get(index).equals(direction)
                && names.get(index).equals(name)) {
                paths.set(index, path);
                set = true;
            }
        }
        if (!set) {
            directions.add(0, direction);
            names.add(0, name);
            paths.add(0, path);
        }
    }

    /**
     * Checks containment.
     * 
     * @param direction the direction
     * @param name the name
     * @return true, if contained
     */
    public boolean contains(final String direction, final String name) {
        boolean result = false;
        for (int index = 0; index < directions.size(); ++index) {
            if (directions.get(index).equals(direction)
                && names.get(index).equals(name)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns.
     * 
     * @param direction the direction
     * @param name the name
     * @return the path
     */
    public String getPath(final String direction, final String name) {
        for (int index = 0; index < directions.size(); ++index) {
            if (directions.get(index).equals(direction)
                && names.get(index).equals(name)) {
                return paths.get(index);
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the distinct directions.
     * 
     * @return the distinct directions
     */
    public Set<String> getDistinctDirections() {
        final Set<String> result = new HashSet<String>();
        result.addAll(directions);
        return Collections.unmodifiableSet(result);
    }

    /**
     * Returns a list of names.
     * 
     * @param direction the direction
     * @return a list of names
     */
    public List<String> getNames(final String direction) {
        final List<String> result = new LinkedList<String>();
        for (int index = 0; index < directions.size(); ++index) {
            if (directions.get(index).equals(direction)) {
                result.add(names.get(index));
            }
        }
        return Collections.unmodifiableList(result);
    }

    // @Override
    // public String toString() {
    // final StringBuilder builder = new StringBuilder();
    // for (int index = directions.size() - 1; index >= 0; index--) {
    // builder.append(directions.get(index))
    // }
    // }

}
