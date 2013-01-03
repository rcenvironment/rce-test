/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.commons;

import java.io.Serializable;
import java.net.URI;

/**
 * Holds information about a revision of a {@link DataReference}. It is immutable.
 * 
 * @author Sandra Schroedter
 */
public class Revision implements Comparable<Revision>, Serializable {

    private static final long serialVersionUID = -8993225849446652233L;

    private Integer hashCode;

    private String stringRepresentation;

    private final int number;

    private final URI location;

    public Revision(int number, URI location) {

        this.number = number;
        this.location = location;
    }

    /**
     * @return Location of the data represented by this {@link Revision}.
     */
    public URI getLocation() {
        return this.location;
    }

    /**
     * @return Revision number of this {@link Revision}.
     */
    public int getRevisionNumber() {
        return number;
    }

    @Override
    public int hashCode() {
        if (hashCode == null) {
            final int prime = 31;
            int result = 1;

            result = prime * result + location.hashCode();
            result = prime * result + number;

            hashCode = Integer.valueOf(result);
        }
        return hashCode;
    }

    @Override
    public String toString() {
        if (stringRepresentation == null) {
            stringRepresentation = "Revision " + number + ":" + location.toString();
        }
        return stringRepresentation;
    }
    
    @Override
    public Revision clone() {
        return new Revision(number, location);
    }

    @Override
    public int compareTo(Revision rev) {
        if (number == rev.number) {
            return location.compareTo(rev.location);
        }
        return Integer.valueOf(number).compareTo(Integer.valueOf(rev.number));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        // "obj instanceof null" is never true,
        // subclasses of Revision are not allowed (Revision is final)
        if (obj instanceof Revision) {
            final Revision other = (Revision) obj;

            // always sort checks by speed, fastest checks first.
            return number == other.number && location.equals(other.location);
        }
        return false;
    }

}
