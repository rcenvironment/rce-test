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

import de.rcenvironment.commons.TypedProperties;
import de.rcenvironment.rce.component.endpoint.Input;


/**
 * Default strategy.
 * Triggers if any of the "OR" parts has been set.
 * Dynamic channels trigger if they are all non-empty.
 * 
 * @version $LastChangedRevision: 0$
 * @author Arne Bachmann
 */
public class DefaultScheduler {
    
    /**
     * Our logger instance.
     */
    private static final Log LOGGER = LogFactory.getLog(DefaultScheduler.class);
    
    /**
     * These names always trigger a run "OR" logic.
     */
    private Collection<String> mustRun;
    
    /**
     * These names only trigger a run, if all of them are not empty "AND" logic.
     */
    private Collection<String> dynamic;
    
    /**
     * Collected last values for the "always trigger" channels.
     */
    private TypedProperties</* key */ String> lastValues = new TypedProperties<String>();
    
    /**
     * Queues of all static input channels.
     */
    private Map<String, Queue<Input>> staticInputs = new Hashtable<String, Queue<Input>>();
    
    /**
     * Queues of all dynamic input channels.
     */
    private Map<String, Queue<Input>> dynamicInputs = new Hashtable<String, Queue<Input>>();
    
    /**
     * If true, run also in the initialRun phase.
     */
    private boolean isSource = false;
    
    
    /**
     * Constructor of the strategy, initializing a non-source component.
     * 
     * @param mustRunChannels The names of channels that trigger a run regardless of other circumstances
     * @param dynamicChannels The names of channels that trigger a run if they are all set
     */
    public DefaultScheduler(final Map<String, Class<? extends Serializable>> mustRunChannels, final Collection<String> dynamicChannels) {
        this(mustRunChannels, dynamicChannels, false);
    }
    
    /**
     * Constructor of the strategy.
     * 
     * @param alwaysRunChannels The names of channels that trigger a run regardless of other circumstances
     * @param dynamicChannels The names of channels that trigger a run if they are all set
     * @param runAsSource If true, allow running in initialrun phase instead of only follow-up runs
     */
    public DefaultScheduler(final Map<String, Class<? extends Serializable>> alwaysRunChannels, final Collection<String> dynamicChannels,
            final boolean runAsSource) {
        // static channels
        isSource = runAsSource;
        if (alwaysRunChannels == null) {
            mustRun = new HashSet<String>();
        } else {
            mustRun = alwaysRunChannels.keySet();
            for (final Entry<String, Class<? extends Serializable>> entry: alwaysRunChannels.entrySet()) {
                lastValues.setType(entry.getKey(), entry.getValue());
            }
        }
        for (final String name: mustRun) {
            staticInputs.put(name, new LinkedList<Input>());
        }
        
        // dynamic channels
        if (dynamicChannels == null) {
            dynamic = new HashSet<String>();
        } else {
            dynamic = new HashSet<String>(dynamicChannels);
        }
        for (final String name: new HashSet<String>(dynamic)) { // must use a copy here
            if (!mustRun.contains(name)) { // just in case the user treis to add all static inputs instead of only dynamic ones
                dynamicInputs.put(name, new LinkedList<Input>());
            } else {
                dynamic.remove(name);
            }
        }
    }

    /**
     * Drop-in replacement of the check method. Checks the incoming value.
     * @param newInputValue The incoming channel value
     * 
     * @return True if we need to run
     */
    public boolean check(final Input newInputValue) {
        if (mustRun.contains(newInputValue.getName())) { // it's one of the "OR" channels: run!
            staticInputs.get(newInputValue.getName()).add(newInputValue);
            return true;
        }
        if (dynamic.contains(newInputValue.getName())) { // add new value to its queue
            dynamicInputs.get(newInputValue.getName()).add(newInputValue);
        } else {
            LOGGER.error("Incoming channel value has unknown name");
        }
        return areAllDynamicValuesAvailable() && areAnyMustRunValuesAvailable();
    }
    
    /**
     * First line of your run method should check this method; it determines behaviour in source vs. non-source components.
     * Determines if we need to run, regardless of run.
     * @param isInInitialRun flag returned from components run method
     * @return True if we need to run
     */
    public boolean mustRun(final boolean isInInitialRun) {
        if (isInInitialRun) { // initial run only fires if we are  a source
            return isSource;
        }
        return areAllDynamicValuesAvailable() && areAnyMustRunValuesAvailable();
    }
    
    /**
     * Check consecutive run (working off the accumulated dynamic channel queues).
     * @return True if we must run again
     */
    public boolean mustRun() {
        return areAnyFurtherMustRunValuesAvailable() || (dynamic.size() > 0) && areAllDynamicValuesAvailable();
    }
    
    /**
     * Check if we can run based on the availability of at least one value per "AND" channel.
     * @return true if run is necessary
     */
    public boolean areAllDynamicValuesAvailable() {
        for (final Queue<Input> input: dynamicInputs.values()) {
            if (input.size() < 1) { // one value is missing: cannot run
                return false;
            }
        }
        return true; // all channels have at least one value, or there are no dynamic channels at all
    }
    
    /**
     * Check if we can run based on the availability of at least one value per "OR" channel.
     * @return True if at least one value has been set
     */
    protected boolean areAnyMustRunValuesAvailable() {
        if (staticInputs.size() == 0) {
            return true;
        }
        for (final Entry<String, Queue<Input>> input: staticInputs.entrySet()) {
            if ((input.getValue().size() >= 1)
                    || (lastValues.get(input.getKey(), Serializable.class) != null)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if we can run based on the availability of at least one value per "OR" channel.
     * @return True if at least one value has been set
     */
    protected boolean areAnyFurtherMustRunValuesAvailable() {
        if (staticInputs.size() == 0) {
            return false;
        }
        for (final Entry<String, Queue<Input>> input: staticInputs.entrySet()) {
            if (input.getValue().size() >= 1) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Consume the last set value from the given static channel, or null if none.
     * Stores the consumed value under "lastValues".
     * 
     * @param <Type> For type-safety in the code
     * @param name The channel name
     * @param type The type expected
     * @return The value
     */
    public <Type extends Serializable> Type consumeValue(final String name, final Class<Type> type) {
        if (!staticInputs.containsKey(name)) {
            return null;
        }
        if (staticInputs.get(name).size() == 0) { // get last value instead
            @SuppressWarnings("unchecked") final Type value = (Type) lastValues.get(name, Serializable.class);
            return value;
        }
        @SuppressWarnings("unchecked") final Type value = (Type) staticInputs.get(name).remove().getValue();
        lastValues.put(name, value);
        return value;
    }
    
    /**
     * Get the last value of the given channel, or null if none.
     * 
     * @param <T> For type-safety in the code
     * @param name The channel name
     * @param type The type expected
     * @return The value
     */
    public <T extends Serializable> T getLastValue(final String name, final Class<T> type) {
        return lastValues.get(name, type); // or null
    }
    
    /**
     * TODO Remove this later, it's just a workaround for use in var mapper (which need a refactoring, too).
     * @return The inputs
     */
    public Map<String, Queue<Input>> getDynamicInputs() {
        return dynamicInputs;
    }

}
