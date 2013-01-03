/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.workflow;

import java.io.Serializable;
import java.util.EventObject;

import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentDescription.EndpointNature;

/**
 * Event informing about a change in a specific channel.
 * 
 * @author Christian Weiss
 */
public abstract class ChannelEvent extends EventObject {

    /**
     * Type of the {@link ChannelEvent}.
     *
     * @author Christian Weiss
     */
    public enum Type {
        /** Channel added. */
        ADD,
        /** Channel changed. */
        CHANGE,
        /** Channel property changed. */
        PROPERTY_CHANGE,
        /** Channel removed. */
        REMOVE;
    }

    private static final long serialVersionUID = 811832166118201014L;

    private final ComponentDescription.EndpointNature nature;

    private final ChannelEvent.Type type;

    public ChannelEvent(final Object source, final ComponentDescription.EndpointNature nature, final ChannelEvent.Type type) {
        super(source);
        this.nature = nature;
        this.type = type;
    }

    public ComponentDescription.EndpointNature getNature() {
        return nature;
    }

    public ChannelEvent.Type getType() {
        return type;
    }

    /**
     * Factory method producing a {@link ChannelEvent} of type {@link Type#ADD}.
     * 
     * @param source the event source
     * @param nature the endpoint nature
     * @param channelName the name of the channel
     * @return a {@link ChannelEvent} of type {@link Type#ADD}
     */
    public static ChannelEvent createAddEvent(final Object source, final ComponentDescription.EndpointNature nature,
        final String channelName) {
        final ChannelEvent event = new ChannelAddEvent(source, nature, channelName);
        return event;
    }

    /**
     * Factory method producing a {@link ChannelEvent} of type {@link Type#CHANGE}.
     * 
     * @param source the event source
     * @param nature the endpoint nature
     * @param oldChannelName the old name of the channel
     * @param newChannelName the new name of the channel
     * @param oldChannelType the old type of the channel
     * @param newChannelType the new type of the channel
     * @return a {@link ChannelEvent} of type {@link Type#REMOVE}
     */
    public static ChannelEvent createChangeEvent(final Object source, final ComponentDescription.EndpointNature nature,
        final String oldChannelName, final String newChannelName, final String oldChannelType, final String newChannelType) {
        final ChannelEvent event =
            new ChannelChangeEvent(source, nature, oldChannelName, newChannelName, oldChannelType, newChannelType);
        return event;
    }

    /**
     * Factory method producing a {@link ChannelEvent} of type {@link Type#CHANGE}.
     * 
     * @param source the event source
     * @param nature the channel nature
     * @param channelName the name of the channel
     * @param propertyName the name of the property
     * @param oldPropertyValue the old value of the property
     * @param newPropertyValue the new value of the property
     * @return a {@link ChannelEvent} of type {@link Type#REMOVE}
     */
    public static ChannelEvent createPropertyChangeEvent(final Object source, final ComponentDescription.EndpointNature nature,
        final String channelName, final String propertyName, final Serializable oldPropertyValue, final Serializable newPropertyValue) {
        final ChannelEvent event =
            new ChannelPropertyChangeEvent(source, nature, channelName, propertyName, oldPropertyValue, newPropertyValue);
        return event;
    }

    /**
     * Factory method producing a {@link ChannelEvent} of type {@link Type#REMOVE}.
     * 
     * @param source the event source
     * @param nature the endpoint nature
     * @param channelName the name of the channel
     * @return a {@link ChannelEvent} of type {@link Type#REMOVE}
     */
    public static ChannelEvent createRemoveEvent(final Object source, final ComponentDescription.EndpointNature nature,
        final String channelName) {
        final ChannelEvent event = new ChannelRemoveEvent(source, nature, channelName);
        return event;
    }

    /**
     * {@link ChannelEvent} of type {@link Type#ADD}.
     * 
     * @author Christian Weiss
     */
    public static class ChannelAddEvent extends ChannelEvent {

        private static final long serialVersionUID = -4739017635425453748L;

        private final String channelName;

        /**
         * Constructor.
         * 
         * @param source the source object
         * @param nature the direction of the channel
         * @param type the type of the channel
         * @param channelName the name of the channel
         */
        public ChannelAddEvent(Object source, EndpointNature nature, String channelName) {
            super(source, nature, Type.ADD);
            this.channelName = channelName;
        }

        public String getChannelName() {
            return channelName;
        }

    }

    /**
     * {@link ChannelEvent} of type {@link Type#REMOVE}.
     * 
     * @author Christian Weiss
     */
    public static class ChannelRemoveEvent extends ChannelEvent {

        private static final long serialVersionUID = -6738532072277505854L;

        private final String channelName;

        /**
         * Constructor.
         * 
         * @param source the source object
         * @param nature the direction of the channel
         * @param type the type of the channel
         * @param channelName the name of the channel
         */
        public ChannelRemoveEvent(Object source, EndpointNature nature, String channelName) {
            super(source, nature, Type.REMOVE);
            this.channelName = channelName;
        }

        public String getChannelName() {
            return channelName;
        }

    }

    /**
     * {@link ChannelEvent} of type {@link Type#CHANGE}.
     * 
     * @author Christian Weiss
     */
    public static class ChannelChangeEvent extends ChannelEvent {

        private static final long serialVersionUID = -2599722776484167360L;

        private final String oldChannelName;

        private final String newChannelName;

        private final String oldChannelType;

        private final String newChannelType;

        /**
         * Constructor.
         * 
         * @param source the source object
         * @param nature the direction of the channel
         * @param type the type of the channel
         * @param oldChannelName the old name of the channel
         * @param newChannelName the new name of the channel
         * @param oldChannelType the old type of the channel
         * @param newChannelType the new type of the channel
         */
        public ChannelChangeEvent(Object source, EndpointNature nature, String oldChannelName, final String newChannelName,
            final String oldChannelType, final String newChannelType) {
            super(source, nature, Type.CHANGE);
            this.oldChannelName = oldChannelName;
            this.newChannelName = newChannelName;
            this.oldChannelType = oldChannelType;
            this.newChannelType = newChannelType;
        }

        public String getOldChannelName() {
            return oldChannelName;
        }

        public String getNewChannelName() {
            return newChannelName;
        }

        public String getOldChannelType() {
            return oldChannelType;
        }

        public String getNewChannelType() {
            return newChannelType;
        }

    }

    /**
     * {@link ChannelEvent} of type {@link Type#PROPERTY_CHANGE}.
     * 
     * @author Christian Weiss
     */
    public static class ChannelPropertyChangeEvent extends ChannelEvent {

        private static final long serialVersionUID = 2588929667017738222L;

        private final String channelName;

        private final String propertyName;

        private final Serializable oldPropertyValue;

        private final Serializable newPropertyValue;

        /**
         * Constructor.
         * 
         * @param source the source object
         * @param nature the direction of the channel
         * @param type the type of the channel
         * @param channelName the name of the channel
         */
        public ChannelPropertyChangeEvent(Object source, EndpointNature nature, String channelName, final String propertyName,
            final Serializable oldPropertyValue, final Serializable newPropertyValue) {
            super(source, nature, Type.PROPERTY_CHANGE);
            this.channelName = channelName;
            this.propertyName = propertyName;
            this.oldPropertyValue = oldPropertyValue;
            this.newPropertyValue = newPropertyValue;
        }

        public String getChannelName() {
            return channelName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public Serializable getOldPropertyValue() {
            return oldPropertyValue;
        }

        public Serializable getNewPropertyValue() {
            return newPropertyValue;
        }


    }

}
