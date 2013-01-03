/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.parts;

import de.rcenvironment.rce.component.workflow.WorkflowNode;

/**
 * Class that encapsulates all connections between two nodes into one connection.
 * 
 * @author Heinrich Wendel
 */
public class ConnectionWrapper {

    /** paint a sourceArrow? */
    private boolean sourceArrow;

    /** paint a targetArrow? */
    private boolean targetArrow;

    /** The source node. */
    private WorkflowNode source;

    /** The target node. */
    private WorkflowNode target;

    /**
     * Constructor.
     * @param source See above.
     * @param target See above.
     */
    public ConnectionWrapper(WorkflowNode source, WorkflowNode target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Setter.
     * @param value Setter.
     */
    public void setSourceArrow(boolean value) {
        this.sourceArrow = value;
    }

    /**
     * Getter.
     * @return Getter.
     */
    public boolean getSourceArrow() {
        return sourceArrow;
    }

    /**
     * Setter.
     * @param value Setter.
     */
    public void setTargetArrow(boolean value) {
        this.targetArrow = value;
    }

    /**
     * Getter.
     * @return Getter.
     */
    public boolean getTargetArrow() {
        return targetArrow;
    }

    /**
     * Getter.
     * @return Getter.
     */
    public WorkflowNode getSource() {
        return source;
    }

    /**
     * Getter.
     * @return Getter.
     */
    public WorkflowNode getTarget() {
        return target;
    }
    
}
