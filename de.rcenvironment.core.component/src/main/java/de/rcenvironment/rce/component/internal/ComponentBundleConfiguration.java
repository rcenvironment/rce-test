/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the component {@link Bundle}.
 * 
 * @author Doreen Seider
 */
public class ComponentBundleConfiguration {

    private boolean publishAllComponents = false;

    private List<String> publishedComponents = new ArrayList<String>();

    public boolean getAllComponentsRemotelyAccessible() {
        return publishAllComponents;
    }

    public void setPublishAllComponents(boolean publishAllComponents) {
        this.publishAllComponents = publishAllComponents;
    }

    public List<String> getPublishedComponents() {
        return publishedComponents;
    }

    public void setPublishedComponents(List<String> components) {
        this.publishedComponents = components;
    }
}
