/*
 * Copyright (C) 2006-2014 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.component.update.api;

import java.io.IOException;
import java.util.List;


/**
 * Is responsible for persistent component descriptions updates.
 *
 * @author Doreen Seider
 */
public interface DistributedPersistentComponentDescriptionUpdateService {

    /**
     * @param descriptions {@link PersistentComponentDescription}s to check
     * @param silent if dialog shouldn't pop up 
     * @return logically concatenated {@link PersistentDescriptionFormatVersion} an update must be performed for
     */
    int getFormatVersionsAffectedByUpdate(List<PersistentComponentDescription> descriptions, boolean silent);
    
    /**
     * Performs updates for all given {@link PersistentComponentDescription}s (if needed).
     * @param formatVersion {@link PersistentDescriptionFormatVersion} the update must be performed for
     * @param descriptions given {@link PersistentComponentDescription}s to possibly update
     * @param silent if dialog shouldn't pop up 
     * @return updated {@link PersistentComponentDescription}s
     * @throws IOException on parsing errors
     */
    List<PersistentComponentDescription> performComponentDescriptionUpdates(int formatVersion,
        List<PersistentComponentDescription> descriptions, boolean silent)
        throws IOException;
    
}
