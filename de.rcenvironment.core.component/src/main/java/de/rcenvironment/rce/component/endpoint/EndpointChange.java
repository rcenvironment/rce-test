/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.endpoint;

import java.io.Serializable;

import de.rcenvironment.rce.component.ComponentDescription;

/**
 * Event class to propagate changes of dynamic endpoints.
 * 
 * @author Christian Weiss
 */
public final class EndpointChange implements Serializable {

    private static final long serialVersionUID = 9088170162935461299L;

    /**
     * The type of {@link EndpointChange}.
     * 
     * @author Christian Weiss
     */
    public static enum Type {
        /** An endpoint was added. */
        Add,
        /** An endpoint was removed. */
        Remove,
        /** An endpoint was changed. */
        Change;
    }

    /** The {@link Type} of the event. */
    private final EndpointChange.Type type;

    /** The {@link ComponentDescription.EndpointNature} of the affected endpoint. */
    private final ComponentDescription.EndpointNature endpointNature;
    
    /** The {@link ComponentDescription} the affected endpoint belongs to. */
    private final ComponentDescription componentDescription;

    /** The name of the affected endpoint. */
    private final String endpointName;

    /** The type of the affected endpoint. */
    private final String endpointType;

    /** The name of the affected endpoint before the action this event informs about happened. */
    private final String formerEndpointName;

    /** The type of the affected endpoint before the action this event informs about happened. */
    private final String formerEndpointType;

    /**
     * Constructs an event.
     * 
     * @param componentDescription The {@link ComponentDescription} which is associated with the
     *        affected endpoint.
     * @param type The {@link EndpointChange.Type} of the event.
     * @param endpointNature The {@link EndpointChange.EndpointNature} of the affected endpoint.
     * @param newEndpointName The name of the affected endpoint.
     * @param newEndpointType The type of the affected endpoint.
     */
    public EndpointChange(EndpointChange.Type type, ComponentDescription.EndpointNature endpointNature,
        String endpointName, String endpointType, String formerEndpointName, String formerEndpointType,
        ComponentDescription componentDescription) {
        
        this.type = type;
        this.endpointNature = endpointNature;
        this.endpointName = endpointName;
        this.endpointType = endpointType;
        this.formerEndpointName = formerEndpointName;
        this.formerEndpointType = formerEndpointType;
        this.componentDescription = componentDescription;
        
    }

    /**
     * Returns the {@link ComponentDescription} of the component the {@link ComponentDescription} is
     * governing the endpoints for.
     * 
     * @return The {@link ComponentDescription} of the component.
     */
    public ComponentDescription getComponentDescription() {
        return componentDescription;
    }

    /**
     * Returns the {@link EndpointChange.EndpointNature} of the affected endpoint.
     * 
     * @return The {@link EndpointChange.EndpointNature} of the affected endpoint.
     */
    public ComponentDescription.EndpointNature getEndpointNature() {
        return endpointNature;
    }

    /**
     * Returns the {@link EndpointChange.Type} of this event.
     * 
     * @return The {@link EndpointChange.Type} of this event.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the name of the endpoint, which is the identifier of the input/output.
     * 
     * @return The name of the endpoint.
     */
    public String getEndpointName() {
        return endpointName;
    }

    /**
     * Returns the type of the endpoint.
     * 
     * @return The type of the endpoint.
     */
    public String getEndpointType() {
        return endpointType;
    }

    /**
     * Returns the name of the endpoint, which is the identifier of the input/output, before the
     * action this event informs about happened.
     * 
     * @return The name of the endpoint before the action this event informs about happened.
     */
    public String getFormerEndpointName() {
        return formerEndpointName;
    }

    /**
     * Returns the type of the endpoint before the action this event informs about happened.
     * 
     * @return The type of the endpoint before the action this event informs about happened.
     */
    public String getFormerEndpointType() {
        return formerEndpointType;
    }

}
