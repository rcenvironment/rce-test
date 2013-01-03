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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Used as pointer to a data set holding all information about it.
 * 
 * @author Sandra Schroedter
 * @author Jens Ruehmkorf
 * @author Juergen Klein
 */
public final class DataReference implements Serializable, Iterable<Revision> {

    /**
     * The type of the data a {@link DataReference} points to.
     * 
     * @author Sandra Schroedter
     */
    public enum DataReferenceType {
        /** Plain file streams. */
        fileObject
    }

    /**
     * Constants representing the last revision of a {@link DataReference}.
     */
    public static final int HEAD_REVISION = -1;

    /**
     * Constants representing the revision independent meta data of a {@link DataReference}.
     */
    public static final int REVISION_INDEPENDENT_REVISION = 0;

    /**
     * Constants representing the very first revision of a {@link DataReference}.
     */
    public static final int FIRST_REVISION = 1;

    private static final long serialVersionUID = -5443653424654542352L;

    private final UUID identifier;

    private final PlatformIdentifier platformIdentifier;

    private final DataReferenceType dataType;

    /**
     * Null if {@link DataReference} has no parent.
     */
    private final ParentRevision parentRev;

    /**
     * Unsorted {@link Map} of revisions available in this {@link DataReference}. The revision number serves as key.
     */
    private final Map<Integer, Revision> revisions = new HashMap<Integer, Revision>();

    public DataReference(DataReferenceType type, UUID guid, PlatformIdentifier platform) {
        this(type, guid, platform, null);
    }

    public DataReference(DataReferenceType type, UUID identifier, PlatformIdentifier platform, ParentRevision parentRevision) {
        this.identifier = identifier;
        this.dataType = type;
        this.platformIdentifier = platform;
        this.parentRev = parentRevision;
        // necessary to fulfill integrity constraint in DB
        clear();
    }
    
    /**
     * Adds a {@link Revision} with the given number and location to this {@link DataReference}.
     * 
     * @param revisionNumber
     *            Number of revision to add.
     * @param location
     *            Location of revision to add.
     */
    public void addRevision(int revisionNumber, URI location) {
        revisions.put(revisionNumber, new Revision(revisionNumber, location));
    }

    /**
     * Removes all {@link Revision}s from this {@link DataReference}.
     */
    public void clear() {
        revisions.clear();
        // necessary to fulfill integrity constraint in DB
        addRevision(REVISION_INDEPENDENT_REVISION, URI.create(""));
    }

    @Override
    public DataReference clone() {
        DataReference newReference;

        if (parentRev == null) {
            newReference = new DataReference(dataType, identifier, platformIdentifier);
        } else {
            newReference = new DataReference(dataType, identifier, platformIdentifier, parentRev.clone());
        }

        for (Revision revision : revisions.values()) {
            newReference.addRevision(revision.clone());
        }
        return newReference;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DataReference) {
            final DataReference other = (DataReference) obj;

            if (parentRev == null) {
                return dataType.equals(other.dataType) && (other.parentRev == null) && identifier.equals(other.identifier)
                        && revisions.equals(other.revisions);
            } else {
                return dataType.equals(other.dataType) && parentRev.equals(other.parentRev) && identifier.equals(other.identifier)
                        && revisions.equals(other.revisions);
            }
        }
        return false;
    }

    /**
     * @return the guid of this {@link DataReference}.
     */
    public UUID getIdentifier() {
        return identifier;
    }

    /**
     * @return the highest revision number of this {@link DataReference}.
     */
    public int getHighestRevisionNumber() {
        int highest = 0;
        for (final Integer number : revisions.keySet()) {
            if (number == DataReference.REVISION_INDEPENDENT_REVISION) {
                continue;
            }
            highest = Math.max(highest, number);
        }
        return highest;
    }

    /**
     * Returns the location of the {@link Revision} with the given revision number of this
     * {@link DataReference}.
     * 
     * @param revisionNumber
     *            Revision number for which the location shall be returned.
     * @return the location of the revision with the given revision number or <code>null</code> if
     *         no {@link Revision} with that number exists.
     */
    public URI getLocation(int revisionNumber) {
        if (revisionNumber <= 0 && revisionNumber != DataReference.HEAD_REVISION) {
            throw new IllegalArgumentException();
        }
        Revision revision = getRevision(revisionNumber);
        if (revision != null) {
            return revision.getLocation();
        } else {
            return null;
        }
    }

    /**
     * @return the {@link ParentRevision} of this {@link DataReference} from which this
     * {@link DataReference} was branched or <code>null</code> if this {@link DataReference} was not branched.
     */
    public ParentRevision getParentRevision() {
        return parentRev;
    }

    /**
     * @return the {@link PlatformIdentifier} of the platform this {@link DataReference} is hosted.
     */
    public PlatformIdentifier getPlatformIdentifier() {
        return platformIdentifier;
    }
    
    /**
     * Returns the current head {@link Revision}.
     * 
     * @return the head {@link Revision}
     */
    public Revision getHeadRevision() {
        return getRevision(DataReference.HEAD_REVISION);
    }

    /**
     * @param revisionNumber Number of {@link Revision} to retrieve.
     * @return the {@link Revision} with the given revision number or <code>null</code> if there is none.
     */
    public Revision getRevision(int revisionNumber) {
        if (!isValidRevisionNumber(revisionNumber)) {
            throw new IllegalArgumentException();
        }
        final Revision result;
        final int highestRevisionNumber = getHighestRevisionNumber();
        if (revisionNumber == DataReference.REVISION_INDEPENDENT_REVISION
                || highestRevisionNumber == DataReference.REVISION_INDEPENDENT_REVISION) {
            result = null;
        } else if (revisionNumber == DataReference.HEAD_REVISION) {
            result = revisions.get(highestRevisionNumber);
        } else {
            result = revisions.get(revisionNumber);
        }
        return result;
    }

    /**
     * @return a sorted array containing the revision numbers of this {@link DataReference}.
     */
    public int[] getRevisionNumbers() {
        int[] result = new int[revisions.size() - 1];
        int i = 0;
        for (final Integer revisionNumber : revisions.keySet()) {
            if (revisionNumber == DataReference.REVISION_INDEPENDENT_REVISION) {
                continue;
            }
            result[i] = revisionNumber;
            i++;
        }
        Arrays.sort(result);
        return result;
    }

    /**
     * @return the type of the data this {@link DataReference} points to.
     */
    public DataReferenceType getDataType() {
        return dataType;
    }

    protected boolean isValidRevisionNumber(int revisionNumber) {
        boolean result = true;
        result &= revisionNumber != DataReference.REVISION_INDEPENDENT_REVISION;
        result &= revisionNumber >= DataReference.HEAD_REVISION;
        return result;
    }

    /**
     * Checks if the given revision number links to a valid {@link Revision} of this {@link DataReference}.
     * 
     * @param revision
     *            Revision number to check.
     * @return <code>true</code> if a {@link Revision} with the given revision number exists, <code>false</code> otherwise.
     */
    public boolean isValidRevision(int revision) {
        if (!isValidRevisionNumber(revision)) {
            throw new IllegalArgumentException();
        }
        if (getRevision(revision) != null) {
            return true;
        }
        return false;            
    }

    /**
     * @return a read-only iterator over the {@link java.util.Collection} of {@link Revision}s.
     * 
     * @see Iterable#iterator()
     */
    @Override
    public Iterator<Revision> iterator() {
        final Map<Integer, Revision> visibleRevisions = new HashMap<Integer, Revision>(revisions);
        visibleRevisions.remove(DataReference.REVISION_INDEPENDENT_REVISION);
        return Collections.unmodifiableCollection(visibleRevisions.values()).iterator();
    }

    /**
     * Removes a {@link Revision} from a given DataReference.
     * 
     * @param revisionNumber
     *            Number of {@link Revision} to remove.
     * @return <code>true</code> if a {@link Revision} is removed, <code>false</code> otherwise.
     */
    public boolean removeRevision(int revisionNumber) {
        if (revisionNumber <= 0) {
            throw new IllegalArgumentException();
        }
        return revisions.remove(revisionNumber) != null;
    }

    /**
     * @return the number of {@link Revision}s of this {@link DataReference}.
     */
    public int size() {
        return revisions.size() - 1;
    }

    @Override
    public String toString() {
        StringBuilder text;
        int strLengh;

        strLengh = (identifier.toString() + "\n").length();
        for (Revision revision : revisions.values()) {
            strLengh += (revision + "\n").length();
        }

        text = new StringBuilder(strLengh);
        text.append(getIdentifier().toString() + "\n");
        for (Revision revision : revisions.values()) {
            text.append(revision + "\n");
        }

        return text.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + identifier.hashCode();
        if (parentRev == null) {
            result = prime * result;
        } else {
            result = prime * result + parentRev.hashCode();
        }
        result = prime * result + revisions.hashCode();
        result = prime * result + dataType.hashCode();

        return result;
    }
    
    private void addRevision(Revision revision) {
        revisions.put(revision.getRevisionNumber(), revision);
    }

}
