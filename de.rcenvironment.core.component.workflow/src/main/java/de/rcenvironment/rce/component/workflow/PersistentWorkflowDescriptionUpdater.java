/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.workflow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import de.rcenvironment.rce.authentication.User;

/**
 * Updates persistent workflow descriptions.
 *
 * @author Sophia Schlegel
 * @author Doreen Seider
 */
public final class PersistentWorkflowDescriptionUpdater {
    
    private PersistentWorkflowDescriptionUpdater() {}
    
    /**
     * Updates persistent workflow description to workflow persistence format of current workflow version.
     * 
     * @param inputStream stream with workflow description to update
     * @param fromVersion indicates workflow version to update from
     * @param user calling user
     * @return stream with update workflow description
     * @throws IOException if update failed for some IO reason.
     */
    public static InputStream updatePersistentWorkflowDescription(InputStream inputStream, int fromVersion, User user) throws IOException {
        
        switch (fromVersion) {
        // sample for following versions (case terms without any 'break;')
        case 0:
            inputStream = updatePersistentWorkflowDescriptionFromVersion0To1(inputStream, user);
//        case 1:
//            inputStream = updatePersistentWorkflowDescriptionFromVersion1To2(inputStream);
        default:
            break;
        }
        
        return inputStream;
    }
    
    /**
     * Checks if given persistent workflow is incompatible with current version.
     * 
     * @param inputStream stream with workflow description to update
     * @param fromVersion indicates workflow version to update from
     * @param user calling user
     * @return <code>true</code> if incompatible, else <code>false</code>
     * @throws IOException if update failed for some IO reason.
     */
    public static boolean isUpdateNeeded(InputStream inputStream, int fromVersion, User user) throws IOException {
        
        switch (fromVersion) {
        // sample for following versions (case terms without any 'break;')
        case 0:
            if (isUpdateNeededFromVersion0To1(inputStream, user)) {
                return true;
            }
//        case 1:
//            if (isUpdateNeededFromVersion1To2(inputStream, user)) {
//                return true;
//            }
        default:
            break;
        }
        
        return false;

    }

    // visibility is protected to allow single test of this method
    protected static InputStream updatePersistentWorkflowDescriptionFromVersion0To1(InputStream inputStream, User user) throws IOException {
                
        WorkflowDescription wd;
        try {
            wd = new WorkflowDescriptionPersistenceHandler().readWorkflowDescriptionFromStream(inputStream, user);
        } catch (ParseException e) {
            throw new IOException("parsing workflow file failed", e);
        }
        
        for (WorkflowNode node : wd.getWorkflowNodes()) {
            if (node.getComponentDescription().getIdentifier().equals("de.rcenvironment.rce.components.python.PythonComponent_Python")) {
                node.setProperty("pythonInstallation", "${pathPlaceholder}");
            }
        }
        
        wd.setWorkflowVersion(1);
        
        return new ByteArrayInputStream(new WorkflowDescriptionPersistenceHandler().writeWorkflowDescriptionToStream(wd).toByteArray());
    }
    
    // visibility is protected to allow single test of this method
    protected static boolean isUpdateNeededFromVersion0To1(InputStream inputStream,  User user) throws IOException {
                
        WorkflowDescription wd;
        try {
            wd = new WorkflowDescriptionPersistenceHandler().readWorkflowDescriptionFromStream(inputStream, user);
        } catch (ParseException e) {
            throw new IOException("parsing workflow file failed", e);
        }
        
        for (WorkflowNode node : wd.getWorkflowNodes()) {
            if (node.getComponentDescription().getIdentifier().equals("de.rcenvironment.rce.components.python.PythonComponent_Python")) {
                return true;
            }
        }
                
        return false;
    }
    
}
