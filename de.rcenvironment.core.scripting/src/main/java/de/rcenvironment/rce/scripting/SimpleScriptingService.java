/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.scripting;

import javax.script.ScriptEngine;

import de.rcenvironment.commons.ServiceUtils;
import de.rcenvironment.commons.scripting.ScriptLanguage;
import de.rcenvironment.commons.scripting.ScriptLanguage.NoEngineException;

/**
 * Class providing easy access to the base implementation of {@link ScriptingService}. Forwards
 * everything to a dynamically bound {@link ScriptingService}.
 * 
 * @author Christian Weiss
 */
public class SimpleScriptingService implements ScriptingService {

    private static final ScriptingService NULL_SERVICE = ServiceUtils.createNullService(ScriptingService.class);

    private static ScriptingService scriptingService = NULL_SERVICE;

    protected void bindScriptingService(final ScriptingService service) {
        SimpleScriptingService.scriptingService = service;
    }

    protected void unbindScriptingService(final ScriptingService service) {
        SimpleScriptingService.scriptingService = NULL_SERVICE;
    }

    @Override
    public boolean supportsScriptLanguage(ScriptLanguage language) {
        return SimpleScriptingService.scriptingService.supportsScriptLanguage(language);
    }

    @Override
    public ScriptEngine createScriptEngine(ScriptLanguage language) throws NoEngineException {
        return SimpleScriptingService.scriptingService.createScriptEngine(language);
    }

}
