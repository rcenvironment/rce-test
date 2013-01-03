/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.scripting.internal;

import de.rcenvironment.rce.scripting.ScriptingService;
import de.rcenvironment.rce.scripting.ScriptingServiceTest;

/**
 * Test for {@link ScriptingServiceImpl}.
 * 
 * @author Christian Weiss
 */
public class ScriptingServiceImplTest extends ScriptingServiceTest {

    @Override
    protected ScriptingService getService() {
        final ScriptingService result = new ScriptingServiceImpl();
        return result;
    }

}
