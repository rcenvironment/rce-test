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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.rcenvironment.rce.component.ChangeSupport;
import de.rcenvironment.rce.component.workflow.Connection;
import de.rcenvironment.rce.component.workflow.WorkflowDescription;
import de.rcenvironment.rce.component.workflow.WorkflowNode;
import de.rcenvironment.rce.gui.workflow.editor.commands.WorkflowNodeCreateCommand;
import de.rcenvironment.rce.gui.workflow.editor.commands.WorkflowNodeMoveCommand;

/**
 * Part holding a {@link WorkflowDescription}.
 * 
 * @author Heinrich Wendel
 */
public class WorkflowPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    private List<ConnectionWrapper> connections = new ArrayList<ConnectionWrapper>();

    @Override
    public void activate() {
        super.activate();
        ((ChangeSupport) getModel()).addPropertyChangeListener(this);
        updateConnectionWrappers();
        propertyChange(new PropertyChangeEvent(this, WorkflowDescription.CONNECTIONS_CHANGED_PROP, null, null));
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ((ChangeSupport) getModel()).removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (WorkflowDescription.CONNECTIONS_CHANGED_PROP.equals(prop)) {
            updateConnectionWrappers();
            for (Object part : getChildren()) {
                ((WorkflowNodePart) part).refreshConnections();
            }
        } else if (WorkflowDescription.NODES_CHANGED_PROP.equals(prop)) {
            refreshChildren();
        }
    }

    /**
     * Getter class called by the children to get all connections represented by
     * {@link ConnectionWrapper}s.
     * @return List of {@link ConnectionWrapper}s.
     */
    public List<ConnectionWrapper> getConnections() {
        return connections;
    }

    @Override
    protected List<WorkflowNode> getModelChildren() {
        return new ArrayList<WorkflowNode>(((WorkflowDescription) getModel()).getWorkflowNodes());
    }

    /**
     * Helper method to update all {@link ConnectionWrapper}s when a {@link Connection} was added or
     * removed.
     */
    private void updateConnectionWrappers() {
        connections.clear();

        for (Connection c : ((WorkflowDescription) getModel()).getConnections()) {
            final int minuseOne = -1;

            int contains2 = minuseOne;
            for (int i = 0; i < connections.size(); i++) {
                if (connections.get(i).getSource().equals(c.getTarget()) && connections.get(i).getTarget().equals(c.getSource())) {
                    contains2 = i;
                    break;
                }
            }

            int contains1 = minuseOne;
            for (int i = 0; i < connections.size(); i++) {
                if (connections.get(i).getSource().equals(c.getSource()) && connections.get(i).getTarget().equals(c.getTarget())) {
                    contains1 = i;
                    break;
                }
            }

            if (contains2 != minuseOne) {
                connections.get(contains2).setSourceArrow(true);
            } else if (contains1 == minuseOne) {
                ConnectionWrapper w = new ConnectionWrapper(c.getSource(), c.getTarget());
                w.setTargetArrow(true);
                connections.add(w);
            }
        }
    }

    @Override
    protected IFigure createFigure() {
        Figure f = new BackgroundLayer();
        f.setBorder(new MarginBorder(3));
        f.setLayoutManager(new FreeformLayout());

        // Create the static router for the connection layer
        ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
        connLayer.setAntialias(SWT.ON);
        connLayer.setConnectionRouter(new ShortestPathConnectionRouter(f));

        return f;
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new WorkflowXYLayoutEditPolicy());
    }

    /**
     * Policy responsible for creating new WorkflowNodes.
     * 
     * @author Heinrich Wendel
     */
    class WorkflowXYLayoutEditPolicy extends XYLayoutEditPolicy {

        @Override
        protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
            if (child instanceof WorkflowNodePart && constraint instanceof Rectangle) {
                return new WorkflowNodeMoveCommand((WorkflowNode) child.getModel(), request, (Rectangle) constraint);
            }
            return super.createChangeConstraintCommand(request, child, constraint);
        }

        @Override
        protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
            return null;
        }

        @Override
        protected Command getCreateCommand(CreateRequest request) {

            Object childClass = request.getNewObjectType();
            if (childClass == WorkflowNode.class) {

                return new WorkflowNodeCreateCommand((WorkflowNode) request.getNewObject(), (WorkflowDescription) getHost().getModel(),
                    (Rectangle) getConstraintFor(request));
            }
            return null;
        }
    }

    /**
     * Class for a custom background image.
     * 
     * @author Heinrich Wendel
     */
    private class BackgroundLayer extends FreeformLayer implements FigureListener {

        private Image orgImage;
        private Image image;
        private int x;
        private int y;
        
        public BackgroundLayer() {
            InputStream stream = getClass().getResourceAsStream("/resources/editor-background.png"); //$NON-NLS-1$
            if (stream != null) {
                orgImage = new Image(Display.getDefault(), stream);
            }
            resize();
            addFigureListener(this);
        }

        private void resize() {
            Rectangle targetRect = getBounds().getCopy();
            if (orgImage != null && targetRect.height != 0) {

                float scaleX = (float) ((float) targetRect.width / (float) orgImage.getBounds().width);
                float scaleY = (float) ((float) targetRect.height / (float) orgImage.getBounds().height);
    
                int sizeX;
                int sizeY;
                if (scaleX < scaleY) {
                    sizeX = (int) (orgImage.getBounds().width * scaleX);
                    sizeY = (int) (orgImage.getBounds().height * scaleX);            
                } else {
                    sizeX = (int) (orgImage.getBounds().width * scaleY);
                    sizeY = (int) (orgImage.getBounds().height * scaleY);
                }
                
                x = (targetRect.width - sizeX) / 2;
                y = (targetRect.height - sizeY) / 2;
    
                image = new Image(Display.getDefault(), orgImage.getImageData().scaledTo(sizeX, sizeY));
            }
        }
        
        protected void paintFigure(Graphics graphics) {
            if (image != null) {                
                graphics.drawImage(image, x, y);
            }
            super.paintFigure(graphics);
        }
        
        @Override
        public void figureMoved(IFigure arg0) {
            resize();
        }
    }
}
