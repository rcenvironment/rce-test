/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.authentication;


/**
 * Represents a user used for RCE single-user-mode.
 *
 * @author Doreen Seider
 */
public class SingleUser extends User {

    private static final long serialVersionUID = -6958573657014138419L;

    public SingleUser(int validityInDays) {
        super(validityInDays);
    }

    @Override
    public String getUserId() {
        return "Chief Engineer";
    }

    @Override
    public String getDomain() {
        return "DLR";
    }

    @Override
    public Type getType() {
        return Type.single;
    }

}
