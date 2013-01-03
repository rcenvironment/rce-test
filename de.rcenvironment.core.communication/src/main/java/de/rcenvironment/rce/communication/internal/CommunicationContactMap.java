/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.internal;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.NetworkContact;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.configuration.ConfigurationService;

/**
 * Utility class for getting the contact information for a remote RCE instance.
 * 
 * It contains maps which hold routes from platform represented by host and instance name to the
 * next {@link NetworkContact}. These maps are used to save the information how a specified platform
 * can be reached. Besides exact matching it also does regex matching for the instance name and
 * checks if a hostname is in a network conforming the CIDR notation, e.g. 192.168.0.0/24.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 * @author Tobias Menden
 */
public final class CommunicationContactMap {

    private static final Log LOGGER = LogFactory.getLog(CommunicationConfiguration.class);

    private static final String ERROR_PARAMETERS_NULL = "The parameter \"{0}\" must not be null.";

    private static final String ERROR_NO_CONTACT = "Communication contact for platform (host:instance) \"{0}\" could not be found.";

    private static final String WARN_INVALID_CONFIGURATION = "Invalid communication configuration: ";

    private static final String COLON = ":";

    private static final String HOST_VARIABLE = "$host";

    private static Map<PlatformIdentifier, NetworkContact> serviceCallContacts =
        Collections.synchronizedMap(new HashMap<PlatformIdentifier, NetworkContact>());

    private static Map<PlatformIdentifier, NetworkContact> fileTransferContacts =
        Collections.synchronizedMap(new HashMap<PlatformIdentifier, NetworkContact>());

    private static ConfigurationService configurationService;

    private static CommunicationConfiguration configuration;

    /** Only called by OSGi. */
    @Deprecated
    public CommunicationContactMap() {}

    /**
     * Activation method called by OSGi.
     * 
     * @param context The injected {@link BundleContext}.
     **/
    public void activate(BundleContext context) {
        configuration = configurationService.getConfiguration(
            context.getBundle().getSymbolicName(), CommunicationConfiguration.class);
        setMappings(CommunicationType.FILE_TRANSFER, extractConfiguration(configuration.getFileTransferContacts()));
        setMappings(CommunicationType.SERVICE_CALL, extractConfiguration(configuration.getServiceCallContacts()));
    }

    /**
     * Deactivation method called by OSGi.
     * 
     * @param context The injected {@link BundleContext}.
     **/
    public void deactivate(BundleContext context) {
        configuration = null;
        removeAllMappings();
    }

    /**
     * Bind method called by OSGi.
     * 
     * @param newConfigurationService Service to bind.
     */
    public void bindConfigurationService(ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }

    /**
     * Unbind method called by OSGi.
     * 
     * @param oldConfigurationService Service to unbind.
     */
    public void unbindConfigurationService(ConfigurationService oldConfigurationService) {
        configurationService = null;
    }

    /**
     * Returns contact information for a RCE instance.
     * 
     * @param type The {@link CommunicationType} (service call or file transfer).
     * @param platformIdentifier The {@link PlatformIdentifier}.
     * 
     * @return The contact information.
     * @throws CommunicationException if no contact could be found.
     */
    public static NetworkContact getContact(CommunicationType type, PlatformIdentifier platformIdentifier)
        throws CommunicationException {

        Assertions.isDefined(type, MessageFormat.format(ERROR_PARAMETERS_NULL, "type"));
        Assertions.isDefined(platformIdentifier, MessageFormat.format(ERROR_PARAMETERS_NULL, "platformIdentifier"));

        NetworkContact contact = null;

        Map<PlatformIdentifier, NetworkContact> contacts = getContactsMap(type);
        synchronized (contacts) {

            for (PlatformIdentifier platform : contacts.keySet()) {
                final int minusOne = -1;
                if (NetworkUtils.isHostInNetwork(platformIdentifier.resolveHost(), platform.resolveHost())
                    && (platform.getPlatformNumber() == minusOne
                    || platformIdentifier.getPlatformNumber() == platform.getPlatformNumber())) {

                    contact = contacts.get(platform);
                    break;
                }
            }
        }

        if (contact == null) {
            for (PlatformIdentifier platform : contacts.keySet()) {
                final int minusOne = -1;
                if ((platform.getHost().equals(HOST_VARIABLE))
                    && (platform.getPlatformNumber() == minusOne
                    || platform.getPlatformNumber() == platformIdentifier.getPlatformNumber())) {

                    contact = contacts.get(platform);
                    if (contact.getHost().equals(HOST_VARIABLE)) {
                        contact = new NetworkContact(platformIdentifier.resolveHost(),
                            contacts.get(platform).getProtocol(),
                            contacts.get(platform).getPort());
                    }
                    break;
                }
            }
        }

        if (contact == null) {
            throw new CommunicationException(MessageFormat.format(ERROR_NO_CONTACT, platformIdentifier));
        }
        return contact;
    }

    /**
     * Sets a {@link NetworkContact} in the contacts map for a given platform represented by host
     * and instance name. If the platform already has a {@link NetworkContact} the old one will be
     * replace by the new one.
     * 
     * @param type The {@link CommunicationType}.
     * @param platformIdentifier The {@link PlatformIdentifier} of the platform to add.
     * @param contact The {@link NetworkContact} for the platform.
     */
    public static void setMapping(CommunicationType type, PlatformIdentifier platformIdentifier, NetworkContact contact) {
        getContactsMap(type).put(platformIdentifier, contact);
    }

    /**
     * Adds a route from a platform (represented by host and instance name) to a
     * {@link NetworkContact} to the contacts map.
     * 
     * @param type The {@link NetworkContact}.
     * @param contacts A map from platform (host:instance) to {@link NetworkContact}.
     */
    public static void setMappings(CommunicationType type, Map<PlatformIdentifier, NetworkContact> contacts) {
        getContactsMap(type).putAll(contacts);
    }

    /**
     * Removes a {@link NetworkContact} from the contacts map and returns it.
     * 
     * @param type The <code>CommunicationType</code>.
     * @param platformIdentifier The {@link PlatformIdentifier} of the platform to remove.
     * @return The <code>CommunicationContact</code> for the removed platform or null if no route
     *         found.
     */
    public static NetworkContact removeMapping(CommunicationType type, PlatformIdentifier platformIdentifier) {
        NetworkContact contact = getContactsMap(type).remove(platformIdentifier);
        return contact;
    }

    /**
     * Removes all entries from the contacts map.
     */
    public static void removeAllMappings() {
        serviceCallContacts.clear();
        fileTransferContacts.clear();
    }

    /**
     * Returns the contacts map to operate on.
     * 
     * @param type The communication type.
     * @return A map from platform (host:instance) to <code>CommunicationContact</code> to operate
     *         on.
     */
    private static Map<PlatformIdentifier, NetworkContact> getContactsMap(CommunicationType type) {
        // check that activate was called thus the configuration was read
        if (configuration == null) {
            throw new IllegalStateException("communication configuration not read yet, "
                + "because communication contact map was not activated until now");
        }

        if (type == CommunicationType.FILE_TRANSFER) {
            return fileTransferContacts;
        } else {
            return serviceCallContacts;
        }
    }

    /**
     * Returns a <code>Map</code> from platform (host:instance) to <code>CommunicationContact</code>
     * with all information got from the configuration service.
     * 
     * @param key The key to read from.
     * @return A <code>Map</code> from platform (host:instance) to <code>CommunicationContact</code>
     *         .
     */
    private Map<PlatformIdentifier, NetworkContact> extractConfiguration(List<String> value) {

        Map<PlatformIdentifier, NetworkContact> config = new HashMap<PlatformIdentifier, NetworkContact>();

        if (value != null && !value.isEmpty()) {
            for (String route : value) {
                try {
                    String[] parts = route.split("=");
                    String target = parts[0];
                    String[] contact = parts[1].split(COLON);

                    config.put(PlatformIdentifierFactory.fromHostAndNumberString(target), new NetworkContact(contact[0], contact[1],
                        new Integer(contact[2])));
                } catch (NumberFormatException e) {
                    LOGGER.warn(WARN_INVALID_CONFIGURATION + route);
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOGGER.warn(WARN_INVALID_CONFIGURATION + route);
                }
            }
        }
        return config;
    }
}
