/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.commons;

/**
 * The permission categories for the data privileges.
 *   
 * @author Juergen Klein
 */
public enum DataPermissionCategories {
    
    /** Permission associated to create data references. */
    CREATEREFERENCE,
    /** Permission associated to delete revisions. */
    DELETEREVISION, 
    /** Permission associated to grant and revoke permissions. */
    GRANTANDREVOKEPERMISSIONS, 
    /** Permission associated to query the catalog. */
    QUERYCATALOG, 
    /** Permission associated to read files. */
    READFILE, 
    /** Permission associated to read the meta data of data references. */
    READMETADATA, 
    /** Permission associated to read the permissions. */
    READPERMISSIONS, 
    /** Permission associated to read the data reference. */
    READREFERENCE, 
    /** Permission associated to write to files. */
    WRITEFILE;

}
