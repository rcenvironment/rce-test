/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.editor.connections;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Display;

import de.rcenvironment.rce.component.workflow.Connection;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider;
import de.rcenvironment.rce.gui.workflow.EndpointHandlingHelper;
import de.rcenvironment.rce.gui.workflow.EndpointLabelProvider;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider.Endpoint;
import de.rcenvironment.rce.gui.workflow.EndpointContentProvider.EndpointGroup;

/**
 * Controller class for the connection dialog.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class ConnectionDialogController {

    private static final String SLASH = "/"; //$NON-NLS-1$

    private static final String SEMICOLON = ";"; //$NON-NLS-1$

    /** The actual dialog. */
    private ConnectionDialog dialog;

    /** The complete WorkflowDescription. */
    private WorkflowDescription description;

    /** The selected source node (maybe null). */
    private WorkflowNode source;

    /** The selected target node (maybe null). */
    private WorkflowNode target;

    /** Input or output item? .*/
    public enum Type {
        /**
         * Endpoint representing an input.
         */
        INPUT,
        /**
         * Endpoint representing an output.
         */
        OUTPUT
    }
    /**
     * Constructor.
     * 
     * @param description The complete WorkflowDescription.
     * @param source The selected source node (maybe null).
     * @param target The selected target node (maybe null).
     */
    public ConnectionDialogController(WorkflowDescription description, WorkflowNode source, WorkflowNode target) {
        this.description = description;
        this.source = source;
        this.target = target;

        dialog = new ConnectionDialog(Display.getCurrent().getActiveShell());
        if (source != null){
            dialog.setInitialSourceFilterText(source.getName());
        }
        if (target != null){
            dialog.setInitialTargetFilterText(target.getName());
        }
        dialog.create();
        dialog.getShell().setText(Messages.connectionEditor);
        initialize();
    }

    /**
     * Shows the dialog.
     * @return The return code (which button pressed).
     */
    public int open() { 

        return dialog.open();   
    }

    /**
     * Initializes the GUI.
     */
    private void initialize() {
        dialog.getSourceTreeViewer().setLabelProvider(new EndpointLabelProvider(Type.OUTPUT));
        dialog.getTargetTreeViewer().setLabelProvider(new EndpointLabelProvider(Type.INPUT));

        dialog.getSourceTreeViewer().setContentProvider(new EndpointContentProvider(Type.OUTPUT));
        dialog.getTargetTreeViewer().setContentProvider(new EndpointContentProvider(Type.INPUT));

        dialog.getSourceTreeViewer().setInput(description);
        dialog.getTargetTreeViewer().setInput(description);

        if (source != null) {
            dialog.getSourceTreeViewer().expandToLevel(source, 2);
            dialog.getSourceTreeViewer().setSelection(new StructuredSelection(source));

        }

        if (target != null) {
            dialog.getTargetTreeViewer().expandToLevel(target, 2);
            dialog.getTargetTreeViewer().setSelection(new StructuredSelection(target));

        }

        dialog.getCanvas().initialize(description, dialog.getSourceTreeViewer(), dialog.getTargetTreeViewer());

        // Repaint
        dialog.getSourceTree().addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent event) {
                dialog.getCanvas().repaint();
            }
        });
        dialog.getTargetTree().addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent event) {
                dialog.getCanvas().repaint();
            }
        });
        dialog.getCanvas().repaint();

        // DND
        int operations = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transfer = new Transfer[] { TextTransfer.getInstance() };

        dialog.getSourceTreeViewer().addDragSupport(operations, transfer, new OutputDragSourceListener(dialog));

        ViewerDropAdapter dropAdapter = new InputViewerDropAdapter(dialog, description);
        dropAdapter.setFeedbackEnabled(false);
        Transfer[] dropTransfer = new Transfer[] { TextTransfer.getInstance() };
        dialog.getTargetTreeViewer().addDropSupport(operations, dropTransfer, dropAdapter);
    }

    /**
     * Listener for handling dragging outputs.
     * @author Doreen Seider
     */
    public static class OutputDragSourceListener implements DragSourceListener {

        private ConnectionDialog dialog;

        public OutputDragSourceListener(ConnectionDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void dragFinished(DragSourceEvent event) {
        }

        @Override
        public void dragSetData(DragSourceEvent event) {
            Object selectedElement = ((ITreeSelection) dialog.getSourceTreeViewer().getSelection()).getFirstElement();
            String data = null;
            if (selectedElement instanceof Endpoint) {
                data = getDataString((Endpoint) selectedElement);
            } else if (selectedElement instanceof EndpointGroup) {
                data = getDataString((EndpointGroup) selectedElement);
            } else if (selectedElement instanceof WorkflowNode) {
                data = getDataString((WorkflowNode) selectedElement); 
            }
            event.data = data;
        }

        @Override
        public void dragStart(DragSourceEvent event) {
            Object item = ((ITreeSelection) dialog.getSourceTreeViewer().getSelection()).getFirstElement();
            event.doit = item instanceof Endpoint || item instanceof EndpointGroup || item instanceof WorkflowNode;
        }

        private String getDataString(String nodeId, String endpointName) {
            return nodeId + SLASH + endpointName;
        }

        private String getDataString(Endpoint endpoint) {
            return getDataString(endpoint.getWorkflowNode().getIdentifier(), endpoint.getCanonicalName());
        }

        private String getDataString(EndpointGroup endpointGroup) {
            StringBuffer data = new StringBuffer();
            for (EndpointGroup eg : endpointGroup.getChildEndpointGroups()) {
                data.append(getDataString(eg));
            }
            for (Endpoint e : endpointGroup.getChildEndpoints()) {
                data.append(getDataString(e) + SEMICOLON);
            }
            return new String(data);
        }

        private String getDataString(WorkflowNode workflowNode) {
            StringBuffer data = new StringBuffer();
            for (String name : workflowNode.getComponentDescription().getOutputDefinitions().keySet()) {
                data.append(getDataString(workflowNode.getIdentifier(), name) + SEMICOLON);
            }
            return new String(data);
        }
    }

    /**
     * Handling dropping to inputs.
     * @author Doreen Seider
     */
    public static class InputViewerDropAdapter extends ViewerDropAdapter {

        private ConnectionDialog dialog;

        private WorkflowDescription description;

        public InputViewerDropAdapter(ConnectionDialog dialog, WorkflowDescription description) {
            super(dialog.getTargetTreeViewer());
            this.dialog = dialog;
            this.description = description;
        }

        @Override
        public boolean performDrop(Object element) {

            boolean performed = false;

            Object currentTarget = getCurrentTarget();
            if (currentTarget instanceof Endpoint || currentTarget instanceof EndpointGroup || currentTarget instanceof WorkflowNode) {

                for (String sourceString : ((String) element).split(Pattern.quote(SEMICOLON))) {
                    String[] splittedSourceString = sourceString.split(Pattern.quote(SLASH), 2);
                    performEndpointDrop(splittedSourceString[0], splittedSourceString[1]);
                }
                performed = true;
            }

            dialog.getCanvas().repaint();

            return performed;
        }

        @Override
        public boolean validateDrop(Object dest, int operation, TransferData transferType) {
            return TextTransfer.getInstance().isSupportedType(transferType)
                && ((dest instanceof Endpoint || dest instanceof EndpointGroup || dest instanceof WorkflowNode));
        }

        private boolean performEndpointDrop(String sourceNodeId, String sourceEndpointName) {

            WorkflowNode sourceNode = null;
            for (final WorkflowNode node: description.getWorkflowNodes()) {
                if (node.getIdentifier().equals(sourceNodeId)) {
                    sourceNode = node;
                    break;
                }
            }

            Endpoint targetEndpoint = null;
            Object currentTarget = getCurrentTarget();
            if (currentTarget instanceof Endpoint) {
                targetEndpoint = (Endpoint) currentTarget;
            } else if (currentTarget instanceof EndpointGroup) {
                targetEndpoint = findEndpoint((EndpointGroup) currentTarget, sourceEndpointName); 
            } else if (currentTarget instanceof WorkflowNode) {
                targetEndpoint = findEndpoint((WorkflowNode) currentTarget, sourceEndpointName);
            }

            // if no matching target endpoint was found
            if (targetEndpoint == null) {
                return false;
            }

            Map<String, Class<? extends Serializable>> properties = sourceNode.getComponentDescription().getOutputDefinitions();
            if (properties.get(sourceEndpointName) != targetEndpoint.getType()) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.error, Messages.incompatibleTypes
                    + properties.get(sourceEndpointName).getName() + Messages.and + targetEndpoint.getType().getName());
                return false;
            }

            Connection connection = new Connection(sourceNode, sourceEndpointName,
                targetEndpoint.getWorkflowNode(), targetEndpoint.getCanonicalName());

            description.addConnection(connection);
            return true;

        }

        private Endpoint findEndpoint(EndpointGroup endpointGroup, String name) {
            for (Endpoint e : endpointGroup.getChildEndpoints()) {
                if (e.getCanonicalName().equals(name)) {
                    return e;
                }
            }
            for (EndpointGroup eg : endpointGroup.getChildEndpointGroups()) {
                Endpoint e = findEndpoint(eg, name);
                if (e != null) {
                    return e;
                }
            }

            return null;
        }

        private Endpoint findEndpoint(WorkflowNode workflowNode, String name) {
            EndpointHandlingHelper helper = new EndpointHandlingHelper();

            for (Endpoint e : helper.getEndpoints(workflowNode, Type.INPUT)) {
                if (e.getCanonicalName().equals(name)) {
                    return e;
                }
            }

            for (EndpointGroup eg : helper.getEndpointGroups(workflowNode, Type.INPUT)) {
                Endpoint e = findEndpoint(eg, name);
                if (e != null) {
                    return e;
                }
            }

            return null;
        }
    }

}
