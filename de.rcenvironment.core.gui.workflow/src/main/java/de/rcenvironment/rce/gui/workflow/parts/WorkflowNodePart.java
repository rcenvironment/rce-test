/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySource;

import de.rcenvironment.rce.component.ChangeSupport;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.editor.WorkflowEditorAction;
import de.rcenvironment.rce.gui.workflow.editor.WorkflowPaletteFactory;
import de.rcenvironment.rce.gui.workflow.editor.commands.ConnectionCreateCommand;
import de.rcenvironment.rce.gui.workflow.editor.commands.WorkflowNodeDeleteCommand;
import de.rcenvironment.rce.gui.workflow.editor.properties.ComponentPropertySource;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidationMessage;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidationSupport;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidityStateEvent;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidityStateListener;
import de.rcenvironment.rce.gui.workflow.parts.ReadonlyWorkflowNodePart.ComponentStateFigureImpl;

/**
 * Part representing a {@link WorkflowNode}.
 * 
 * <p>For information about how the validation works, see {@link WorkflowNodeValidatorSupport}.</p>
 * 
 * @author Heinrich Wendel
 * @author Christian Weiss
 */
public class WorkflowNodePart extends AbstractGraphicalEditPart implements PropertyChangeListener, NodeEditPart {

    private static final Image ERROR_IMAGE = ImageDescriptor.createFromURL(
        ComponentStateFigureImpl.class.getResource("/resources/icons/error.gif")).createImage();
    
    private static final Image WARNING_IMAGE = ImageDescriptor.createFromURL(
        ComponentStateFigureImpl.class.getResource("/resources/icons/warning.gif")).createImage();

    private final WorkflowNodeValidationSupport validationSupport = new WorkflowNodeValidationSupport();
    
    private final IFigure errorFigure = new ImageFigure(ERROR_IMAGE);
    {
        final int offset = 3;
        final int size = 16;
        errorFigure.setBounds(new Rectangle(offset, offset, size, size));
        errorFigure.setVisible(false);
    }

    private final IFigure warningFigure = new ImageFigure(WARNING_IMAGE);
    {
        final int offsetX = 56;
        final int offsetY = 3;
        final int size = 16;
        warningFigure.setBounds(new Rectangle(offsetX, offsetY, size, size));
        warningFigure.setVisible(false);
    }

    /**
     * {@link WorkflowNodeValidityStateListener} to update the valid state of this
     * {@link WorkflowNodePart} using {@link #updateValid(boolean)}.
     */
    private final WorkflowNodeValidityStateListener validityStateListener = new WorkflowNodeValidityStateListener() {

        @Override
        public void handleWorkflowNodeValidityStateEvent(final WorkflowNodeValidityStateEvent event) {
            updateValid();
        }

    };

    public WorkflowNodePart() {
        validationSupport.addWorkflowNodeValidityStateListener(validityStateListener);
    }

    @Override
    public void activate() {
        super.activate();
        ((ChangeSupport) getModel()).addPropertyChangeListener(this);
        validationSupport.setWorkflowNode(getWorkflowNode());
        updateValid();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ((ChangeSupport) getModel()).removePropertyChangeListener(this);
        validationSupport.setWorkflowNode(null);
    }

    private List<WorkflowNodeValidationMessage> getValidationMessages() {
        final List<WorkflowNodeValidationMessage> result = new LinkedList<WorkflowNodeValidationMessage>(validationSupport.getMessages());
        return result;
    }

    private String getValidationMessageText(final WorkflowNodeValidationMessage.Type type) {
        final StringBuilder builder = new StringBuilder();
        for (final WorkflowNodeValidationMessage message : getValidationMessages()) {
            if (type == null || type == message.getType()) {
                String messageText = message.getAbsoluteMessage();
                if (messageText == null || messageText.isEmpty()) {
                    final String property = message.getProperty();
                    final String relativeMessage = message.getRelativeMessage();
                    if (property == null || property.isEmpty()) {
                        messageText = relativeMessage;
                    } else {
                        messageText = String.format("%s: %s", property, relativeMessage);
                    }
                }
                builder.append(messageText);
                builder.append("\n");
            }
        }
        return builder.toString().trim();
    }

    /**
     * Updates the visual indicators for {@link WorkflowNodeValidationMessage}s and refreshes the
     * graphical representation of this {@link WorkflowNodePart}.
     * 
     * @param valid true, if validation yielded not {@link WorkflowNodeValidationMessage}s.
     */
    private void updateValid() {
        final boolean valid = validationSupport.isValid();
        final String errorText = getValidationMessageText(WorkflowNodeValidationMessage.Type.ERROR);
        errorFigure.setVisible(!errorText.isEmpty());
        if (!valid) {
            errorFigure.setToolTip(new Label(errorText));
        }
        final String warningText = getValidationMessageText(WorkflowNodeValidationMessage.Type.WARNING);
        warningFigure.setVisible(!warningText.isEmpty());
        if (!valid) {
            warningFigure.setToolTip(new Label(warningText));
        }
        refresh();
        refreshVisuals();
    }

    protected WorkflowNode getWorkflowNode() {
        return (WorkflowNode) getModel();
    }

    @Override
    protected IFigure createFigure() {
        final IFigure figure = createBaseFigure();
        figure.add(errorFigure);
        figure.add(warningFigure);
        return figure;
    }

    protected IFigure createBaseFigure() {
        Image image = null;
        byte[] icon = ((WorkflowNode) getModel()).getComponentDescription().getIcon32();
        if (icon != null) {
            image = new Image(Display.getCurrent(), new ByteArrayInputStream(icon));
        } else {
            image = ImageDescriptor.createFromURL(
                WorkflowPaletteFactory.class.getResource("/resources/icons/component32.gif")).createImage(); //$NON-NLS-1$
        }
        
        final String labelText = ((WorkflowNode) getModel()).getName();
        final Label label = new Label(labelText, image);
        label.setOpaque(true);
        label.setTextPlacement(PositionConstants.SOUTH);

        final int white = 255;
        final int yellow = 225;
        label.setBackgroundColor(new Color(null, white, white, yellow));
        label.setBorder(new LineBorder());
        return label;
    }
    
    /**
     * Set the tooltip text.
     */
    protected void setTooltipText() {   
        String[] splitClass = ((WorkflowNode) getModel()).getComponentDescription().toString().split("_");
        final String nodeClass = splitClass[splitClass.length - 1];
        final String tooltipText = " " + nodeClass + " - " + ((WorkflowNode) getModel()).getName();
        getFigure().setToolTip(new Label(tooltipText));
    }
    
    @Override
    protected void refreshVisuals() {
        Point loc = new Point(((WorkflowNode) getModel()).getX(), ((WorkflowNode) getModel()).getY());
        
        final int width = 75;
        final int height = 75;
        Rectangle r = new Rectangle(loc, new Dimension(width, height));
        
        final Label label = (Label) getFigure();
        label.setText(getWorkflowNode().getName());
        
        setTooltipText();
        
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, label, r);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (WorkflowNode.LOCATION_PROP.equals(prop)) {
            refreshVisuals();
        } else if (WorkflowNode.NAME_PROP.equals(prop)) {
            refreshVisuals();
        }
    }
    
    /**
     * Method called by the WorkflowPart to refresh the connections.
     */
    public void refreshConnections() {
        refreshSourceConnections();
        refreshTargetConnections();    
    }
    
    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        return new ChopboxAnchor(getFigure());
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return new ChopboxAnchor(getFigure());
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        return new ChopboxAnchor(getFigure());
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return new ChopboxAnchor(getFigure());
    }
    
    @Override
    protected List<ConnectionWrapper> getModelSourceConnections() {
        List<ConnectionWrapper> sourceConnections = new ArrayList<ConnectionWrapper>();
        
        for (ConnectionWrapper c: ((WorkflowPart) getParent()).getConnections()) {
            if (c.getSource().equals(getModel())) {
                sourceConnections.add(c);
            }
        }
        
        return sourceConnections;
    }

    @Override
    protected List<ConnectionWrapper> getModelTargetConnections() {   
        List<ConnectionWrapper> targetConnections = new ArrayList<ConnectionWrapper>();

        for (ConnectionWrapper c: ((WorkflowPart) getParent()).getConnections()) {
            if (c.getTarget().equals(getModel())) {
                targetConnections.add(c);
            }
        }
        
        return targetConnections;
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
        if (type == IPropertySource.class) {
            return new ComponentPropertySource(getViewer().getEditDomain().getCommandStack(), (WorkflowNode) getModel());
        }
        return super.getAdapter(type);
    }


    @Override
    public void performRequest(Request req) {
        if (req.getType().equals(RequestConstants.REQ_OPEN)) {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                    "org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
            } catch (PartInitException e) {
                throw new RuntimeException(e);
            }
        } else if (req.getType().equals(RequestConstants.REQ_DIRECT_EDIT)) {
            performDefaultAction();
        }
    }
    
    @Override
    protected void createEditPolicies() {
        // allow removal of the associated model element
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new DeleteEditPolicy());

        // allow connections
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ConnectEditPolicy());
    }
    
    private void performDefaultAction() {
        IConfigurationElement[] confElements = Platform.getExtensionRegistry()
            .getConfigurationElementsFor("de.rcenvironment.rce.gui.workflow.editorActions"); //$NON-NLS-1$

        for (final IConfigurationElement confElement : confElements) {

            WorkflowNode node = getWorkflowNode();

            if (node.getComponentDescription().getIdentifier().matches(confElement.getAttribute("component"))
                && confElement.getAttribute("default") != null
                && Boolean.TRUE.toString().matches(confElement.getAttribute("default"))) { //$NON-NLS-1$

                final WorkflowEditorAction action;
                try {
                    Object actionObject = (WorkflowEditorAction) confElement.createExecutableExtension("class");
                    if (!(actionObject instanceof WorkflowEditorAction)) {
                        throw new RuntimeException(String.format(
                            "Class in attribute 'class' is not a subtype of '%s'.",
                            WorkflowEditorAction.class.getName()));
                    }
                    action = (WorkflowEditorAction) actionObject;
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
                action.setWorkflowNode(node);    
                action.performAction();
                break;
            }
        }
    }


    /**
     * EditPolicy that allows removal of nodes.
     *
     * @author Heinrich Wendel
     */
    class DeleteEditPolicy extends ComponentEditPolicy {

        @Override
        protected Command createDeleteCommand(GroupRequest deleteRequest) {
            Object parent = getHost().getParent().getModel();
            Object child = getHost().getModel();
            if (parent instanceof WorkflowDescription && child instanceof WorkflowNode) {
                return new WorkflowNodeDeleteCommand((WorkflowDescription) parent, (WorkflowNode) child);
            }
            return super.createDeleteCommand(deleteRequest);
        }
    }

    /**
     * EditPolicy that allows connections.
     *
     * @author Heinrich Wendel
     */
    class ConnectEditPolicy extends GraphicalNodeEditPolicy {

        @Override
        protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
            ConnectionCreateCommand cmd = (ConnectionCreateCommand) request.getStartCommand();
            cmd.setTarget((WorkflowNode) getHost().getModel());
            return cmd;
        }

        @Override
        protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
            WorkflowNode source = (WorkflowNode) getHost().getModel();
            ConnectionCreateCommand cmd = new ConnectionCreateCommand((WorkflowDescription) getParent().getModel(), source);
            request.setStartCommand(cmd);
            return cmd;
        }
        
        @Override
        protected Command getReconnectSourceCommand(ReconnectRequest request) {
            return null;
        }

        @Override
        protected Command getReconnectTargetCommand(ReconnectRequest request) {
            return null;
        }
    }
}
