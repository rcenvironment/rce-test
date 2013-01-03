/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

import de.rcenvironment.commons.FileSupport;
import de.rcenvironment.core.utils.common.security.AllowRemoteAccess;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.authorization.AuthorizationException;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformService;
import de.rcenvironment.rce.component.ComponentBundleListener;
import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentContext;
import de.rcenvironment.rce.component.ComponentController;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.ComponentRegistry;
import de.rcenvironment.rce.component.ComponentUtils;
import de.rcenvironment.rce.component.DeclarativeComponentDescription;
import de.rcenvironment.rce.component.Registerable;
import de.rcenvironment.rce.configuration.ConfigurationService;
import de.rcenvironment.rce.notification.DistributedNotificationService;

/**
 * Implementation of the {@link ComponentRegistry}.
 * 
 * @author Roland Gude
 * @author Jens Ruehmkorf
 * @author Doreen Seider
 * @author Heinrich Wendel
 */
public class ComponentRegistryImpl implements ComponentRegistry {
    
    private static final Log LOGGER = LogFactory.getLog(ComponentRegistryImpl.class);

    private final ComponentDescriptionRegistry compDescRegistry = new ComponentDescriptionRegistry();

    private ComponentInstanceRegistry controllerInstanceRegistry = new ComponentInstanceRegistry();

    private List<ServiceReference> unhandledCompControllers = new ArrayList<ServiceReference>();

    private PlatformService platformService;

    private DistributedNotificationService notificationService;

    private ConfigurationService configurationService;
    
    private PlatformIdentifier localPlatform;

    private ServiceReference compContrServiceRef;
    
    private ComponentBundleConfiguration configuration;

    private org.osgi.service.component.ComponentContext componentCtx;

    protected synchronized void activate(org.osgi.service.component.ComponentContext newContext) {
        componentCtx = newContext;

        localPlatform = platformService.getPlatformIdentifier();
        for (ServiceReference component : unhandledCompControllers) {
            addComponent(component);
        }
        unhandledCompControllers.clear();

        Runnable r = new Runnable() {

            @Override
            public void run() {
                // start all future Bundles providing a Component
                componentCtx.getBundleContext().addBundleListener(new ComponentBundleListener());
                // start all current Bundles providing a Component
                for (Bundle b : componentCtx.getBundleContext().getBundles()) {
                    ComponentBundleListener.handleBundle(b);
                }
            }
        };
        Executors.newFixedThreadPool(1).execute(r);
        
        configuration = configurationService.getConfiguration(componentCtx.getBundleContext().getBundle().getSymbolicName(),
            ComponentBundleConfiguration.class);
        
    }

    protected void bindPlatformService(PlatformService newPlatformService) {
        platformService = newPlatformService;
    }

    protected void bindDistributedNotificationService(DistributedNotificationService newNotificationService) {
        notificationService = newNotificationService;
    }

    protected void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }

    protected void bindComponentControllerFactory(ServiceReference newServiceReference) {
        compContrServiceRef = newServiceReference;
    }

    /**
     * Bind method called by the OSGi framework, if a new Component Factory of type
     * de.rcenvironment.rce.component was registered. The component registration stuff is done here.
     */
    @SuppressWarnings("unchecked")
    protected synchronized void addComponent(ServiceReference reference) {
        // if this bundle is not activated yet, store the component controller and handle it after
        // activation within the activate method
        if (componentCtx == null) {
            unhandledCompControllers.add(reference);
            return;
        }

        ComponentFactory compFactory = (ComponentFactory) componentCtx.getBundleContext().getService(reference);

        String tmpIdentifier = UUID.randomUUID().toString();
        Dictionary<String, String> serviceProperties = new Hashtable<String, String>();
        serviceProperties.put(ComponentConstants.COMP_INSTANCE_ID_KEY, tmpIdentifier);
        ComponentInstance instance;

        try {
            instance = compFactory.newInstance(serviceProperties);     
        } catch (RuntimeException e) {
            LOGGER.error("Componente could not be loaded because of an error in xml file or components constructor: " + e.getMessage());
            LOGGER.error(Arrays.toString(e.getStackTrace()));
            return;
        }

        String filter = "(" + ComponentConstants.COMP_INSTANCE_ID_KEY + "=" + tmpIdentifier + ")";

        ServiceReference[] references;
        try {
            references = componentCtx.getBundleContext().getAllServiceReferences(Registerable.class.getName(), filter);

            if (references == null || references.length != 1) {
                throw new IllegalStateException("No one or more than one component with this identifier found: " + tmpIdentifier);
            }

        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException("Invalid syntax. This is a bug.", e);
        }

        Map<String, Object> properties = new HashMap<String, Object>();

        // FIXME: review sense of for loop and properties map.
        for (String key : references[0].getPropertyKeys()) {
            properties.put(key, references[0].getProperty(key));
        }

        reference = references[0];
        String name = (String) reference.getProperty(ComponentConstants.COMPONENT_NAME_KEY);
        String group = (String) reference.getProperty(ComponentConstants.COMPONENT_NAME_GROUP);
        String identifier = (String) reference.getProperty(ComponentConstants.COMPONENT_CLASS_KEY)
            + ComponentConstants.COMPONENT_ID_SEPARATOR + name;
        String version = (String) references[0].getProperty(ComponentConstants.VERSION_DEF_KEY);

        String[] inputData = ((String[]) properties.get(ComponentConstants.INPUTS_DEF_KEY));
        Map<String, String> inputMetaData = null;
        
        if (inputData != null){
            Object[] result = ComponentUtils.divideProperty(inputData);
            inputData = (String[]) result[0];
            inputMetaData = (Map<String, String >) result[1];
        }
        

        String[] outputData = ((String[]) properties.get(ComponentConstants.OUTPUTS_DEF_KEY));
        Map<String, String> outputMetaData = null;

        if (outputData != null){
            Object[] result = ComponentUtils.divideProperty(outputData);
            outputData = (String[]) result[0];
            outputMetaData = (Map<String, String >) result[1];
        }
        
        Map<String, Class<? extends Serializable>> inputDefs = ComponentUtils
            .parsePropertyForConfigTypes(inputData);
        Map<String, Class<? extends Serializable>> outputDefs = ComponentUtils
            .parsePropertyForConfigTypes(outputData);
        Map<String, Class<? extends Serializable>> configDefs = ComponentUtils
            .parsePropertyForConfigTypes((String[]) properties.get(ComponentConstants.CONFIGURATION_DEF_KEY));

        Map<String, String> rawDefaultConfig = ComponentUtils
            .parsePropertyForConfigValues((String[]) properties.get(ComponentConstants.CONFIGURATION_DEF_KEY));
        Map<String, Serializable> defaultConfig = ComponentUtils.convertConfigurationValues(configDefs, rawDefaultConfig);

        
        Map<String, Map<String, Serializable>> inputMetaDefs = ComponentUtils.parsePropertyForMetaTypes(inputMetaData);

        Map<String, Map<String, Serializable>> outputMetaDefs = ComponentUtils.parsePropertyForMetaTypes(outputMetaData);

        Map<String, Map<String, String>> placeholderAttributesDefs = ComponentUtils
            .parsePlaceholderAttributes((String[]) properties.get(ComponentConstants.PLACEHOLDER_ATTRIBUTES_DEF_KEY));
        
        byte[] icon16 = readIcon(ComponentConstants.ICON_16_KEY, references[0]);
        byte[] icon32 = readIcon(ComponentConstants.ICON_32_KEY, references[0]);

        DeclarativeComponentDescription declarativeCD = new DeclarativeComponentDescription(identifier, name, group, version,
            inputDefs, outputDefs, inputMetaDefs, outputMetaDefs, configDefs, placeholderAttributesDefs, defaultConfig, icon16, icon32);

        ComponentDescription componentDescription = new ComponentDescription(declarativeCD);
        componentDescription.setPlatform(localPlatform);

        compDescRegistry.addComponent(componentDescription, compFactory);
        LOGGER.info("Registered component: " + componentDescription.getIdentifier());

        instance.dispose();

    }

    protected synchronized void removeComponent(ComponentFactory factory) {
        String identifier = compDescRegistry.removeComponent(factory);
        LOGGER.info("Removed Component: " + identifier);
    }

    // not remotely accessible because getComponentDescriptions(User user, PlatformIdentifier requestingPlatform) must be used
    // to ensure filtering of remotely provided components is done
    // keep this method as part of the API to be able to switch back later on if filtering is done on user base and filtering on platform
    // base is removed again -- seid_do
    @Override
    public Set<ComponentDescription> getComponentDescriptions(User user) {
        checkCertificate(user);
        return compDescRegistry.getComponentDescriptions();
    }

    @Override
    @AllowRemoteAccess
    public Set<ComponentDescription> getComponentDescriptions(User user, PlatformIdentifier requestingPlatform) {
        checkCertificate(user);
        if (configuration.getAllComponentsRemotelyAccessible() || localPlatform.equals(requestingPlatform)) {
            return getComponentDescriptions(user);
        } else {
            return filterComponentDescriptions(getComponentDescriptions(user));
        }
    }
    
    private Set<ComponentDescription> filterComponentDescriptions(Set<ComponentDescription> installedCompDescs) {
        Set<ComponentDescription> hostedCompDescs = new HashSet<ComponentDescription>();
        List<String> hostedCompDescIds = configuration.getPublishedComponents();
        for (ComponentDescription comDesc : installedCompDescs) {
            if (hostedCompDescIds.contains(comDesc.getIdentifier())) {
                hostedCompDescs.add(comDesc);
            }
        }
        return hostedCompDescs;
    }

    @Override
    public ComponentDescription getComponentDescription(User user, String compDescId) {
        checkCertificate(user);
        return compDescRegistry.getComponentDescription(compDescId);
    }

    @Override
    @AllowRemoteAccess
    public ComponentInstanceDescriptor createComponentInstance(User user, ComponentDescription compDesc,
        ComponentContext compCtx, String compName, Boolean inputConnected) throws ComponentException {
        checkCertificate(user);

        ComponentFactory factory = compDescRegistry.getComponentFactory(compDesc.getIdentifier());

        if (factory == null) {
            throw new IllegalArgumentException("A component with the given identifier is not installed: " + compDesc.getIdentifier());
        }

        String controllerId = UUID.randomUUID().toString();
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(ComponentConstants.COMP_INSTANCE_ID_KEY, controllerId);
        ComponentInstance compControllerInstance = ((ComponentFactory) componentCtx.getBundleContext()
                .getService(compContrServiceRef)).newInstance(properties);
        ComponentController compController = (ComponentController) compControllerInstance.getInstance();
        ((ComponentControllerImpl) compController).setConfigurationService(configurationService);
        ((ComponentControllerImpl) compController).setDistributedNotificationService(notificationService);

        ComponentInstanceDescriptor compInstanceDesc = compController.initialize(user, controllerId,
            compDesc.getClassName(), compName, compDesc, compCtx, inputConnected);

        controllerInstanceRegistry.addCompControllerInstance(compControllerInstance, compInstanceDesc, user);            

        return compInstanceDesc;

    }

    @Override
    @AllowRemoteAccess
    public void disposeComponentInstance(User user, String instanceIdentifier) throws AuthorizationException {
        checkCertificate(user);

        final ComponentInstance componentInstance = controllerInstanceRegistry.getComponentInstance(instanceIdentifier);
        if (componentInstance == null) {
            throw new IllegalArgumentException("Component instance with the given identifier not found: " + instanceIdentifier);
        }
        checkCertificate(user, instanceIdentifier);

        ComponentInstanceDescriptor compInstanceDesc = controllerInstanceRegistry.getComponentInstanceDescriptor(instanceIdentifier);
        File workdir = new File(compInstanceDesc.getWorkingDirectory());
        FileSupport.deleteFile(workdir);
        componentInstance.dispose();
        controllerInstanceRegistry.removeComponentInstance(instanceIdentifier);
    }

    @Override
    public ComponentInstanceDescriptor getComponentInstanceDescriptor(User user, String instanceIdentifier) {
        checkCertificate(user);
        checkCertificate(user, instanceIdentifier);

        return controllerInstanceRegistry.getComponentInstanceDescriptor(instanceIdentifier);
    }

    @Override
    public boolean isCreator(String instanceInformationIdentifier, User user) {
        checkCertificate(user);
        return controllerInstanceRegistry.isCreator(instanceInformationIdentifier, user);
    }

    private byte[] readIcon(String key, ServiceReference reference) {
        String iconName = (String) reference.getProperty(key);
        if (iconName != null) {
            URL url = reference.getBundle().getResource(iconName);
            if (url != null) {
                try {
                    InputStream stream = url.openStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    while (true) {
                        int r = stream.read();
                        if (r < 0) {
                            break;
                        }
                        bos.write(r);
                    }
                    return bos.toByteArray();
                } catch (IOException e) {
                    LOGGER.warn("Cannot read icon: " + iconName);
                    return null;
                }
            } else {
                LOGGER.warn("Icon not found: " + iconName);
            }
        }
        return null;
    }

    private void checkCertificate(final User user) {
        if (!user.isValid()) {
            throw new IllegalArgumentException("User certificate must not be invalid!");
        }
    }

    private void checkCertificate(final User user, final String instanceIdentifier) {
        if (!isCreator(instanceIdentifier, user)) {
            throw new AuthorizationException("A Component information can only ba accessed "
                + "by the user who created the associated Component.");
        }
    }

}
