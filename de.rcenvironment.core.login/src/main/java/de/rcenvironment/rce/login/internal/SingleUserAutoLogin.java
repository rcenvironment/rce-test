/*
 * Copyright (C) 2006-2010 DLR Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.login.internal;

import de.rcenvironment.rce.authentication.AuthenticationException;
import de.rcenvironment.rce.login.AbstractLogin;
import de.rcenvironment.rce.login.LoginInput;

/**
 * Single user implementation of {@link AbstractLogin}.
 * @author Doreen Seider
 */
public class SingleUserAutoLogin extends AbstractLogin {

    @Override
    protected void informUserAboutError(String errorMessage, Throwable e) {
        LOGGER.error(errorMessage, e);
    }

    @Override
    protected LoginInput getLoginInput() throws AuthenticationException {
        return new LoginInput(true);
    }

}
