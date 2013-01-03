/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.datamanagement.browser.spi;

/**
 * Type of the data management object.
 * 
 * @author Markus Litz
 * @author Robert Mischke
 */
public enum DMBrowserNodeType {

    /** DM-Object is a chameleon-rce workflow. */
    Workflow,

    /** DM-Object is a timeline. */
    Timeline,

    /** DM-Object is a component container. */
    Components,

    /** DM-Object is a chameleon-rce file resource. */
    Resource,

    /** DM-Object is a chameleon-rce component. */
    Component,

    /** DM-Object is a chameleon-rce folder. */
    Folder,

    /** DM-Object is a chameleon-rce versioned file. */
    VersionizedResource,

    /**
     * Node type for the root of a history object group.
     */
    HistoryRoot,

    /**
     * Node type for individual history objects.
     */
    HistoryObject,

    /**
     * A type for nodes that represent a DM reference which in turn represents a
     * file; such a file is expected to have an filename associated via
     * {@link MetaDataKeys#FILENAME}.
     */
    DMFileResource,

    /**
     * Node type for information text nodes.
     */
    InformationText,

    /**
     * Node type for warning text nodes.
     */
    WarningText,

    /**
     * Node type for an empty node.
     */
    Empty,

    /**
     * Node type for placeholder nodes indicating that content is being fetched.
     */
    Loading;

}
