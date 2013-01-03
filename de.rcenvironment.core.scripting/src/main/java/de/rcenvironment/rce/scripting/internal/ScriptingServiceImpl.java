/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.scripting.internal;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.python.jsr223.PyScriptEngineFactory;

import de.rcenvironment.commons.scripting.ScriptLanguage;
import de.rcenvironment.commons.scripting.ScriptLanguage.NoEngineException;
import de.rcenvironment.rce.scripting.ScriptingService;


/**
 * Default implementation of {@link ScriptingService}.
 *
 * @author Christian Weiss
 * @author Robert Mischke (make Jython available from OSGi import)
 */
public class ScriptingServiceImpl implements ScriptingService {

    private final ScriptEngineManager engineManager = new ScriptEngineManager();
    
    public ScriptingServiceImpl() {
        // assertion: Jython should not be registered yet
        if (!supportsScriptLanguageExtension(ScriptLanguage.Jython.getExtension())) {
            engineManager.registerEngineExtension(ScriptLanguage.Jython.getExtension(), new PyScriptEngineFactory());
        }
        // register Jython manually from OSGi package import
    }

    @Override
    public boolean supportsScriptLanguage(final ScriptLanguage language) {
        final String extension = language.getExtension();
        final boolean result = supportsScriptLanguageExtension(extension);
        return result;
    }

    @Override
    public ScriptEngine createScriptEngine(final ScriptLanguage language) throws NoEngineException {
        assert supportsScriptLanguage(language);
        final String extension = language.getExtension();
        try {
            final ScriptEngine result = engineManager.getEngineByExtension(extension);
            return result;
        } catch (NoEngineException e) {
            throw new NoEngineException(language, e);
        }
    }

    protected boolean supportsScriptLanguageExtension(final String extension) {
        final ScriptEngine engine = engineManager.getEngineByExtension(extension);
        final boolean result = engine != null;
        return result;
    }

    protected ScriptEngine createScriptEngineByExtension(final String extension) {
        assert ScriptLanguage.getByExtension(extension) != null;
        assert supportsScriptLanguageExtension(extension);
        final ScriptEngine result = engineManager.getEngineByExtension(extension);
        if (result == null) {
            final ScriptLanguage language = ScriptLanguage.getByExtension(extension);
            throw new NoEngineException(language);
        }
        return result;
    }

}
