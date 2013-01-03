/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.component.scripting;

import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.DO_POST_INIT_SCRIPT;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.DO_PRE_INIT_SCRIPT;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.INIT;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.POST;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.POST_INIT_SCRIPT;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.POST_INIT_SCRIPT_LANGUAGE;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.POST_RUN_SCRIPT;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.POST_RUN_SCRIPT_LANGUAGE;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.PRE;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.PRE_INIT_SCRIPT;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.PRE_INIT_SCRIPT_LANGUAGE;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.PRE_RUN_SCRIPT;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.PRE_RUN_SCRIPT_LANGUAGE;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.RUN;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import de.rcenvironment.commons.scripting.ScriptLanguage;
import de.rcenvironment.commons.scripting.ScriptableComponentConstants.ComponentRunMode;
import de.rcenvironment.commons.scripting.ScriptableComponentConstants.ScriptTime;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ComponentInstanceInformationUtils;
import de.rcenvironment.rce.component.DefaultComponent;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.scripting.ScriptingService;

/**
 * 
 * Abstract implementation of {@link ScriptableComponent} to be used as base class for concrete
 * implementations.
 * 
 * @author Christian Weiss
 */
public abstract class AbstractScriptingComponent extends DefaultComponent {

    protected static final TriggerMode TRIGGER_MODE_DEFAULT = TriggerMode.Automatic;

    private static final String CURRENT_DIR = ".";

    private static final String OUTPUT_TAG = "_rce_output_";

    private static final String FILE_SEPERATOR = "/";
    
    private static ScriptingService scriptingService;

    private Map<String, Object> scriptExecConfig = new HashMap<String, Object>();

    /**
     * The mode the script execution is triggered.
     *
     * @author Christian Weiss
     */
    protected enum TriggerMode {
        /** Automatic triggering - around the execution. */
        Automatic,
        /** Manual triggering - arbitrary execution time. */
        Manual;
    }

    private final TriggerMode triggerMode;

    private final Map<ScriptTime, Map<ComponentRunMode, String>> scripts = new HashMap<ScriptTime, Map<ComponentRunMode, String>>();

    private final Map<ScriptTime, Map<ComponentRunMode, String>> languages = new HashMap<ScriptTime, Map<ComponentRunMode, String>>();

    private final Map<ScriptTime, Map<ComponentRunMode, Boolean>> doScripts = new HashMap<ScriptTime, Map<ComponentRunMode, Boolean>>();

    private final PrintWriter scriptWriter = new PrintWriter(getScriptWriter());

    private final PrintWriter scriptErrorWriter = new PrintWriter(getScriptErrorWriter());

    protected AbstractScriptingComponent() {
        this(TRIGGER_MODE_DEFAULT);
    }

    protected AbstractScriptingComponent(final TriggerMode triggerMode) {
        this.triggerMode = triggerMode;
    }

    /**
     * Factory method for the {@link InfoLogWriter} instance to be used for print commands in scripts.
     * 
     * @return the {@link InfoLogWriter} instance to be used for print commands in scripts
     */
    protected Writer getScriptWriter() {
        return new InfoLogWriter();
    }

    /**
     * Factory method for the {@link InfoLogWriter} instance to be used to output errors occuring in scripts.
     * 
     * @return the {@link InfoLogWriter} instance to be used to output errors occuring in scripts
     */
    protected Writer getScriptErrorWriter() {
        return new ErrorLogWriter();
    }

    protected boolean hasScriptingService() {
        final boolean result = scriptingService != null;
        return result;
    }

    protected ScriptingService getScriptingService() {
        assert hasScriptingService();
        return scriptingService;
    }

    protected void bindScriptingService(final ScriptingService service) {
        scriptingService = service;
    }

    protected void unbindScriptingService(final ScriptingService service) {
        /*
         * nothing to do here, this unbind method is only needed, because DS is throwing an
         * exception when disposing otherwise. probably a bug
         */
    }

    public TriggerMode getTriggerMode() {
        return triggerMode;
    }

    @Override
    public void onPrepare(ComponentInstanceInformation incCompInstanceInformation) throws ComponentException {
        super.onPrepare(incCompInstanceInformation);
        String script;
        String language;
        boolean hasScript;
        boolean doScript;
        // pre init
        doScript = ComponentInstanceInformationUtils.getConfigurationValue(DO_PRE_INIT_SCRIPT, Boolean.class, false, instInformation);
        if (!doScript) {
            setScript(PRE, INIT, doScript, null, null);
        } else {
            script = ComponentInstanceInformationUtils.getConfigurationValue(PRE_INIT_SCRIPT, String.class, "", instInformation);
            language = ComponentInstanceInformationUtils.getConfigurationValue(PRE_INIT_SCRIPT_LANGUAGE, String.class, "", instInformation);
            hasScript = script != null && !script.isEmpty()
                && language != null && !language.isEmpty();
            if (!hasScript) {
                throw new ComponentException("pre init script not configured correctly");
            }
            setScript(PRE, INIT, doScript, script, language);
        }
        // pre run
        doScript = ComponentInstanceInformationUtils.hasInputs(instInformation);
        if (!doScript) {
            setScript(PRE, RUN, doScript, null, null);
        } else {
            script = ComponentInstanceInformationUtils.getConfigurationValue(PRE_RUN_SCRIPT, String.class, "", instInformation);
            language = ComponentInstanceInformationUtils.getConfigurationValue(PRE_RUN_SCRIPT_LANGUAGE, String.class, "", instInformation);
            if (script != null && !script.isEmpty() && language.isEmpty()) {
                throw new ComponentException("pre run script configured, but language not set");
            }
            hasScript = script != null && !script.isEmpty()
                && language != null && !language.isEmpty();
            doScript = hasScript;
            setScript(PRE, RUN, doScript, script, language);
        }
        // post init
        doScript = ComponentInstanceInformationUtils.getConfigurationValue(DO_POST_INIT_SCRIPT, Boolean.class, false, instInformation);
        if (!doScript) {
            setScript(POST, INIT, doScript, null, null);
        } else {
            script = ComponentInstanceInformationUtils.getConfigurationValue(POST_INIT_SCRIPT, String.class, "", instInformation);
            language = ComponentInstanceInformationUtils.getConfigurationValue(POST_INIT_SCRIPT_LANGUAGE, String.class, "",
                instInformation);
            hasScript = script != null && !script.isEmpty()
                && language != null && !language.isEmpty();
            if (!hasScript) {
                throw new ComponentException("post init script not configured correctly");
            }
            setScript(POST, INIT, doScript, script, language);
        }
        // post run
        doScript = ComponentInstanceInformationUtils.hasInputs(instInformation);
        if (!doScript) {
            setScript(POST, RUN, doScript, null, null);
        } else {
            script = ComponentInstanceInformationUtils.getConfigurationValue(POST_RUN_SCRIPT, String.class, "", instInformation);
            language = ComponentInstanceInformationUtils.getConfigurationValue(POST_RUN_SCRIPT_LANGUAGE, String.class, "", instInformation);
            if (script != null && !script.isEmpty() && language.isEmpty()) {
                throw new ComponentException("post run script configured, but language not set");
            }
            hasScript = script != null && !script.isEmpty()
                && language != null && !language.isEmpty();
            doScript = hasScript;
            setScript(POST, RUN, doScript, script, language);
        }
    }

    private void setScript(final ScriptTime scriptTime, final ComponentRunMode componentRunMode,
        final Boolean doScript, final String script, final String language) {
        if (doScript && (script == null || script.isEmpty()
            || language == null || language.isEmpty())) {
            throw new RuntimeException("script and language needed");
        }
        if (!doScripts.containsKey(scriptTime)) {
            doScripts.put(scriptTime, new HashMap<ComponentRunMode, Boolean>());
        }
        if (!scripts.containsKey(scriptTime)) {
            scripts.put(scriptTime, new HashMap<ComponentRunMode, String>());
        }
        if (!languages.containsKey(scriptTime)) {
            languages.put(scriptTime, new HashMap<ComponentRunMode, String>());
        }
        doScripts.get(scriptTime).put(componentRunMode, doScript);
        scripts.get(scriptTime).put(componentRunMode, script);
        languages.get(scriptTime).put(componentRunMode, language);
    }

    private boolean doScript(final ScriptTime scriptTime, final ComponentRunMode componentRunMode) {
        Boolean result = false;
        if (doScripts.containsKey(scriptTime)) {
            if (doScripts.get(scriptTime).containsKey(componentRunMode)) {
                result = doScripts.get(scriptTime).get(componentRunMode);
            }
        }
        return result;
    }

    private String getScript(final ScriptTime scriptTime, final ComponentRunMode componentRunMode) {
        String result = null;
        if (scripts.containsKey(scriptTime)) {
            result = scripts.get(scriptTime).get(componentRunMode);
        }
        return result;
    }

    private String getLanguage(final ScriptTime scriptTime, final ComponentRunMode componentRunMode) {
        String result = null;
        if (languages.containsKey(scriptTime)) {
            result = languages.get(scriptTime).get(componentRunMode);
        }
        return result;
    }

    @Override
    public final boolean runInitial(final boolean inputsConnected) throws ComponentException {
        if (doScript(PRE, INIT) && triggerMode == TriggerMode.Automatic) {
            executeScript(PRE, INIT, CURRENT_DIR, null);
        }
        final boolean result = runInitialInScriptingComponent(inputsConnected);
        if (doScript(POST, INIT) && triggerMode == TriggerMode.Automatic) {
            executeScript(POST, INIT, CURRENT_DIR, null);
        }
        return result;
    }

    protected abstract boolean runInitialInScriptingComponent(boolean inputsConnected) throws ComponentException;
    @Override
    public boolean runStep(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        if (doScript(PRE, RUN) && triggerMode == TriggerMode.Automatic) {
            executeScript(PRE, RUN, CURRENT_DIR, inputValues);
        }
        final boolean result = runStepInScriptingComponent(inputValues);
        if (doScript(POST, RUN) && triggerMode == TriggerMode.Automatic) {
            executeScript(POST, RUN, CURRENT_DIR, inputValues);
        }
        return result;
    }

    protected void triggerScript(final ScriptTime scriptTime, final ComponentRunMode componentRunMode,
        String workDir) throws ComponentException {
        triggerScript(scriptTime, componentRunMode, workDir, null);
    }

    protected void triggerScript(final ScriptTime scriptTime, final ComponentRunMode componentRunMode,
        String workDir, final Map<String, Deque<Input>> inputValues)
        throws ComponentException {
        assertManualMode();
        if (doScript(scriptTime, componentRunMode)) {
            executeScript(scriptTime, componentRunMode, workDir, inputValues);
        }
    }

    private void assertManualMode() {
        if (triggerMode != TriggerMode.Manual) {
            throw new UnsupportedOperationException("TriggerMode ist not set to manual");
        }
    }

    protected abstract boolean runStepInScriptingComponent(final Map<String, Deque<Input>> inputValues)
        throws ComponentException;

    protected void executeScript(final ScriptTime scriptTime, final ComponentRunMode componentRunMode,
            String workDir, final Map<String, Deque<Input>> inputValues)
        throws ComponentException {
        final String script = getScript(scriptTime, componentRunMode);
        final String language = getLanguage(scriptTime, componentRunMode);
        if (script == null || script.isEmpty()
                || language == null || language.isEmpty()) {
            throw new ComponentException("script or scripting language not set");
        }
        logger.debug(String.format("Executing %s %s script of '%s'", scriptTime, componentRunMode,
            instInformation.getComponentName()));
        executeScript(scriptTime, componentRunMode, script, language, workDir, inputValues);
    }

    protected void executeScript(final ScriptTime scriptTime, final ComponentRunMode componentRunMode,
            String script, final String language, String workDir,
            final Map<String, Deque<Input>> inputValues) throws ComponentException {
        final ScriptEngine engine = createScriptEngineWithChecks(language);
        try {
            engine.put("config", scriptExecConfig);
            engine.getContext().setWriter(scriptWriter);
            engine.getContext().setErrorWriter(scriptErrorWriter);
            
            script = replaceVariables(script, workDir, inputValues);
            beforeScriptExecution(engine);
            engine.eval(script);
            final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            /*
             * Extract all values calculated/set in the script to a custom scope so the
             * calculated/set values are accessible via the current Context.
             */
            for (final String key : bindings.keySet()) {
                Object value = bindings.get(key);
                if (value.getClass().getSimpleName().equals("NativeJavaObject")) {
                    try {
                        value = value.getClass().getMethod("unwrap").invoke(value);
                    } catch (IllegalArgumentException e) {
                        e = null;
                    } catch (SecurityException e) {
                        e = null;
                    } catch (IllegalAccessException e) {
                        e = null;
                    } catch (InvocationTargetException e) {
                        e = null;
                    } catch (NoSuchMethodException e) {
                        e = null;
                    }
                }
                scriptExecConfig.put(key, value);
                
                if (scriptTime == ScriptTime.POST) {
                    if (key.startsWith(OUTPUT_TAG) && key.endsWith(OUTPUT_TAG)) {
                        instInformation.getOutput(extractOutputName(key)).write((Serializable) value);
                    }                    
                }
            }
            afterScriptExecution(engine);
        } catch (final ScriptException e) {
            final String message =
                String.format("%s in %s %s script: %s", e.getClass().getSimpleName(), scriptTime, componentRunMode, e.getMessage());
            throw new ComponentException(message, e);
        }
    }
    
    private String extractOutputName(String taggedOutputName) {
        return taggedOutputName.replaceAll(OUTPUT_TAG, "");
    }
    
    private String replaceVariables(String script, String workDir, Map<String, Deque<Input>> inputValues) {
        script = replaceInputVariables(script, inputValues);
        script = replaceOutputVariables(script, instInformation.getOutputDefinitions().keySet());
        script = replaceCWDVariable(script, workDir);
        return script;
    }

    private String replaceInputVariables(String script, Map<String, Deque<Input>> inputValues) {
        if (inputValues != null) {
            for (String inputName : inputValues.keySet()) {
                if (!inputValues.get(inputName).isEmpty()) {
                    script = script.replace(String.format(ScriptingComponentConstants.VARIABLE, inputName),
                        inputValues.get(inputName).removeFirst().getValue().toString());
                }
            }
        }
        return script;
    }
    
    private String replaceOutputVariables(String script, Set<String> outputs) {
        for (String outputName : outputs) {
            script = script.replace(String.format(ScriptingComponentConstants.VARIABLE, outputName),
                OUTPUT_TAG + outputName + OUTPUT_TAG);
        }
        return script;
    }

    private String replaceCWDVariable(String script, String workDir) {
        String cwd = workDir;
        cwd = cwd.replace("\\\\", FILE_SEPERATOR);
        cwd = cwd.replace("\\", FILE_SEPERATOR);
        script = script.replace(ScriptingComponentConstants.VARIABLE_CWD, cwd);
        return script;
    }

    protected void beforeScriptExecution(final ScriptEngine engine) {
        /* empty default implementation */
    }

    protected void afterScriptExecution(final ScriptEngine engine) {
        /* empty default implementation */
    }

    protected ScriptEngine createScriptEngineWithChecks(final String language) throws ComponentException {
        if (!hasScriptingService()) {
            throw new ComponentException("No Scripting service connected");
        }
        final ScriptLanguage scriptLanguage;
        try {
            scriptLanguage = ScriptLanguage.valueOf(language);
        } catch (final IllegalArgumentException e) {
            throw new ComponentException(String.format("Scripting language '%s' is unknown", language));
        }
        if (!scriptingService.supportsScriptLanguage(scriptLanguage)) {
            throw new ComponentException(String.format("Scripting language '%s' is not supported by any registered ScriptEngine",
                scriptLanguage));
        }
        final ScriptEngine result = scriptingService.createScriptEngine(scriptLanguage);
        return result;
    }

    /**
     * An abstract {@link Writer} implementation redirecting to the {@link java.util.log.Logger} of the instance ({@link #logger}).
     *
     * @author Christian Weiss
     */
    protected abstract class AbstractLogWriter extends Writer {

        private StringBuffer buffer = new StringBuffer();

        @Override
        public final void write(final char[] cbuf, final int off, final int len) throws IOException {
            synchronized (buffer) {
                final char[] chars = Arrays.copyOfRange(cbuf, off, off + len);
                buffer.append(chars);
            }
        }

        @Override
        public final void flush() throws IOException {
            final String message;
            synchronized (buffer) {
                message = buffer.toString();
                buffer = new StringBuffer();
            }
            log(message);
        }

        @Override
        public final void close() throws IOException {
            buffer = null;
        }

        protected void log(final String message) {
            final Scanner scanner = new Scanner(message);
            while (scanner.hasNextLine()) {
                logLine(scanner.nextLine());
            }
        }

        protected abstract void logLine(final String line);

    }

    /**
     * {@link AbstractLogWriter} logging on {@link #logger} at log level INFO.
     *
     * @author Christian Weiss
     */
    protected class InfoLogWriter extends AbstractLogWriter {

        @Override
        protected void logLine(String line) {
            logger.info(line);
        }

    }

    /**
     * {@link AbstractLogWriter} logging on {@link #logger} at log level ERROR.
     *
     * @author Christian Weiss
     */
    protected class ErrorLogWriter extends AbstractLogWriter {

        @Override
        protected void logLine(String line) {
            logger.error(line);
        }

    }

}
