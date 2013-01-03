/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.workflow.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;


/**
 * EditPart representing all connections between two workflow nodes.
 *
 * @author Heinrich Wendel
 */
public class ConnectionPart extends AbstractConnectionEditPart {

    @Override 
    public IFigure createFigure() {
        Polyline figure = (Polyline) super.createFigure();
        PolylineConnection connection = new PolylineConnection();
        connection.setPoints(figure.getPoints());
        
        if (((ConnectionWrapper) getModel()).getSourceArrow()) {
            connection.setSourceDecoration(new PolylineDecoration());
        }
        
        if (((ConnectionWrapper) getModel()).getTargetArrow()) {
            connection.setTargetDecoration(new PolylineDecoration());
        }
        return connection;
    }
    
    @Override
    protected void createEditPolicies() {
    }
    
}
