/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.datamanagement.internal;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.datamanagement.commons.MetaData;
import de.rcenvironment.rce.datamanagement.commons.MetaDataKeys;
import de.rcenvironment.rce.datamanagement.commons.MetaDataSet;

/**
 * Common utilities for component-level data management.
 * 
 * @author Robert Mischke
 */
public abstract class ComponentDataManagementUtil {

    private static Log log = LogFactory.getLog(ComponentDataManagementUtil.class);

    /**
     * Inserts the appropriate metadata values for the given {@link ComponentInstanceInformation}
     * into the given {@link MetaDataSet}.
     * <ul>
     * <li>COMPONENT_CONTEXT_UUID</li>
     * <li>COMPONENT_CONTEXT_NAME</li>
     * <li>COMPONENT_UUID</li>
     * <li>COMPONENT_NAME</li>
     * </ul>
     * 
     * @param mds the metadata set to modify
     * @param componentInformation the source for the metadata values
     */
    public static void setComponentMetaData(MetaDataSet mds, ComponentInstanceInformation componentInformation) {

        // TODO do the 2nd and 3rd parameters need to be configurable from the outside?
        MetaData mdContextUUID = new MetaData(MetaDataKeys.COMPONENT_CONTEXT_UUID, true, true);
        MetaData mdContextName = new MetaData(MetaDataKeys.COMPONENT_CONTEXT_NAME, true, true);
        MetaData mdComponentUUID = new MetaData(MetaDataKeys.COMPONENT_UUID, true, true);
        MetaData mdComponentName = new MetaData(MetaDataKeys.COMPONENT_NAME, true, true);

        // transfer workflow/context uuid
        mds.setValue(mdContextUUID, String.valueOf(componentInformation.getComponentContextIdentifier()));

        // transfer workflow/context name
        mds.setValue(mdContextName, String.valueOf(componentInformation.getComponentContextName()));

        // transfer component uuid
        mds.setValue(mdComponentUUID, String.valueOf(componentInformation.getComponentIdentifier()));

        // transfer component name
        mds.setValue(mdComponentName, String.valueOf(componentInformation.getComponentName()));
    }

    /**
     * Sets the FILENAME metadata field.
     * 
     * @param mds the {@link MetaDataSet} to modify
     * @param filename the filename to set
     */
    public static void setAssociatedFilename(MetaDataSet mds, String filename) {
        MetaData mdFilename = new MetaData(MetaDataKeys.FILENAME, true, true);
        mds.setValue(mdFilename, filename);
    }

    /**
     * Logs debug information about a {@link MetaDataSet}.
     * 
     * TODO merge into {@link MetaDataSet}?
     * 
     * @param mds the object to log information about
     */
    public static void printDebugInfo(MetaDataSet mds) {
        Iterator<MetaData> iter = mds.iterator();
        while (iter.hasNext()) {
            MetaData md = iter.next();
            // TODO using sysout here due to current problems with log capture
            log.debug(md.getKey() + "/" + md.isRevisionIndependent() + "/" + md.isReadOnly() + " -> " + mds.getValue(md));
        }
    }

}
