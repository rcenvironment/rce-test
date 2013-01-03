/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component;

import java.io.Serializable;
import java.util.Set;

import de.rcenvironment.rce.communication.PlatformIdentifier;


/**
 * Container holding information about a component instance.
 *
 * @author Doreen Seider
 */
public class ComponentInstanceDescriptor implements Serializable {

    private static final long serialVersionUID = -1439105062604601152L;

    private String identifier;
    
    private String name;
    
    private PlatformIdentifier platform;
    
    private String workDir;

    private String compIdentifier;
    
    private String componentContextName;
    
    private Set<PlatformIdentifier> involvedPlatforms;
    
    public ComponentInstanceDescriptor(String newIdentifier, String newName, PlatformIdentifier newPlatform, String newWorkDir,
        String newCompIdentifier, String aCompContextName, Set<PlatformIdentifier> theInvolvedPlatforms) {
        
        identifier = newIdentifier;
        name = newName;
        platform = newPlatform;
        workDir = newWorkDir;
        compIdentifier = newCompIdentifier;
        componentContextName = aCompContextName;
        involvedPlatforms = theInvolvedPlatforms;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    public String getName() {
        return name;
    }
    
    public PlatformIdentifier getPlatform() {
        return platform;
    }
    
    public String getWorkingDirectory() {
        return workDir;
    }
    
    public String getComponentIdentifier() {
        return compIdentifier;
    }
    
    public String getComponentContextName() {
        return componentContextName;
    }
    
    public Set<PlatformIdentifier> getPlatformsInvolvedInComponentContext() {
        return involvedPlatforms;
    }

}
