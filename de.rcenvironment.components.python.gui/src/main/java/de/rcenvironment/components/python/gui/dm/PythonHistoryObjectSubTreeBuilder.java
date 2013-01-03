/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.components.python.gui.dm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import de.rcenvironment.commons.channel.DataManagementFileReference;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNode;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.DMBrowserNodeType;
import de.rcenvironment.rce.gui.datamanagement.browser.spi.HistoryObjectSubtreeBuilder;


/**
 * Build up the tree for the python history.
 *
 * @author Arne Bachmann
 */
public class PythonHistoryObjectSubTreeBuilder implements HistoryObjectSubtreeBuilder {

    @Override
    public String[] getSupportedObjectClassNames() {
        return new String[] { PythonComponentHistoryObject.class.getName(),
            "de.rcenvironment.rce.components.python.PythonComponentHistoryObject" };
    }

    @Override
    public Serializable deserializeHistoryObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
        return (Serializable) ois.readObject();
    }

    @Override
    public void buildInitialHistoryObjectSubtree(final Serializable historyObject, final DMBrowserNode parent) {
        if (historyObject instanceof PythonComponentHistoryObject) {
            buildInitial((PythonComponentHistoryObject) historyObject, parent);
        } else {
            throw new IllegalArgumentException(historyObject.getClass().getCanonicalName());
        }
    }
    
    private void buildInitial(final PythonComponentHistoryObject runningState, final DMBrowserNode parent) {
        //Input root node
        if ((runningState.getInputDmHandles() != null) && (runningState.getInputDmHandles().size() > 0)) {
            final DMBrowserNode inputRootNode = DMBrowserNode.addNewChildNode("Input data management entries",
                DMBrowserNodeType.Folder, parent);        
            for (final DataManagementFileReference dm: runningState.getInputDmHandles()) {
                final DMBrowserNode node = DMBrowserNode.addNewLeafNode(dm.getName(), DMBrowserNodeType.DMFileResource, inputRootNode);
                node.setAssociatedFilename(dm.getName());
                node.setDataReferenceId(dm.getReference());
            }
        }
        
        //Output root node
        if ((runningState.getOutputDmHandles() != null) && (runningState.getOutputDmHandles().size() > 0)) {
            final DMBrowserNode outputRootNode = DMBrowserNode.addNewChildNode("Output data management entries",
                DMBrowserNodeType.Folder, parent);        
            for (final DataManagementFileReference dm: runningState.getOutputDmHandles()) {
                final DMBrowserNode node = DMBrowserNode.addNewLeafNode(dm.toString(), DMBrowserNodeType.DMFileResource, outputRootNode);
                node.setAssociatedFilename(dm.toString());
                node.setDataReferenceId(dm.toString());
            }
        }
        
        // Status
        final DMBrowserNode exitCodeNode;
        if (runningState.getExitCode() == 0) {
            exitCodeNode = DMBrowserNode.addNewLeafNode("Exit code", DMBrowserNodeType.InformationText, parent);
        } else { 
            exitCodeNode = DMBrowserNode.addNewLeafNode("Exit code", DMBrowserNodeType.WarningText, parent);
        }
        exitCodeNode.setTitle("Exit code: " + Integer.toString(runningState.getExitCode()));
        
        // Error code, if any
        if (runningState.getError() != null) { // Exception handling
            final DMBrowserNode errorNode = DMBrowserNode.addNewChildNode("Error" + runningState.getError().getMessage(),
                    DMBrowserNodeType.WarningText, parent);
            errorNode.setTitle("Error:");
            for (final StackTraceElement ste: runningState.getError().getStackTrace()) {
                DMBrowserNode.addNewLeafNode("method '" + ste.getMethodName() + "' in line "
                        + Integer.toString(ste.getLineNumber()) + " in class '" + ste.getClassName() + "' in file " + ste.getFileName(),
                    DMBrowserNodeType.InformationText, errorNode);
            }
        }
    }

}
