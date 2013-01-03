/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.core.utils.common.concurrent.AsyncExceptionListener;
import de.rcenvironment.core.utils.common.concurrent.CallablesGroup;
import de.rcenvironment.core.utils.common.concurrent.SharedThreadPool;
import de.rcenvironment.core.utils.common.concurrent.TaskDescription;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.datamanagement.DistributedQueryService;
import de.rcenvironment.rce.datamanagement.QueryService;
import de.rcenvironment.rce.datamanagement.commons.DataReference;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResult;
import de.rcenvironment.rce.datamanagement.commons.MetaDataResultList;
import de.rcenvironment.rce.datamanagement.commons.Query;

/**
 * Implementation of {@link DistributedQueryServiceImpl}.
 * 
 * @author Doreen Seider
 * @author Robert Mischke (parallelized distributed query)
 */
public class DistributedQueryServiceImpl implements DistributedQueryService {

    private static final Log LOGGER = LogFactory.getLog(DistributedQueryServiceImpl.class);

    private CommunicationService communicationService;

    private PlatformService platformService;

    private BundleContext context;

    protected void activate(BundleContext bundleContext) {
        context = bundleContext;
    }

    protected void bindCommunicationService(CommunicationService newCommunicationService) {
        communicationService = newCommunicationService;
    }

    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    @Override
    public Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults) {
        Set<DataReference> references = new HashSet<DataReference>();

        for (PlatformIdentifier pi : communicationService.getAvailableNodes(false)) {
            QueryService queryService = (QueryService) communicationService.getService(QueryService.class, pi, context);
            try {
                references.addAll(queryService.executeQuery(proxyCertificate, query, maxResults));
            } catch (RuntimeException e) {
                LOGGER.warn("Failed to execute query on platform: " + pi, e);
            }
        }
        return references;
    }

    @Override
    public Collection<DataReference> executeQuery(User proxyCertificate, Query query, Integer maxResults, PlatformIdentifier platform) {
        if (platform == null) {
            platform = platformService.getPlatformIdentifier();
        }
        QueryService queryService = (QueryService) communicationService.getService(QueryService.class, platform, context);
        try {
            return queryService.executeQuery(proxyCertificate, query, maxResults);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to execute query on platform: " + platform, e);
            return new HashSet<DataReference>();
        }
    }

    @Override
    public MetaDataResultList executeMetaDataQuery(final User proxyCertificate, final Query query, final Integer maxResults) {
        // create parallel tasks
        CallablesGroup<MetaDataResultList> callablesGroup = SharedThreadPool.getInstance().createCallablesGroup(MetaDataResultList.class);
        for (PlatformIdentifier pi : communicationService.getAvailableNodes(false)) {
            final PlatformIdentifier pi2 = pi;
            callablesGroup.add(new Callable<MetaDataResultList>() {

                @Override
                @TaskDescription("Distributed metadata query")
                public MetaDataResultList call() throws Exception {
                    QueryService queryService = (QueryService) communicationService.getService(QueryService.class, pi2, context);
                    try {
                        return queryService.executeMetaDataQuery(proxyCertificate, query, maxResults);
                    } catch (RuntimeException e) {
                        LOGGER.warn("Failed to execute metadata query on platform: " + pi2, e);
                        return null;
                    }
                }
            });
        }
        // execute
        List<MetaDataResultList> parallelResults = callablesGroup.executeParallel(new AsyncExceptionListener() {

            @Override
            public void onAsyncException(Exception e) {
                LOGGER.warn("Asynchronous exception during parallel metadata query", e);
            }
        });
        // merge results
        MetaDataResultList mergedResult = new MetaDataResultList();
        for (MetaDataResultList singleResult : parallelResults) {
            if (singleResult != null) {
                for (final MetaDataResult subResult : singleResult) {
                    mergedResult.add(subResult);
                }
            }
        }
        // TODO do some global sorting-magic?
        return mergedResult;
    }

    @Override
    public MetaDataResultList executeMetaDataQuery(User proxyCertificate, Query query, Integer maxResults, PlatformIdentifier platform) {
        if (platform == null) {
            platform = platformService.getPlatformIdentifier();
        }
        QueryService queryService = (QueryService) communicationService.getService(QueryService.class, platform, context);
        try {
            return queryService.executeMetaDataQuery(proxyCertificate, query, maxResults);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to execute metadata query on platform: " + platform, e);
            return null;
        }
    }

    @Override
    public DataReference getReference(User proxyCertificate, UUID dataReferenceGuid, PlatformIdentifier platform)
        throws AuthorizationException {

        if (platform == null) {
            platform = platformService.getPlatformIdentifier();
        }
        QueryService queryService = (QueryService) communicationService.getService(QueryService.class, platform, context);
        try {
            return queryService.getReference(proxyCertificate, dataReferenceGuid);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to get reference on platform: " + platform, e);
            return null;
        }
    }

    @Override
    public DataReference getReference(User proxyCertificate, UUID dataReferenceGuid) throws AuthorizationException {
        return getReference(proxyCertificate, dataReferenceGuid, communicationService.getAvailableNodes(false));
    }

    @Override
    public DataReference getReference(User proxyCertificate, UUID dataReferenceGuid, Collection<PlatformIdentifier> platforms)
        throws AuthorizationException {
        DataReference reference = null;

        for (PlatformIdentifier pi : communicationService.getAvailableNodes(false)) {
            QueryService queryService = (QueryService) communicationService.getService(QueryService.class, pi, context);
            try {
                reference = queryService.getReference(proxyCertificate, dataReferenceGuid);
                if (reference != null) {
                    break;
                }
            } catch (RuntimeException e) {
                LOGGER.warn("Failed to get reference on platform: " + pi, e);
            }
        }

        return reference;
    }
}
