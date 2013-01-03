/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.scripting;

import javax.script.ScriptEngine;

import de.rcenvironment.commons.scripting.ScriptLanguage;
import de.rcenvironment.commons.scripting.ScriptLanguage.NoEngineException;


/**
 * Service interface to be implemented by providers providing access to scripting.
 *
 * @author Christian Weiss
 */
public interface ScriptingService {

    /**
     * Returns whether a {@link ScriptEngine} for the given extension is registered.
     * 
     * @param language the language to be supported
     * @return true, if a {@link ScriptEngine} for the given extension is registered
     */
    boolean supportsScriptLanguage(ScriptLanguage language);

    /**
     * Creates and returns a {@link ScriptEngine} instance handling scripts of the specified {@link ScriptLanguage}.
     * 
     * @param language the {@link ScriptLanguage} to be supported by the returned {@link ScriptEngine}
     * @return a {@link ScriptEngine} supporting scripts of the specified {@link ScriptLanguage}
     * @throws NoEngineException if no {@link ScriptEngine} is registered, can be checked via
     *         {@link #hasScriptEngine()}
     */
    ScriptEngine createScriptEngine(ScriptLanguage language) throws NoEngineException;

//    /**
//     * Creates an instance of a {@link ScriptEngine} handling the {@link ScriptLanguage} registered with the specified type.
//     * 
//     * @param extension the extension of the {@link ScriptLanguage} to be supported by the returned {@link ScriptEngine}
//     * @return a {@link ScriptEngine} supporting scripts of the {@link ScriptLanguage} having the specified extension
//     */
//    ScriptEngine createScriptEngineByExtension(String extension);

}
