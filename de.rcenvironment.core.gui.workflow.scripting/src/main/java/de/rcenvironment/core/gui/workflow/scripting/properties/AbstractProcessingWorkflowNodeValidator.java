/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.core.gui.workflow.scripting.properties;

import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.FACTORY;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.INIT;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.RUN;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.rcenvironment.commons.scripting.ScriptableComponentConstants.ScriptTime;
import de.rcenvironment.rce.gui.workflow.editor.validator.AbstractWorkflowNodeValidator;
import de.rcenvironment.rce.gui.workflow.editor.validator.WorkflowNodeValidationMessage;

/**
 * Validator for {@link BasicWrapperComponent}s.
 * 
 * @author Christian Weiss
 */
public class AbstractProcessingWorkflowNodeValidator extends AbstractWorkflowNodeValidator {

    private final ScriptTime scriptTime;

    protected AbstractProcessingWorkflowNodeValidator(final ScriptTime scriptTime) {
        this.scriptTime = scriptTime;
    }

    @Override
    public Collection<WorkflowNodeValidationMessage> validate() {
        final List<WorkflowNodeValidationMessage> messages = new LinkedList<WorkflowNodeValidationMessage>();
        final String doInitScriptKey = FACTORY.doScript(scriptTime, INIT);
        final String initScriptKey = FACTORY.script(scriptTime, INIT);
        final String initLanguageKey = FACTORY.language(scriptTime, INIT);
        final boolean hasInitScriptProperties = hasProperty(initScriptKey) && hasProperty(initLanguageKey);
        final boolean doInitScript = hasInitScriptProperties
                && (!hasProperty(doInitScriptKey) || getProperty(doInitScriptKey, Boolean.class, false));
        if (doInitScript) {
            final String initScript = getProperty(initScriptKey, String.class, "");
            final String initLanguage = getProperty(initLanguageKey, String.class, "");
            final boolean scriptMissing = initScript.isEmpty() && hasProperty(doInitScriptKey);
            final boolean languageMissing = initLanguage.isEmpty() && (!initScript.isEmpty() || hasProperty(doInitScriptKey));
            if (scriptMissing) {
                final WorkflowNodeValidationMessage message =
                    new WorkflowNodeValidationMessage(
                        WorkflowNodeValidationMessage.Type.ERROR,
                        initScriptKey,
                        Messages.bind2(Messages.scriptMissingRelative, scriptTime.name(), INIT.name()),
                        Messages.bind2(Messages.scriptMissingAbsolute, scriptTime.name(), INIT.name(), initScriptKey));
                messages.add(message);
            }
            if (languageMissing) {
                final WorkflowNodeValidationMessage message =
                    new WorkflowNodeValidationMessage(
                        WorkflowNodeValidationMessage.Type.ERROR,
                        initLanguageKey,
                        Messages.bind2(Messages.languageMissingRelative, scriptTime.name(), INIT.name()),
                        Messages.bind2(Messages.languageMissingAbsolute, scriptTime.name(), INIT.name(), initLanguageKey));
                messages.add(message);
            }
        }
        final String runScriptKey = FACTORY.script(scriptTime, RUN);
        final String runLanguageKey = FACTORY.language(scriptTime, RUN);
        final String runScript = getProperty(runScriptKey, String.class, "");
        final String runLanguage = getProperty(runLanguageKey, String.class, "");
        final boolean doRunScript = !runScript.isEmpty() || !runLanguage.isEmpty();
        if (doRunScript && runScript.isEmpty()) {
            final WorkflowNodeValidationMessage message =
                new WorkflowNodeValidationMessage(
                    WorkflowNodeValidationMessage.Type.ERROR,
                    runScriptKey,
                    Messages.bind2(Messages.scriptMissingRelative, scriptTime.name(), RUN.name()),
                    Messages.bind2(Messages.scriptMissingAbsolute, scriptTime.name(), RUN.name(), runScriptKey));
            messages.add(message);
        }
        if (doRunScript && runLanguage.isEmpty()) {
            final WorkflowNodeValidationMessage message =
                new WorkflowNodeValidationMessage(
                    WorkflowNodeValidationMessage.Type.ERROR,
                    runLanguageKey,
                    Messages.bind2(Messages.languageMissingRelative, scriptTime.name(), RUN.name()),
                    Messages.bind2(Messages.languageMissingAbsolute, scriptTime.name(), RUN.name(), runLanguageKey));
            messages.add(message);
        }
        return messages;
    }

}
