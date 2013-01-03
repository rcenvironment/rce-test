/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.datamanagement.internal;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.datamanagement.backend.CatalogBackend;
import de.rcenvironment.rce.datamanagement.backend.DataBackend;
import de.rcenvironment.rce.datamanagement.commons.DataReference.DataReferenceType;

/**
 * Utility class returning backend services.
 * 
 * @author Doreen Seider
 * @author Juergen Klein
 */
public final class BackendSupport {

    private static final String CLOSE_BRACKET = ")";
    
    private static final String OPEN_BRACKET = "(";
  
    private static final String EQUALS_SIGN = "=";
   
    private static final Log LOGGER = LogFactory.getLog(BackendSupport.class);
    
    private static DataManagementConfiguration dmConfig;
    
    private static BundleContext bundleContext;
    
    private static ConfigurationService configurationService;

    protected void activate(BundleContext context) {
        bundleContext = context;
        dmConfig = configurationService.getConfiguration(context.getBundle().getSymbolicName(), DataManagementConfiguration.class);
    }
    
    protected void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }

    protected void unbindConfigurationService(ConfigurationService oldConfigurationService) {
        configurationService = ServiceUtils.createNullService(ConfigurationService.class);
    }

    /**
     * @return the configured {@link CatalogBackend} to use.
     */
    public static CatalogBackend getCatalogBackend() {
        
        String catalogProvider = dmConfig.getCatalogBackend();
        CatalogBackend catalogBackend = null;
        String filterString = OPEN_BRACKET + CatalogBackend.PROVIDER + EQUALS_SIGN + catalogProvider + CLOSE_BRACKET;
        ServiceReference[] serviceReferences = null;
        try {
            serviceReferences = bundleContext.getServiceReferences(CatalogBackend.class.getName(), filterString);
        } catch (InvalidSyntaxException e) {
            LOGGER.error("Failed to get a catalog backend. Invalid protocol filter syntax.");
        }
        if (serviceReferences != null && serviceReferences.length > 0) {
            catalogBackend = (CatalogBackend) bundleContext.getService(serviceReferences[0]);
            if (catalogBackend == null) {
                throw new IllegalStateException("The configured catalog backend is not available: " + catalogProvider);
            }
        } else {
            throw new IllegalStateException("The configured catalog backend is not available: " + catalogProvider);
        }
        return catalogBackend;
    }

    /**
     * @param dataURI The {@link URI} of the data the {@link DataBackend} should handle.
     * @return the configured {@link DataBackend} responsible handling the given {@link URI}.
     */
    public static DataBackend getDataBackend(URI dataURI) {

        String scheme = dataURI.getScheme();
        DataBackend dataBackend = null;
        String filterString = OPEN_BRACKET + DataBackend.SCHEME + EQUALS_SIGN + scheme + CLOSE_BRACKET;
        ServiceReference[] serviceReferences = null;
        try {
            serviceReferences = bundleContext.getServiceReferences(DataBackend.class.getName(), filterString);
        } catch (InvalidSyntaxException e) {
            LOGGER.error("Failed to get a cdata backend. Invalid protocol filter syntax.");
        }
        if (serviceReferences != null && serviceReferences.length > 0) {
            dataBackend = (DataBackend) bundleContext.getService(serviceReferences[0]);
            if (dataBackend == null) {
                throw new IllegalStateException("A data backend for this scheme is not available: " + scheme);
            }
        } else {
            throw new IllegalStateException("A data backend for this scheme is not available: " + scheme);
        }
        return dataBackend;
    }
    
    /**
     * @param dataType The type of the data to handle.
     * @return the configured {@link DataBackend} responsible handling the given {@link DataReferenceType}.
     */
    public static DataBackend getDataBackend(DataReferenceType dataType) {
        
        String dataBackendProvider = null;
        
        if (dataType.equals(DataReferenceType.fileObject)) {
            dataBackendProvider = dmConfig.getFileDataBackend();
        } else {
            throw new IllegalArgumentException("The given data type is not supported: " + dataType);
        }
        
        DataBackend dataBackend = null;
        String filterString = OPEN_BRACKET + DataBackend.PROVIDER + EQUALS_SIGN + dataBackendProvider + CLOSE_BRACKET;
        ServiceReference[] serviceReferences = null;
        try {
            serviceReferences = bundleContext.getServiceReferences(DataBackend.class.getName(), filterString);
        } catch (InvalidSyntaxException e) {
            LOGGER.error("Failed to get a data backend. Invalid protocol filter syntax.");
        }
        if (serviceReferences != null && serviceReferences.length > 0) {
            dataBackend = (DataBackend) bundleContext.getService(serviceReferences[0]);
            if (dataBackend == null) {
                throw new IllegalStateException("The configured data backend is not available: " + dataBackendProvider);
            }
        } else {
            throw new IllegalStateException("The configured data backend is not available: " + dataBackendProvider);
        }
        return dataBackend;
    }

}
