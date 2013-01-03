/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.utils;

import java.util.HashMap;
import java.util.Map;

import de.rcenvironment.core.communication.model.NodeIdentifier;
import de.rcenvironment.core.communication.routing.internal.LinkStateAdvertisement;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;

/**
 * Helps to access/produce/filter meta data information that is attached to network messages.
 * 
 * <pre>
 * "category", "routing"|"communication"|...
 * "topic", "lsa"|"forward"|....
 * "receiver", {@link NodeIdentifier}
 * "hop-count", 1..n
 * "statuscode", 1|2|3|...
 * "trace", ...
 * </pre>
 * 
 * @author Phillip Kroll
 */
public class MetaDataWrapper {

    private static final String KEY_CATEGORY = "category";

    private static final String VALUE_CATEGORY_ROUTING = "routing";

    private static final String VALUE_CATEGORY_HEALTH_CHECK = "healthCheck";

    private static final String KEY_TOPIC = "topic";

    private static final String KEY_TYPE = "type";

    private static final String VALUE_TYPE_MESSAGE = "message";

    private static final String VALUE_TYPE_CONFIRMATION = "confirmation";

    private static final String VALUE_TYPE_FAILURE = "failure";

    private static final String KEY_HOPCOUNT = "hopcount";

    private static final String KEY_RECEIVER = "receiver";

    private static final String KEY_SENDER = "sender";

    private static final String KEY_MESSAGEID = "messageid";

    private static final String KEY_TRACE = "trace";

    private Map<String, String> filter;

    public MetaDataWrapper() {
        filter = new HashMap<String, String>();
    }

    public MetaDataWrapper(Map<String, String> metaData) {
        filter = metaData;
    }

    /**
     * @return An empty instance.
     */
    public static MetaDataWrapper createEmpty() {
        return new MetaDataWrapper();
    }

    /**
     * @return An empty routing instance.
     */
    public static MetaDataWrapper createRouting() {
        return MetaDataWrapper.createEmpty().setCategoryRouting();
    }

    /**
     * Creates the metadata for an {@link LinkStateAdvertisement} message.
     * 
     * @return the generated {@link MetaDataWrapper}
     */
    public static MetaDataWrapper createLsaMessage() {
        return MetaDataWrapper.createEmpty().setCategoryRouting().setTopicLsa();
    }

    /**
     * Creates the basic metadata for a "routed" message.
     * 
     * FIXME review; clarify against {@link #createRoutedMessage()}
     * 
     * @return the generated {@link MetaDataWrapper}
     */
    public static MetaDataWrapper createRouted() {
        return MetaDataWrapper.createRouting().
            setTopicRouted();
    }

    /**
     * Creates the basic metadata for a "routed" message.
     * 
     * FIXME review; clarify against {@link #createRouted()}
     * 
     * @return the generated {@link MetaDataWrapper}
     */
    public static MetaDataWrapper createRoutedMessage() {
        return MetaDataWrapper.createRouted().
            setTypeMessage();
    }

    /**
     * Creates the metadata for a "confirmation" message.
     * 
     * TODO check: obsolete?
     * 
     * @return the generated {@link MetaDataWrapper}
     */
    @Deprecated
    public static MetaDataWrapper createRoutedConfirmation() {
        return MetaDataWrapper.createRouted().
            setTypeConfirmation();
    }

    /**
     * Creates the metadata for a "failure" message.
     * 
     * TODO check: obsolete?
     * 
     * @return the generated {@link MetaDataWrapper}
     */
    public static MetaDataWrapper createRoutedFailure() {
        return MetaDataWrapper.createRouted().
            setTypeFailure();
    }

    /**
     * Creates the metadata used for both health check requests and responses.
     * 
     * @return the generated {@link MetaDataWrapper}
     */
    public static MetaDataWrapper createHealthCheckMetadata() {
        return createEmpty().setValue(KEY_CATEGORY, VALUE_CATEGORY_HEALTH_CHECK);
    }

    /**
     * @param metaData The data structure.
     * @return The instance created from a given data structure.
     */
    public static MetaDataWrapper wrap(Map<String, String> metaData) {
        return new MetaDataWrapper(metaData);
    }

    /**
     * Factory method that clones the given map and wraps it into a {@link MetaDataWrapper}.
     * 
     * @param metaData the map to clone and wrap
     * @return the generated {@link MetaDataWrapper}
     */
    public static MetaDataWrapper cloneAndWrap(Map<String, String> metaData) {
        return new MetaDataWrapper(new HashMap<String, String>(metaData));
    }

    /**
     * Clones and returns the internal metadata map.
     * 
     * @return an independent clone of the internal map
     */
    public Map<String, String> cloneData() {
        Map<String, String> clone = new HashMap<String, String>();
        clone.putAll(filter);
        return clone;
    }

    /**
     * Tests if all key/value-pairs match with the internal data.
     * 
     * @param metaData The meta data.
     * @return Whether data structures match.
     */
    public boolean matches(Map<String, String> metaData) {
        boolean result = true;
        for (String key : filter.keySet()) {
            result &= metaData.containsKey(key);
            if (!result) {
                return false;
            }
            result &= metaData.get(key).equals(filter.get(key));
            if (!result) {
                return false;
            }
        }
        return result;
    }

    /**
     * TODO krol_ph: Enter comment!
     * 
     * @param data The meta data handler.
     * @return Whether it matches.
     */
    public boolean matches(MetaDataWrapper data) {
        return matches(data.getInnerMap());
    }

    /**
     * TODO krol_ph: Enter comment!
     * 
     * @param key The map key.
     * @param value The map value.
     * @return Whether it matches.
     */
    public boolean matches(String key, String value) {
        return filter.containsKey(key) && filter.get(key).equals(value);
    }

    /**
     * TODO krol_ph: Enter comment!
     * 
     * @param value The value.
     * @return Whether it matches a topic.
     */
    public boolean matchesTopic(String value) {
        return filter.containsKey(KEY_TOPIC) && filter.get(KEY_TOPIC).equals(value);
    }

    /**
     * TODO krol_ph: Enter comment!
     * 
     * @param value The value.
     * @return Whether it matches a category.
     */
    public boolean matchesCategory(String value) {
        return filter.containsKey(KEY_CATEGORY) && filter.get(KEY_CATEGORY).equals(value);
    }

    /**
     * @return The map.
     */
    public Map<String, String> getInnerMap() {
        return filter;
    }

    /**
     * @param key The key.
     * @return The value assigned to the key.
     */
    public String getValue(String key) {
        String result = filter.get(key);
        if (result != null) {
            return filter.get(key);
        } else {
            // do never return null
            return "";
        }
    }

    /**
     * TODO krol_ph: Enter comment!
     * 
     * @param key The map key.
     * @param value The map value.
     * @return Itself.
     */
    public MetaDataWrapper setValue(String key, String value) {
        filter.put(key, value);
        return this;
    }

    /**
     * Setting the <code>category</code> to '<code>routing</code>'.
     * 
     * @return Returning itself.
     */
    public MetaDataWrapper setCategoryRouting() {
        setValue(KEY_CATEGORY, VALUE_CATEGORY_ROUTING);
        return this;
    }

    /**
     * Sets the value for {@link #KEY_TYPE} to {@link #VALUE_TYPE_MESSAGE}.
     * 
     * TODO rework or add semantic description
     * 
     * @return self
     */
    public MetaDataWrapper setTypeMessage() {
        setValue(KEY_TYPE, VALUE_TYPE_MESSAGE);
        return this;
    }

    /**
     * Sets the value for {@link #KEY_TYPE} to {@link #VALUE_TYPE_CONFIRMATION}.
     * 
     * TODO review: obsolete?
     * 
     * @return self
     */
    public MetaDataWrapper setTypeConfirmation() {
        setValue(KEY_TYPE, VALUE_TYPE_CONFIRMATION);
        return this;
    }

    /**
     * Sets the value for {@link #KEY_TYPE} to {@link #VALUE_TYPE_FAILURE}.
     * 
     * TODO review: obsolete?
     * 
     * @return self
     */
    public MetaDataWrapper setTypeFailure() {
        setValue(KEY_TYPE, VALUE_TYPE_FAILURE);
        return this;
    }

    /**
     * Setting the <code>topic</code> to '<code>lsa</code>' (link state advertisement).
     * 
     * @return Returning itself.
     */
    public MetaDataWrapper setTopicLsa() {
        setValue(KEY_TOPIC, "lsa");
        return this;
    }

    /**
     * Setting the <code>topic</code> to '<code>forward</code>' (forwarded routing message).
     * 
     * @return Returning itself.
     */
    public MetaDataWrapper setTopicRouted() {
        setValue(KEY_TOPIC, "routed");
        return this;
    }

    /**
     * TODO krol_ph: Enter comment!
     * 
     * @param receiver The receiver {@link NodeIdentifier}
     * @return Itself.
     */
    public MetaDataWrapper setReceiver(NodeIdentifier receiver) {
        setValue(KEY_RECEIVER, receiver.getNodeId());
        return this;
    }

    /**
     * Sets the {@link #KEY_SENDER} field.
     * 
     * @param sender the sender to set
     * @return self
     */
    public MetaDataWrapper setSender(NodeIdentifier sender) {
        setValue(KEY_SENDER, sender.getNodeId());
        return this;
    }

    /**
     * Sets the {@link #KEY_MESSAGEID} field.
     * 
     * @param id the message id to set
     * @return self
     */
    public MetaDataWrapper setMessageId(String id) {
        setValue(KEY_MESSAGEID, id);
        return this;
    }

    /**
     * 
     * TODO krol_ph: Enter comment!
     * 
     * @param text Any string.
     * @return Itself.
     */
    public MetaDataWrapper addTraceItem(String text) {
        setValue(KEY_TRACE, getValue(KEY_TRACE) + text + ",");
        return this;
    }

    /**
     * Increment hop count to check for maximum time to live.
     * 
     * @return Itself.
     */
    public MetaDataWrapper incHopCount() {
        if (filter.containsKey(KEY_HOPCOUNT)) {
            setValue(KEY_HOPCOUNT, Integer.toString(getHopCount() + 1));
        } else {
            setValue(KEY_HOPCOUNT, "1");
        }
        return this;
    }

    /**
     * @return The trace.
     */
    public String getTrace() {
        return getValue(KEY_TRACE);
    }

    /**
     * @return The receiver.
     */
    public NodeIdentifier getReceiver() {
        return PlatformIdentifierFactory.fromNodeId(getValue(KEY_RECEIVER));
    }

    /**
     * @return The sender.
     */
    public NodeIdentifier getSender() {
        return PlatformIdentifierFactory.fromNodeId(getValue(KEY_SENDER));
    }

    /**
     * @return The hash.
     */
    public String getMessageId() {
        return getValue(KEY_MESSAGEID);
    }

    /**
     * @return The hop count.
     */
    public int getHopCount() {
        try {
            return Integer.parseInt(getValue(KEY_HOPCOUNT));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
