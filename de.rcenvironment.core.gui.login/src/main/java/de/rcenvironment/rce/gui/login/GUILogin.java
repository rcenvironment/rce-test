/*
 * Copyright (C) 2006-2011 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.gui.login;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;

import de.rcenvironment.rce.gui.login.internal.LoginDialog;
import de.rcenvironment.rce.gui.login.internal.Messages;
import de.rcenvironment.rce.login.AbstractLogin;
import de.rcenvironment.rce.login.LoginInput;

/**
 * 
 * Concrete implementation of {@link AbstractLogin} for graphical login.
 *
 * @author Doreen Seider
 * @author Heinrich Wendel
 */
public class GUILogin extends AbstractLogin {

    @Override
    protected LoginInput getLoginInput() {
        LoginInput loginInput = null;
        
        LoginDialog loginDialog = new LoginDialog(authenticationService, loginConfiguration);

        if (loginDialog.open() == Window.OK) {
            loginInput = loginDialog.getLoginInput();
        }

        return loginInput;
    }

    @Override
    protected void informUserAboutError(String errorMessage, Throwable e) {
        MessageDialog.openError(null, Messages.login, errorMessage);
    }

}
