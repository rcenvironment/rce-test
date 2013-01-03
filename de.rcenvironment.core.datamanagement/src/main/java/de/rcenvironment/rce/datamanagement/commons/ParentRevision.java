/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.datamanagement.commons;

import java.io.Serializable;
import java.util.UUID;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.rce.communication.PlatformIdentifier;

/**
 * Holds information about the parent of this {@link DataReference}. A parent is the {@link DataReference}
 * which this {@link DataReference} is branched from. It is immutable.
 * 
 * @author Sandra Schroedter
 * @author Jens Ruehmkorf
 * @author Juergen Klein
 */
public final class ParentRevision implements Serializable {

    private static final long serialVersionUID = 461523746357980889L;

    private Integer parentHashCode;

    private final PlatformIdentifier parentPlatformIdentifier;

    private final UUID parentId;

    private final int parentRevision;

    public ParentRevision(UUID identifier, PlatformIdentifier platformId, int revision) {
        Assertions.isBiggerThan(revision, 0, "Revision number must be bigger than 0.");
        
        parentId = identifier;
        parentPlatformIdentifier = platformId;
        parentRevision = revision;
    }

    /**
     * @return the identifier of this parent {@link DataReference}.
     */
    public UUID getIdentifier() {
        return parentId;
    }

    /**
     * @return the host of the parent {@link DataReference}.
     */
    public PlatformIdentifier getPlatformIdentifier() {
        return parentPlatformIdentifier;
    }

    /**
     * @return the revision number of the parent {@link DataReference}.
     */
    public int getRevisionNumber() {
        return parentRevision;
    }

    @Override
    public int hashCode() {
        if (parentHashCode == null) {
            final int prime = 31;
            int result = 1;

            result = prime * result + parentPlatformIdentifier.hashCode();
            result = prime * result + parentId.hashCode();
            result = prime * result + parentRevision;

            parentHashCode = Integer.valueOf(result);
        }
        return parentHashCode;
    }

    @Override
    public ParentRevision clone() {
        return new ParentRevision(parentId, parentPlatformIdentifier, parentRevision);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        // null is never instance of any class
        // subclasses are not allowed (ParentRevision is final)
        if (obj instanceof ParentRevision) {
            final ParentRevision other = (ParentRevision) obj;

            // always sort checks by speed, fastest checks first.
            return parentRevision == other.parentRevision && parentPlatformIdentifier.equals(other.parentPlatformIdentifier)
                    && parentId.equals(other.parentId);
        }
        return false;
    }

}
