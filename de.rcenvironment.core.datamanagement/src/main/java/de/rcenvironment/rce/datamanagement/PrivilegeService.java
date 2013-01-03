/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement;

import java.util.Map;
import java.util.Set;

import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.datamanagement.commons.DataPermissionCategories;
import de.rcenvironment.rce.datamanagement.commons.DataReference;

/**
 * Interface for the RCE user privilege system.
 * 
 * @author Juergen Klein
 */
public interface PrivilegeService {

    /**
     * 
     * Returns a {@link Map} with the {@link DataPermissionCategories} and the entity ids which own
     * the permissions on the passed {@link DataReference}.
     * 
     * @param proxyCertificate The {@link User} of the user.
     * @param dataReference The {@link DataReference} the permissions are requested for
     * @return A {@link Map} of {@link DataPermissionCategories} and entity ids
     * @throws AuthorizationException if the requesting suer does not own permissions on the passed
     *         {@link DataReference}
     */
    Map<DataPermissionCategories, String> getPermissionsAndEntities(User proxyCertificate, DataReference dataReference)
        throws AuthorizationException;

    /**
     * 
     * Grants {@link DataPermissionCategories} for the catalog to a {@link Role}.
     * 
     * Must own the permission {@link DataPermissionCategories}.GRANTANDREVOKEPERMISSIONS
     * @param proxyCertificate The {@link User} of the user.
     * @param dataPermissionCategories The {@link DataPermissionCategories} to grant
     * @param entityIds The entity ids to grant the catalog permissions to. An entity id can be of
     *        the type String (user id of a {@link User} or role id of a {@link Role}.
     * 
     * @throws AuthorizationException if the user does not own the permission to grant permissions
     *         on the catalog
     */
    void grantCatalogPermissions(User proxyCertificate, Set<DataPermissionCategories> dataPermissionCategories,
        Set<String> entityIds) throws AuthorizationException;

    /**
     * 
     * Grants {@link DataPermissionCategories} for a {@link DataReference} to a {@link Role}.
     * @param proxyCertificate The {@link User} of the user.
     * @param dataReference The {@link DataReference} to grant the data permissions for
     * @param dataPermissionCategories The {@link DataPermissionCategories} to grant
     * @param entityIds The entity ids to grant the data permissions to. An entity id can be of the
     *        type String (user id of a {@link User} or role id of a {@link Role}.
     * 
     * @throws AuthorizationException if the user does not own the permission to grant permissions
     *         on this {@link DataReference}
     */
    void grantDataPermissions(User proxyCertificate, DataReference dataReference,
        Set<DataPermissionCategories> dataPermissionCategories,
        Set<String> entityIds) throws AuthorizationException;

    /**
     * 
     * Revokes {@link DataPermissionCategories} for the catalog from a {@link Role}.
     * 
     * Must own the permission {@link DataPermissionCategories}.GRANTANDREVOKEPERMISSIONS
     * @param proxyCertificate The {@link User} of the user.
     * @param dataPermissionCategories The {@link DataPermissionCategories} to revoke
     * @param entityIds The entities to revoke the catalog permissions from. An entity id can be
     *        of the type String (user id of a {@link User} or role id of a {@link Role}
     *        .
     * 
     * @throws AuthorizationException if the user does not own the permission to revoke permissions
     *         on the catalog
     */
    void revokeCatalogPermissions(User proxyCertificate, Set<DataPermissionCategories> dataPermissionCategories,
        Set<String> entityIds) throws AuthorizationException;

    /**
     * 
     * Revokes {@link DataPermissionCategories} from a {@link DataReference} to a {@link Role}.
     * @param proxyCertificate The {@link User} of the user.
     * @param dataReference The {@link DataReference} to revoke the data permissions from
     * @param dataPermissionCategories The {@link DataPermissionCategories} to revoke
     * @param entityIds The entity ids to revoke the data permissions from. An entity id can be of
     *        the type String (user id of a {@link User} or role id of a {@link Role}.
     * 
     * @throws AuthorizationException if the user does not own the permission to revoke permissions
     */
    void revokeDataPermissions(User proxyCertificate, DataReference dataReference,
        Set<DataPermissionCategories> dataPermissionCategories,
        Set<String> entityIds) throws AuthorizationException;
}
