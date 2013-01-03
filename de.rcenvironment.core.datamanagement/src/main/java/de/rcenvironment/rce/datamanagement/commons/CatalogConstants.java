/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.commons;

/**
 * Constants for names in catalog database.
 * 
 * @author Dirk Rossow
 */
public final class CatalogConstants {

    /** Name of table holding {@link DataReference}s. */
    public static final String DATAREFERENCES = "DATAREFERENCES";

    /** Name of column in {@link DataReference} table. */
    public static final String GUID = "GUID";

    /** Name of column in {@link DataReference} table. */
    public static final String TYPE = "TYPE";

    /** Name of column in {@link DataReference} table. */
    public static final String PARENT_GUID = "PARENT_GUID";

    /** Name of column in {@link DataReference} table. */
    public static final String PARENT_REVISION = "PARENT_REVISION";

    /** Name of column in {@link DataReference} table. */
    public static final String PARENT_EXT_HOST = "PARENT_EXT_HOST";

    /** Name of column in {@link DataReference} table. */
    public static final String PARENT_EXT_INSTANCE_ID = "PARENT_EXT_INSTANCE_ID";

    /** Name of table holding {@link Revision} information. */
    public static final String REVISIONS = "REVISIONS";

    /** Name of column in {@link Revision} table. */
    public static final String NUMBER = "NUMBER";

    /** Name of column in {@link Revision} table. */
    public static final String LOCATION = "LOCATION";

    /** Name of column in {@link Revision} table. */
    public static final String DATAREFERENCE_GUID = "DATAREFERENCE_GUID";

    /** Name of table holding {@link Revision} {@link MetaData} information. */
    public static final String METADATA = "METADATA";

    /** Name of table holding {@link Revision} independent {@link MetaData} information. */
    public static final String REFERENCE_METADATA = "REFERENCE_METADATA";

    /** Name of view holding the most recent {@link MetaData} information. */
    public static final String CURRENT_METADATA = "CURRENT_METADATA";

    /** Name of table holding the most recent {@link Revision} information. */
    public static final String HEAD_REVISION = "HEAD_REVISION";

    /** Name of table holding all {@link MetaData} information. */
    public static final String ALL_METADATA = "ALL_METADATA";

    /** Name of column in {@link MetaData} table. */
    public static final String REVISION_NUMBER = "REVISION_NUMBER";

    /** Name of column in {@link MetaData} table. */
    public static final String KEY = "KEY_";

    /** Name of column in {@link MetaData} table. */
    public static final String VALUE = "VALUE";
    
    /** Flag indication if the mata data is read only. */
    public static final String READ_ONLY = "READ_ONLY";
    
    private CatalogConstants() {
        
    }
}
