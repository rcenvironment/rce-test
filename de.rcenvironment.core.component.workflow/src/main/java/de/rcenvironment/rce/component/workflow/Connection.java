/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.Serializable;

/**
 * Connection class for connecting {@link ComponentDescription}s within a
 * {@link WorkflowDescription}.
 * 
 * @author Roland Gude
 * @author Heinrich Wendel
 */
public class Connection implements Serializable {

    private static final long serialVersionUID = 6019856436149503867L;
    
    /** The source {@link WorkflowNode}. */
    private final WorkflowNode source;
    
    /** The id of the output of the source {@link WorkflowNode}. */
    private final String output;
    
    /** The target {@link WorkflowNode}. */
    private final WorkflowNode target;
    
    /**  The id of the input of the target  {@link WorkflowNode}. */
    private final String input;

    /**
     * Constructor.
     * 
     * @param source See above.
     * @param output See above.
     * @param target See above.
     * @param input See above.
     */
    public Connection(WorkflowNode source, String output, WorkflowNode target, String input) {
        this.source = source;
        this.output = output;
        this.target = target;
        this.input = input;
    }
    
    public WorkflowNode getSource() {
        return source;
    }

    public String getOutput() {
        return output;
    }

    public WorkflowNode getTarget() {
        return target;
    }

    public String getInput() {
        return input;
    }
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof Connection) {
            Connection c = (Connection) o;
            if (c.getTarget().getIdentifier().equals(target.getIdentifier())
                && c.getSource().getIdentifier().equals(source.getIdentifier())
                && input.equals(c.getInput())
                && output.equals(c.getOutput())) {
                equals = true;
            }
        }
        return equals;
    }
    
    @Override
    public int hashCode() {
        StringBuilder builder = new StringBuilder();
        builder.append(target.getIdentifier());
        builder.append(source.getIdentifier());
        builder.append(input);
        builder.append(output);
        return builder.toString().hashCode();
    }
}
