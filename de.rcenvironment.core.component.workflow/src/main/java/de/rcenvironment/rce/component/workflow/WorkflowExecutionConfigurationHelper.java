/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import de.rcenvironment.rce.authentication.AuthenticationException;
import de.rcenvironment.rce.authentication.Session;
import de.rcenvironment.rce.communication.PlatformIdentifier;
import de.rcenvironment.rce.communication.SimpleCommunicationService;
import de.rcenvironment.rce.component.ComponentDescription;
import de.rcenvironment.rce.component.ComponentUtils;
import de.rcenvironment.rce.component.SimpleComponentRegistry;


/**
 * Helper class providing utility functions aiding in the configuration of a workflow description
 * and execution.
 * 
 * @author Heinrich Wendel
 * @author Christian Weiss
 */
public final class WorkflowExecutionConfigurationHelper {

    /**
     * Compares PlatformIdentifier instances by their name.
     *
     * @author Christian Weiss
     */
    private static final class PlatformIdentifierNameComparator implements Comparator<PlatformIdentifier> {

        private static final int LT = -1;

        private static final int GT = 1;

        /**
         * {@inheritDoc}
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(PlatformIdentifier o1, PlatformIdentifier o2) {
            int result;
            if (o1 == null && o2 == null) {
                result = 0;
            } else if (o1 == null) {
                result = LT;
            } else if (o2 == null) {
                result = GT;
            } else {
                result = o1.toString().compareTo(o2.toString());
            }
            return result;
        }
    }

    /** Comparator to sort PlatformIdentifier instances by their name. */
    private static final PlatformIdentifierNameComparator PLATFORM_IDENTIFIER_COMPARATOR = new PlatformIdentifierNameComparator();

    private final SimpleComponentRegistry scr;

    private final SimpleCommunicationService scs;

    private final SimpleWorkflowRegistry workflowRegistry;

    private PlatformIdentifier localPlatform;

    private final List<ComponentDescription> descriptions;

    public WorkflowExecutionConfigurationHelper(SimpleComponentRegistry scr, SimpleCommunicationService scs,
            final SimpleWorkflowRegistry simpleWorkflowRegistry) {
        this.scr = scr;
        this.scs = scs;
        this.workflowRegistry = simpleWorkflowRegistry;
        this.descriptions = scr.getAllComponentDescriptions(false);
    }

    /**
     * Loads the workflow from the given file.
     * 
     * @param file the file.
     * @return WorfklowDescription, null on error.
     */
    public WorkflowDescription loadWorkflow(IFile file) {
        try {
            return loadWorkflow(file.getFullPath().toString());
        } catch (AuthenticationException e) {
            return null;
        }

    }

    /**
     * Loads the workflow located at the given filename.
     * 
     * @param filename Filename.
     * @return WorfklowDescription, null on error.
     * @throws AuthenticationException : no User
     */
    public WorkflowDescription loadWorkflow(String filename) throws AuthenticationException {

        WorkflowDescription wd = null;
        IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(filename);
        if (res instanceof IFile) {
            WorkflowDescriptionPersistenceHandler wdHandler = new WorkflowDescriptionPersistenceHandler();
            try {
                wd = wdHandler.readWorkflowDescriptionFromStream(((IFile) res).getContents(), Session.getInstance().getUser());
            } catch (IOException e) {
                wd = null;
            } catch (CoreException e) {
                wd = null;
            } catch (ParseException e) {
                wd = null;
            }
        }
        return wd;
    }
    /**
     * Loads the workflow located at the given filename.
     * 
     * @param filename Filename.
     * @return WorfklowDescription, null on error.
     * @throws AuthenticationException : no User
     */
    public WorkflowDescription loadWorkflow(File filename) throws AuthenticationException {

        WorkflowDescription wd = null;

        WorkflowDescriptionPersistenceHandler wdHandler = new WorkflowDescriptionPersistenceHandler();
        try {
            InputStream is = filename.toURI().toURL().openStream();
            wd = wdHandler.readWorkflowDescriptionFromStream(is, Session.getInstance().getUser());
        } catch (IOException e) {
            wd = null;
        } catch (ParseException e) {
            wd = null;
        }

        return wd;
    }
    /**
     * Returns if the current description is valid so that it can be run.
     * 
     * @param wd WorkflowDescription to run.
     * @return True or false.
     */
    public boolean isValid(WorkflowDescription wd) {

        // FIXME: error messages

        boolean valid = true;

        // workflow valid
        if (wd != null) {

            // controller platform valid
            PlatformIdentifier pi = wd.getTargetPlatform();
            if (pi != null && !scs.getAvailableNodes().contains(pi)) {
                valid = false;
            }

            // component platforms valid
            for (WorkflowNode node : wd.getWorkflowNodes()) {
                if (node.getComponentDescription().getPlatform() == null) {
                    pi = localPlatform;
                } else {
                    pi = node.getComponentDescription().getPlatform();
                }
                if (!ComponentUtils.hasComponent(descriptions, node.getComponentDescription().getIdentifier(), pi)) {

                    valid = false;
                }
            }
        } else {
            valid = false;
        }

        return valid;
    }

    public Set<PlatformIdentifier> getTargetPlatforms() {
        return scs.getAvailableNodes();
    }
    /**
     * 
     * @return platdorms
     */
    public List<PlatformIdentifier> getTargetPlatformsSortedByName() {
        List<PlatformIdentifier> result = new ArrayList<PlatformIdentifier>(getTargetPlatforms());
        Collections.sort(result, PLATFORM_IDENTIFIER_COMPARATOR);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the local platform.
     * 
     * @return the local platform
     */
    public PlatformIdentifier getLocalPlatform() {
        if (localPlatform == null) {
            for (PlatformIdentifier platform : scs.getAvailableNodes()) {
                if (scs.isLocalPlatform(platform)) {
                    localPlatform = platform;
                }
            }
        }
        return localPlatform;
    }

    public List<ComponentDescription> getComponents() {
        return descriptions;
    }

    /**
     * Returns the used SimpleComponentRegistry instance.
     * 
     * @return the used SimpleComponentRegistry instance
     */
    public SimpleComponentRegistry getSimpleComponentRegistry() {
        return scr;
    }

    /**
     * Returns the used SimpleWorkflowRegistry instance.
     * 
     * @return the used SimpleWorkflowRegistry instance
     */
    public SimpleWorkflowRegistry getSimpleWorkflowRegistry() {
        return workflowRegistry;
    }

    /**
     * Returns a list of platforms the component is installed on.
     * 
     * @param componentDescription Description of the component.
     * @return List of platform the component is installed on.
     */
    public List<PlatformIdentifier> getTargetPlatformsForComponent(ComponentDescription componentDescription) {
        return ComponentUtils.getPlatformsForComponent(descriptions, componentDescription.getIdentifier());
    }

    /**
     * Returns a list of platforms the component is installed on, sorted by their name.
     * 
     * @param componentDescription Description of the component.
     * @return List of platform the component is installed on.
     */
    public List<PlatformIdentifier> getTargetPlatformsForComponentSortedByName(ComponentDescription componentDescription) {
        final List<PlatformIdentifier> result = new ArrayList<PlatformIdentifier>(
                ComponentUtils.getPlatformsForComponent(descriptions, componentDescription.getIdentifier()));
        Collections.sort(result, PLATFORM_IDENTIFIER_COMPARATOR);
        return Collections.unmodifiableList(result);
    }

}
