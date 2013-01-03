/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.login;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.rcenvironment.rce.authentication.AuthenticationException;
import de.rcenvironment.rce.authentication.Session;

/**
 * Handling re-login.
 *
 * @author Bea Hornef
 * @author Michael Drost
 */
public class GUIReLoginHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        try {
            // check if session already exists..
            Session.getInstance();
            new GUIReLogin().login();
            
        } catch (AuthenticationException e) {
            //if not, create a new session
            new GUILogin().login();
        }
        
        return null;
    }

}
