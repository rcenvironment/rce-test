/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.testutils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.PlatformIdentifierFactory;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentInstanceDescriptor;
import de.rcenvironment.rce.component.DeclarativeComponentDescription;


/**
 * Factory class for default {@link ComponentDescription}s for test purposes.
 *
 * @author Doreen Seider
 */
public final class MockComponentStuffFactory {

    /** Mock component's class name. */
    public static final String COMPONENT_CLASS = "class";
    
    /** Mock component's name. */
    public static final String COMPONENT_NAME = "name";

    /** Mock component's identifier. */
    public static final String COMPONENT_IDENTIFIER = "class_" + COMPONENT_NAME;
    
    /** Mock workflow node's platform. */
    public static final PlatformIdentifier COMPONENT_PLATFORM = PlatformIdentifierFactory.fromHostAndNumberString("Erichs Lampenladen:89");

    /** Mock component instance's name. */
    public static final String COMPONENT_INSTANCE_IDENTIFIER = "Lange Lulatsch";
    
    /** Mock workflow's name. */
    public static final String WORKFLOW_NAME = "hansestadt";
    
    private static final String COMPONENT_GROUP = "group";

    private static final String COMPONENT_VERSION = "version";

    private static final Map<String, Class<? extends Serializable>> COMPONENT_INPUTS_DEF
        = new HashMap<String, Class<? extends Serializable>>();

    private static final Map<String, Class<? extends Serializable>> COMPONENT_OUTPUTS_DEF
        = new HashMap<String, Class<? extends Serializable>>();

    private static final Map<String, Class<? extends Serializable>> COMPONENT_CONFIG_DEFS
        = new HashMap<String, Class<? extends Serializable>>();

    private static final Map<String, Serializable> COMPONENT_DEFAULT_CONFIG = new HashMap<String, Serializable>();

    private static final byte[] COMPONENT_ICON16 = new byte[10];

    private static final byte[] COMPONENT_ICON32 = new byte[0];

    private static final String COMPONENT_INSTANCE_NAME = "Schwangere Auster";

    private static final String COMPONENT_INSTANCE_WORKDIR = "Goldelse";
    
    private static final Set<PlatformIdentifier> INVOLVED_PLATFORMS = new HashSet<PlatformIdentifier>();
    
    static {
        INVOLVED_PLATFORMS.add(COMPONENT_PLATFORM);
        INVOLVED_PLATFORMS.add(PlatformIdentifierFactory.fromHostAndNumber("jubidu", 5));
    }
    
    private MockComponentStuffFactory() {}
    
    /**
     * @return mock {@link DeclarativeComponentDescription}
     */
    public static DeclarativeComponentDescription createDeclarativeComponentDescription() {
        return new DeclarativeComponentDescription(COMPONENT_IDENTIFIER, COMPONENT_NAME, COMPONENT_GROUP,
            COMPONENT_VERSION, COMPONENT_INPUTS_DEF, COMPONENT_OUTPUTS_DEF, null, null, COMPONENT_CONFIG_DEFS,
            null, COMPONENT_DEFAULT_CONFIG, COMPONENT_ICON32, COMPONENT_ICON16);
    }
    
    /**
     * @return mock {@link ComponentDescription}
     */
    public static ComponentDescription createComponentDescription() {
        return new ComponentDescription(createDeclarativeComponentDescription());
    }
    
    /**
     * @return mock {@link ComponentInstanceDescriptor}.
     */
    public static ComponentInstanceDescriptor createComponentInstanceDescriptor() {
        return new ComponentInstanceDescriptor(COMPONENT_INSTANCE_IDENTIFIER, COMPONENT_INSTANCE_NAME, COMPONENT_PLATFORM,
            COMPONENT_INSTANCE_WORKDIR, COMPONENT_IDENTIFIER, WORKFLOW_NAME, INVOLVED_PLATFORMS);
    }
}
