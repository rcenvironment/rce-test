/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.gui.workflow.editor.validator;

import java.io.Serializable;

import de.rcenvironment.commons.StringUtils;


/**
 * Messages to inform about validation results of a {@link WorkflowNodeValidator}.
 * 
 * @author Christian Weiss
 */
public class WorkflowNodeValidationMessage implements Serializable {

    /**
     * The type of {@link WorkflowNodeValidationMessage}.
     * 
     * @author Christian Weiss
     */
    public enum Type {
        /** Warning message type. */
        WARNING,
        /** ERROR message type. */
        ERROR;
    }

    private static final long serialVersionUID = 3558625170986798920L;

    private final Type type;

    private final String property;

    private final String relativeMessage;

    private final String absoluteMessage;

    @Deprecated
    public WorkflowNodeValidationMessage(final String property, final String relativeMessage, final String absoluteMessage) {
        this(Type.ERROR, property, relativeMessage, absoluteMessage);
    }

    public WorkflowNodeValidationMessage(Type type, final String property, final String relativeMessage, final String absoluteMessage) {
        this.type = type;
        this.property = property;
        this.relativeMessage = relativeMessage;
        this.absoluteMessage = absoluteMessage;
    }

    public Type getType() {
        return type;
    }

    public String getProperty() {
        return property;
    }

    public String getRelativeMessage() {
        return relativeMessage;
    }

    public String getAbsoluteMessage() {
        return absoluteMessage;
    }

    @Override
    public boolean equals(final Object obj) {
        boolean result = super.equals(obj);
        if (obj instanceof WorkflowNodeValidationMessage) {
            final WorkflowNodeValidationMessage other = (WorkflowNodeValidationMessage) obj;
            final String thisString = "" + property + relativeMessage + absoluteMessage;
            final String otherString = "" + other.property + other.relativeMessage + other.absoluteMessage;
            result = thisString.equals(otherString);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return StringUtils.notNull(property + relativeMessage + absoluteMessage).hashCode();
    }

    @Override
    public String toString() {
        if (property != null && !property.isEmpty()
                && relativeMessage != null && !relativeMessage.isEmpty()) {
            return String.format("%s: %s", property, relativeMessage);
        } else {
            return absoluteMessage;
        }
    }

}
