/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.commons.configuration;

import de.rcenvironment.commons.configuration.annotation.Configurable.LabelProvider;
import de.rcenvironment.commons.configuration.annotation.Configurable.NoLabelProvider;
import de.rcenvironment.commons.configuration.annotation.Configurable.NoValueProvider;
import de.rcenvironment.commons.configuration.annotation.Configurable.ValueProvider;

/**
 * Class holding constants uses within the {@link Configurable} annotation. If it is declared there
 * the javac generates an error "annotation is missing <clinit>"
 * @author Doreen Seider
 */
public final class ConfigurationConstants {

    /** The {@link ChoiceProvider} representing a not set value. */
    public static final Class<? extends LabelProvider> NO_LABEL_PROVIDER = NoLabelProvider.class;

    /** The {@link ChoiceProvider} representing a not set value. */
    public static final Class<? extends ValueProvider> NO_VALUE_PROVIDER = NoValueProvider.class;
    
    /** Private constructor of this utility class. */
    private ConfigurationConstants() {}
}
