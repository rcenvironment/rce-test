/*
 * Copyright (C) 2011-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.scheduler;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.rce.component.endpoint.Input;


/**
 * A strategy that requires for all channels a new input value to run. This is plain "AND" logic.
 * 
 * @version $LastChangedRevision: 0$
 * @author Arne Bachmann
 */
public class AggregatingScheduler {
    
    /**
     * Our logger instance.
     */
    private static final Log LOGGER = LogFactory.getLog(AggregatingScheduler.class);
    
    /**
     * These names always trigger a run "OR" logic.
     */
    private Collection<String> names;
    
    /**
     * Queues of all static input channels.
     */
    private Map<String, Queue<Input>> inputs = new Hashtable<String, Queue<Input>>();
    
    
    /**
     * Constructor of the strategy.
     * 
     * @param allChannels The names of channels
     */
    public AggregatingScheduler(final Map<String, Class<? extends Serializable>> allChannels) {
        if (allChannels == null) {
            names = new HashSet<String>();
        } else {
            names = allChannels.keySet();
        }
        for (final String name: names) {
            inputs.put(name, new LinkedList<Input>());
        }
    }

    /**
     * Check the incoming value.
     * @param newInputValue The incoming channel value
     * 
     * @return True if we need to run
     */
    public boolean check(final Input newInputValue) {
        if (names.contains(newInputValue.getName())) {
            inputs.get(newInputValue.getName()).add(newInputValue); // store value
            return areAllValuesAvailable();
        } else {
            LOGGER.error("Incoming channel value has unknown name");
            return false;
        }
    }
    
    /**
     * Determines inside the run method, if we need a value-less run.
     * @param initialRun If this is the initial call
     * @return True if we aren't initial (this isn't a source component)
     */
    public boolean mustRun(final boolean initialRun) {
        return !initialRun && areAllValuesAvailable();
    }
    
    /**
     * Consecutive runs.
     * 
     * @return True if we need to run again
     */
    public boolean mustRun() {
        return areAllValuesAvailable();
    }
    
    /**
     * Check if we can run based on the availability of at least one value per "AND" channel.
     * @return true if run is necessary
     */
    public boolean areAllValuesAvailable() {
        for (final Queue<Input> input: inputs.values()) {
            if (input.size() < 1) { // one value is missing: cannot run
                return false;
            }
        }
        return true; // all channels have at least one value, or there are no dynamic channels at all
    }
    
    /**
     * Get the last set of values for all channels.
     * 
     * @return The values map
     */
    public Map<String, Serializable> consumeValues() {
        final Map<String, Serializable> values = new Hashtable<String, Serializable>();
        for (final Entry<String, Queue<Input>> entry: inputs.entrySet()) {
            if (entry.getValue().size() == 0) {
                values.put(entry.getKey(), null);
                continue;
            }
            values.put(entry.getKey(), entry.getValue().poll().getValue());
        }
        return values;
    }
    
}
