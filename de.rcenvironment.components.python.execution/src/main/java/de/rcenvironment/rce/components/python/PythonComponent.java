/*
 * Copyright (C) 2010-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.python;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.commons.channel.DataManagementFileReference;
import de.rcenvironment.commons.channel.VariantArray;
import de.rcenvironment.commons.executor.LocalApacheCommandLineExecutor;
import de.rcenvironment.commons.variables.BoundVariable;
import de.rcenvironment.commons.variables.VariableType;
import de.rcenvironment.components.python.gui.dm.PythonComponentHistoryObject;
import de.rcenvironment.core.component.executor.ScriptUsage;
import de.rcenvironment.core.component.executor.SshExecutorConstants;
import de.rcenvironment.executor.commons.ExecutorException;
import de.rcenvironment.executor.python.PythonExecutionContext;
import de.rcenvironment.executor.python.PythonExecutionResult;
import de.rcenvironment.executor.python.PythonExecutor;
import de.rcenvironment.executor.python.PythonExecutorFactory;
import de.rcenvironment.rce.component.ComponentConstants;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ComponentInstanceInformationUtils;
import de.rcenvironment.rce.component.ComponentState;
import de.rcenvironment.rce.component.ConsoleRow;
import de.rcenvironment.rce.component.DefaultComponent;
import de.rcenvironment.rce.component.datamanagement.stateful.SimpleComponentDataManagementService;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.components.python.commons.PythonComponentConstants;
import de.rcenvironment.rce.configuration.PersistentSettingsService;
import de.rcenvironment.rce.notification.DistributedNotificationService;
/**
 * Python {@link Component}.
 *
 * @author Markus Litz
 * @author Arne Bachmann
 */
public class PythonComponent extends DefaultComponent {
    /** Constant. */
    public static PersistentSettingsService persistentSettingsService;

    private static final int THOUSAND = 1000;

    private static final Log LOGGER = LogFactory.getLog(PythonComponent.class);

    private static ExecutorService threadPool;

    private static Map<? extends Serializable, VariableType> convertToVariable = new HashMap<Serializable, VariableType>() {
        private static final long serialVersionUID = -45512055668996734L;
        {
            put(null, VariableType.Empty);
            put(Boolean.class, VariableType.Logic);
            put(Byte.class, VariableType.Integer);
            put(Short.class, VariableType.Integer);
            put(Integer.class, VariableType.Integer);
            put(Long.class, VariableType.Integer);
            put(Float.class, VariableType.Real);
            put(Double.class, VariableType.Real);
            put(String.class, VariableType.String);
            put(File.class, VariableType.File);
        }

    };

    private static DistributedNotificationService notificationService;

    private static PythonExecutorFactory executorFactory;

    private ComponentInstanceInformation compInfo;

    private List<BoundVariable> inputVariables;

    private List<BoundVariable> outputVariables;

    /**
     * Waiting values that come into the component.
     */
    private Map<String, Deque<Input>> inputValues;

    /**
     * Names of all connected data management input channels.
     */
    private List<String> dmHandlesIn;

    /**
     * Names of all connected data management output channels.
     */
    private List<String> dmHandlesOut;

    /**
     * Names of all connected array input channels.
     */
    private List<String> inputArrays;

    /**
     * Names of all connected array output channels.
     */
    private List<String> outputArrays;

    static {
        threadPool = Executors.newCachedThreadPool();
    }

    protected void bindNotificationService(final DistributedNotificationService newNotificationService) {
        notificationService = newNotificationService;
    }

    protected void unbindNotificationService(final DistributedNotificationService oldNotificationService) {
        // nothing to do here, but if this unbind method is missing, OSGi DS failed when disposing component
        // (seems to be a DS bug)
    }

    protected void bindPythonExecutorFactory(final PythonExecutorFactory newPythonExecutorFactory) {
        executorFactory = newPythonExecutorFactory;
    }

    protected void unbindPythonExecutorFactory(final PythonExecutorFactory newPythonExecutorFactory) {
        // nothing to do here, but if this unbind method is missing, OSGi DS failed when disposing component
        // (seems to be a DS bug)
    }

    @Override
    public void onPrepare(final ComponentInstanceInformation information) throws ComponentException {

        super.onPrepare(information);


        LOGGER.debug("Python component preparing: " + information.getName());
        compInfo = information; // store channel information

        // define variables to consider for the script        
        inputVariables = new ArrayList<BoundVariable>(compInfo.getInputDefinitions().size());
        dmHandlesIn = new ArrayList<String>(); // names of input channels that expect dm references
        inputArrays = new ArrayList<String>(); // names of input channels that expect arrays
        inputValues = new HashMap<String, Deque<Input>>(); // name, value-queue for channel handling
        for (final Entry<String, ? extends Serializable> inputEntry: compInfo.getInputDefinitions().entrySet()) {
            final String key = inputEntry.getKey();
            final Serializable clazz = inputEntry.getValue();
            final VariableType type = convertToVariable.get(clazz); // determine variable type from channel
            if (type != null) {
                inputVariables.add(new BoundVariable(/* name */ inputEntry.getKey(), type));
                inputValues.put(key, new LinkedList<Input>());
            } else if (clazz == DataManagementFileReference.class) {
                dmHandlesIn.add(key);
                inputValues.put(key, new LinkedList<Input>());
            } else if (clazz == VariantArray.class) {
                inputArrays.add(key);
                inputValues.put(key, new LinkedList<Input>());
            } else {
                LOGGER.error("Input channel definition has unknown type: Falling back to String");
                inputVariables.add(new BoundVariable(/* name */ inputEntry.getKey(), VariableType.String));
            }
        }

        // define the output variables for the script
        outputVariables = new ArrayList<BoundVariable>(compInfo.getOutputDefinitions().size());
        dmHandlesOut = new ArrayList<String>(); // names of channels that expect file references
        outputArrays = new ArrayList<String>(); // names of channels that expect arrays
        for (final Entry<String, ? extends Serializable> outputEntry: compInfo.getOutputDefinitions().entrySet()) {
            final String key = outputEntry.getKey();
            final Serializable clazz = outputEntry.getValue();
            final VariableType type = convertToVariable.get(clazz);
            if (type != null) {
                outputVariables.add(new BoundVariable(key, type));
            } else if (clazz == DataManagementFileReference.class) {
                dmHandlesOut.add(key);
            } else if (clazz == VariantArray.class) {
                outputArrays.add(key);
            } else {
                LOGGER.error("Output channel definition has unknown type: Falling back to String");
                outputVariables.add(new BoundVariable(key, VariableType.String));
            }
        }
        
        notificationService.setBufferSize(compInfo.getIdentifier() + ConsoleRow.NOTIFICATION_SUFFIX, THOUSAND);
        checkPythonVersion();
        LOGGER.debug("Python component prepared: " + compInfo.getName());
       
    }
    private void checkPythonVersion() throws ComponentException {
        try {
            // Copy test script to temp folder
            File tempDir = TempFileUtils.getDefaultInstance().createManagedTempDir();
            File pythonVersionScript = new File(tempDir.getAbsolutePath() + File.separator + "version.py");
            FileUtils.copyInputStreamToFile(PythonComponent.class.getResourceAsStream("/resources/version.py"), pythonVersionScript);
            // Start script with chosen python installation
            LocalApacheCommandLineExecutor executor = new LocalApacheCommandLineExecutor(new File(pythonVersionScript.getParent()));
            String command = (String) compInfo.getConfigurationValue(PythonComponentConstants.PYTHON_INSTALLATION) 
                + " version.py"; 
            executor.start(command);
            executor.waitForTermination();
            // Gather and parse output, which should be a version number: X.Y.Z
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream inputStream = executor.getStdout();
            int b;
            while ((b = inputStream.read()) != 0 - 1){
                baos.write(b);
            }
            String line = baos.toString();
            line = StringUtils.removeEndIgnoreCase(line, "\n");
            if (StringUtils.isNotBlank(line)) {
                // Check if X >= 3, so the chosen python version is >= 3.x.x
                int versionMainNumber = Integer.parseInt(line.substring(0, line.indexOf('.')));
                if (versionMainNumber >= 3){
                    String errorLine = "Python versions 3.x are not supported by RCE, your selected version is " + line;
                    LOGGER.error(errorLine);
                    throw new ComponentException(errorLine);
                }
            }
            TempFileUtils.getDefaultInstance().disposeManagedTempDirOrFile(tempDir);
        } catch (IOException e) {
            LOGGER.debug("Python version information", e);
        } catch (InterruptedException e) {
            LOGGER.debug("Python version information", e);
        }
    }

    @Override
    public boolean canRunAfterNewInput(Input newInput, Map<String, Deque<Input>> inputValuesIn) throws ComponentException {
        boolean canRun = super.canRunAfterNewInput(newInput, inputValuesIn);
        inputValues.get(newInput.getName()).add(newInput); // add input value to queue
        return canRun;
    }
    @Override
    public void onCancel() {
        if (notificationService != null && compInfo != null){
            notificationService.removePublisher(compInfo.getIdentifier() + ConsoleRow.NOTIFICATION_SUFFIX);
            LOGGER.debug("Python component canceled: " + compInfo.getName());
        }
    }

    @Override
    public void onFinish() {
        // nothing to do here yet
    }

    @Override
    public void onDispose() {
        notificationService.removePublisher(compInfo.getIdentifier() + ConsoleRow.NOTIFICATION_SUFFIX);
        LOGGER.debug("Python component disposed: " + compInfo.getName());
    }

    @Override
    public boolean runInitial(final boolean inputsConnected) {
        return run(true, inputsConnected, null, new HashMap<String, Deque<Input>>());
    }

    @Override
    public boolean runStep(final Input newInput, final Map<String, Deque<Input>> pendingInputValues) {
        return run(false, true, newInput, pendingInputValues);
    }

    // this method is an odd interface method. it was replaced by runInitial and runStep.
    // did it that way anyway because it is running code. if code will be reworked, it should be match the
    // new interface methods runInitial and runStep
    private boolean run(final boolean initialRun, final boolean inputsConnected, final Input newInput,
        Map<String, Deque<Input>> pendingInputValues) {
        // collect files for deletion after execution
        Set<File> tempInputFiles = new HashSet<File>();
        LOGGER.debug("Python component running: " + compInfo.getName());
        if (initialRun && (inputValues.size() > 0)) { // if we aren't a source: wait until regular run when all input slots are filled
            return true;
        }
        final SimpleComponentDataManagementService dmService = new SimpleComponentDataManagementService(compInfo);
        final PythonComponentHistoryObject pyrs = new PythonComponentHistoryObject(); // history object
        try {
            final Map<String, String> dmContextHandles = new Hashtable<String, String>(); // all paths of temporary files
            final List<VariantArray> arraysIn = new ArrayList<VariantArray>();
            if (!initialRun) { // only calculate inputs if we aren't a source component
                // get all input variable values for script context
                for (final BoundVariable variable: inputVariables) {
                    if (!inputValues.get(variable.getName()).isEmpty()) {
                        final Input input = inputValues.get(variable.getName()).poll(); // check if value is already there
                        variable.setValue(input.getValue()); // throws IllegalArgumentException when wrong data type is found
                        pendingInputValues.get(variable.getName()).removeFirst(); // remove used input from pending input 
                        // queues because of new scheduler logic
                    }
                }
                // get all input data management handles for script context
                final List<DataManagementFileReference> historyDmHandlesIn = new ArrayList<DataManagementFileReference>();
                pyrs.setInputDmHandles(historyDmHandlesIn);
                for (final String name: dmHandlesIn) { // get text value of file reference
                    // check if input value is available, because input could be declared as not required
                    if (!inputValues.get(name).isEmpty()) {
                        try {
                            final File tempFile = TempFileUtils.getDefaultInstance().createTempFileFromPattern("python-component-dm-*");
                            dmService.copyReferenceToLocalFile(((DataManagementFileReference) inputValues
                                .get(name).peek().getValue()).getReference(), tempFile, compInfo.getPlatformsInvolvedInComponentContext());
                            tempInputFiles.add(tempFile);
                            dmContextHandles.put(name, tempFile.getAbsolutePath());
                            historyDmHandlesIn.add(((DataManagementFileReference) inputValues.get(name).poll().getValue()));
                            pendingInputValues.get(name).removeFirst();  // remove used input from pending input 
                            //queues because of new scheduler logic

                        } catch (final IOException e) {
                            LOGGER.error("Could not create temp file for data management input");
                            throw new IllegalStateException("Could not create temp file for data management input", e);
                        }
                    } else if (!compInfo.getInputMetaData(name).get(ComponentConstants.METADATAKEY_INPUT_USAGE) 
                        .equals(ComponentConstants.INPUT_USAGE_TYPE_OPTIONAL)) { // if it is not available but not optional throw exception
                        throw new ComponentException("input must be provided: " + name);
                    }
                }
                // get all input arrays for script context
                for (final String name: inputArrays) {
                    if (!inputValues.get(name).isEmpty()) {
                        final VariantArray tmp = new VariantArray(name, (VariantArray) inputValues.get(name).poll().getValue());
                        arraysIn.add(tmp);
                        pendingInputValues.get(name).removeFirst();
                    } else if (!compInfo.getInputMetaData(name).get(ComponentConstants.METADATAKEY_INPUT_USAGE) 
                        .equals(ComponentConstants.INPUT_USAGE_TYPE_OPTIONAL)) { // if it is not available but not optional throw exception
                        throw new ComponentException("input must be provided: " + name);
                    }
                }
            }
            ScriptUsage jobScriptUsage = ScriptUsage.valueOf(ComponentInstanceInformationUtils
                .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_USAGEOFSCRIPT, String.class, instInformation));
            String script = null;
            switch (jobScriptUsage) {
            case LOCAL:
                script = ComponentInstanceInformationUtils
                .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_LOCALSCRIPT, String.class, instInformation);
                break;

            case NEW: 
                script = (String) compInfo.getConfigurationValue(PythonComponentConstants.SCRIPT);
                if (script == null) {
                    throw new RuntimeException("python script not configured");
                }
            default:
                // should not happen because of enum
                break;
            }
            final PythonExecutionContext executionContext = new PythonExecutionContext(script);
            executionContext.setInputVariables(inputVariables);
            executionContext.setOutputVariables(outputVariables);
            executionContext.setInputArrays(arraysIn);
            executionContext.setOutputArrays(outputArrays);
            executionContext.setDataManagementHandles(dmContextHandles);
            if (compInfo.getConfigurationValue(PythonExecutor.DEBUG) != null) {
                try {
                    executionContext.put(PythonExecutor.DEBUG, compInfo.getConfigurationValue(PythonExecutor.DEBUG));
                } catch (final ClassCastException e) { // TODO compatibility with old workflow files. Remove e.g. end of 2011)
                    executionContext.put(PythonExecutor.DEBUG,
                        Boolean.valueOf((String) compInfo.getConfigurationValue(PythonExecutor.DEBUG)));
                }
            }
            String pythonInstallation = (String) compInfo.getConfigurationValue(PythonComponentConstants.PYTHON_INSTALLATION);
            if (pythonInstallation == null) {
                throw new ComponentException("Python installation path not configured");
            }
            executionContext.setPythonExecutablePath(pythonInstallation);
            try {
                final PythonExecutor executor = executorFactory.createExecutor(executionContext);
                final Future<PythonExecutionResult> futureExecResult = executor.execute();
                threadPool.submit(new ConsoleOutputPublisher(compInfo, executor.getStdoutStream(),
                    ConsoleRow.Type.STDOUT, notificationService));
                threadPool.submit(new ConsoleOutputPublisher(compInfo, executor.getStderrStream(),
                    ConsoleRow.Type.STDERR, notificationService));
                final PythonExecutionResult execResult = futureExecResult.get(); // block until script has finished
                if (execResult == null) {
                    LOGGER.error("No return from Python executor service (check previous error messages)");
                    pyrs.setError(new IllegalStateException("No return from Python executor service (check previous error messages)"));
                    return true;
                }
                pyrs.setExitCode(execResult.getExitCode());
                if (execResult.getExitCode() != 0){
                    throw new ComponentException("Python script threw exception.");
                }
                setOutputValues(pyrs, execResult, dmService);
            } catch (final ExecutorException e) {
                pyrs.setError(e);
                LOGGER.error(e);
                throw new IllegalStateException("Error creating the script executor", e);
            }
        } catch (final InterruptedException e) {
            pyrs.setError(e);
            throw new IllegalStateException(e);
        } catch (final RuntimeException e) {
            pyrs.setError(e);
            throw new IllegalStateException(e);
        } catch (final ComponentException e) {
            pyrs.setError(e);
            throw new IllegalStateException(e);
        }  catch (ExecutionException e) {
            pyrs.setError(e);
            throw new IllegalStateException(e);
        } catch (final OutOfMemoryError e) {
            pyrs.setError(e);
            throw new IllegalStateException(e);
        } finally {
            try {
                dmService.addHistoryDataPoint(pyrs, compInfo.getName());
                // release temp files used for input files. explicitly done, because they can be
                // large and Python component can be executed many times per workflow run
                for (File tempFile : tempInputFiles) {
                    TempFileUtils.getDefaultInstance().disposeManagedTempDirOrFile(tempFile);
                }
            } catch (final IOException ee) {
                LOGGER.error(ee);
            }
        }
        LOGGER.debug("Python component run finished: " + compInfo.getName());
        return true;
    }

    private void setOutputValues(PythonComponentHistoryObject pyrs, PythonExecutionResult execResult,
        SimpleComponentDataManagementService dmService) {
        // Set output values
        final Map<String, Class<? extends Serializable>> outputDefs = compInfo.getOutputDefinitions(); // all defs
        final Map<String, String> dmReturnedHandles = execResult.getDataManagementHandles();
        final Map<String, VariantArray> arraysOut = execResult.getOutputArrays();
        final List<DataManagementFileReference> historyDmHandlesOut = new ArrayList<DataManagementFileReference>();
        pyrs.setOutputDmHandles(historyDmHandlesOut);
        for (final Entry<String, Class<? extends Serializable>> outputDef: outputDefs.entrySet()) { // for all endpoints
            final String key = outputDef.getKey();
            // data management (file) reference
            if (dmHandlesOut.contains(key) // we know that it's an reference channel
                && dmReturnedHandles.containsKey(key)) { // if it was actually set from within the script
                try {
                    File file = new File(dmReturnedHandles.get(key));
                    final String uuid = dmService.createTaggedReferenceFromLocalFile(file, key);
                    final DataManagementFileReference dm = new DataManagementFileReference(uuid, file.getName());
                    compInfo.getOutput(key).write(dm); // set the channel value
                    historyDmHandlesOut.add(dm);
                } catch (final IOException e) {
                    LOGGER.error("Could not create data management entry from script return file");
                    throw new IllegalStateException("Could not create data management entry from script return file", e);
                }
                // array
            } else if (outputArrays.contains(key) && arraysOut.containsKey(key)) {
                compInfo.getOutput(key).write(arraysOut.get(key));
                // normal variable
            } else {
                for (final BoundVariable variable: execResult.getOutputVariables()) {
                    if (variable.getName().equals(key)) { // only write out value, if anything returned from script
                        // write real type (double, string, ...)
                        if (variable.getValue() == null) {
                            compInfo.getOutput(variable.getName())
                                .write(ComponentState.FINISHED.name());                                    
                        } else {
                            compInfo.getOutput(variable.getName()).write(variable.getValue());                                    
                        }
                        break;
                    }
                }
            }
        }
    }

    protected void bindPersistentSettingsService(PersistentSettingsService newPersistentSettingsService) {
        persistentSettingsService = newPersistentSettingsService;
    }
    protected void unbindPersistentSettingsService(PersistentSettingsService newPersistentSettingsService) {

    }
}
