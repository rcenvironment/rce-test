/*
 * Copyright (C) 2006-2010 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.rce.components.simplewrapper;

import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.INIT;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.POST;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.PRE;
import static de.rcenvironment.commons.scripting.ScriptableComponentConstants.RUN;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.rcenvironment.commons.FileSupport;
import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.commons.channel.DataManagementFileReference;
import de.rcenvironment.commons.executor.CommandLineExecutor;
import de.rcenvironment.commons.scripting.ScriptableComponentConstants.ComponentRunMode;
import de.rcenvironment.commons.textstream.TextStreamWatcher;
import de.rcenvironment.commons.validation.ValidationFailureException;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ComponentInstanceInformationUtils;
import de.rcenvironment.rce.component.ConsoleRow;
import de.rcenvironment.rce.component.ConsoleRow.Type;
import de.rcenvironment.rce.component.datamanagement.stateful.SimpleComponentDataManagementService;
import de.rcenvironment.rce.component.datamanagement.stateful.StatefulComponentDataManagementService;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.Output;
import de.rcenvironment.rce.component.scripting.AbstractScriptingComponent;
import de.rcenvironment.rce.component.wrapper.MonitoringEventListener;
import de.rcenvironment.rce.component.wrapper.impl.DefaultWrapperConfigurationFactory;
import de.rcenvironment.rce.component.wrapper.impl.LocalExecutionEnvironment;
import de.rcenvironment.rce.component.wrapper.sandboxed.ExecutionEnvironment;
import de.rcenvironment.rce.component.wrapper.sandboxed.SandboxedWrapperBase;
import de.rcenvironment.rce.component.wrapper.sandboxed.WrapperConfigurationFactory;
import de.rcenvironment.rce.components.simplewrapper.commons.FileMappings;
import de.rcenvironment.rce.components.simplewrapper.commons.ConfigurationValueConverter;
import de.rcenvironment.rce.components.simplewrapper.commons.SimpleWrapperComponentConstants;
import de.rcenvironment.rce.notification.DistributedNotificationService;


/**
 * {@link Component} wrapping executables in the simplest possible way - executing it via system calls.
 * 
 * @author Christian Weiss
 */
public class SimpleWrapperComponent extends AbstractScriptingComponent {

    private static final String FILE_SEPERATOR = "/";

    private static final Set<String> SYSTEM_ENVIRONMENT = System.getenv().keySet();

    private static DistributedNotificationService notificationService = null;

    private FileMappings fileMappings;

    private File executableDirectory;

    private SimpleComponentDataManagementService dataManagementService;

    private SimpleCommandWrapper wrapper;

    private Map<String, String> outputFileMappingsCache;

    private Map<String, String> inputFileMappingsCache;

    public SimpleWrapperComponent() {
        super(TriggerMode.Manual);
    }

    protected void bindDistributedNotificationService(DistributedNotificationService newNotificationService) {
        notificationService = newNotificationService;
    }

    protected void unbindDistributedNotificationService(DistributedNotificationService oldNotificationService) {
    }

    @Override
    public final void onPrepare(ComponentInstanceInformation incInstInformation) throws ComponentException {
        super.onPrepare(incInstInformation);
        final String fileMappingsString = ComponentInstanceInformationUtils.getConfigurationValue(
            SimpleWrapperComponentConstants.PROPERTY_FILE_MAPPING, String.class, instInformation);
        fileMappings = ConfigurationValueConverter.getConfiguredMappings(fileMappingsString);
        deployExecutableDirectory();
        dataManagementService = new SimpleComponentDataManagementService(instInformation);
        setupStaticEnvironment();

        final int bufferSize = 1000;
        final String componentId = instInformation.getComponentIdentifier();
        notificationService.setBufferSize(componentId + ConsoleRow.NOTIFICATION_SUFFIX, bufferSize);

        logger.debug("Simple wrapper component prepared");
    }

    @Override
    protected Writer getScriptWriter() {
        return new PrePostProcessingInfoLogWriter();
    }

    @Override
    protected Writer getScriptErrorWriter() {
        return new PrePostProcessingErrorLogWriter();
    }

    private void setupStaticEnvironment() {
        final WrapperConfigurationFactory configurationFactory = new FolderSynchronizingWrapperConfigurationFactory(executableDirectory);
        wrapper = new SimpleCommandWrapper(configurationFactory,
            new SimpleComponentDataManagementService(instInformation), new SimpleWrapperMonitoringEventListener());
        try {
            wrapper.setupStaticEnvironment();
        } catch (IOException e) {
            logger.error("Failed to setup command environment:", e);
            throw new RuntimeException(e);
        } catch (ValidationFailureException e) {
            logger.error("Failed to setup command environment:", e);
            throw new RuntimeException(e);
        }
    }

    private void tearDownStaticEnvironment() {
        try {
            // delete temp folder for loaded executable folder. explicitly done, because it can be
            // large and workflow with Simple Wrapper component can be executed many times anew
            if (executableDirectory != null) {
                TempFileUtils.getDefaultInstance().disposeManagedTempDirOrFile(executableDirectory);
            }
            if (wrapper != null){
                wrapper.tearDownStaticEnvironment();
            }
        } catch (IOException e) {
            logger.error("Failed to tear down command environment:", e);
            throw new RuntimeException(e);
        }
    }

    private void deployExecutableDirectory() {
        try {
            File tempDirectory = TempFileUtils.getDefaultInstance().createManagedTempDir();
            executableDirectory = new File(tempDirectory, "__executable__");
            executableDirectory.mkdir();
            final String contentString = ComponentInstanceInformationUtils.getConfigurationValue(
                SimpleWrapperComponentConstants.PROPERTY_EXECUTABLE_DIRECTORY_CONTENT, String.class, "", instInformation);
            if (!contentString.isEmpty()) {
                final byte[] content = ConfigurationValueConverter.executableDirectoryContent(contentString);
                FileSupport.unzip(content, executableDirectory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to deploy tool directory:", e);
        }
    }

    private boolean isInitCommandEnabled() {
        final boolean result = ComponentInstanceInformationUtils
            .getConfigurationValue(SimpleWrapperComponentConstants.PROPERTY_DO_INIT_COMMAND, Boolean.class, instInformation);
        return result;
    }

    @Override
    protected boolean runInitialInScriptingComponent(boolean inputsConnected) throws ComponentException {
        boolean result = true;
        if (isInitCommandEnabled()) {
            final String command = ComponentInstanceInformationUtils
                .getConfigurationValue(SimpleWrapperComponentConstants.PROPERTY_INIT_COMMAND, String.class, instInformation);
            try {
                result = executeCommand(command);
            } catch (IOException e) {
                final String message = "Failed to run initial component step:";
                logger.error(message, e);
                throw new ComponentException(message, e);
            }
        }
        return result;
    }

    @Override
    protected boolean runStepInScriptingComponent(final Map<String, Deque<Input>> inputValues) throws ComponentException {
        final String command = ComponentInstanceInformationUtils.getConfigurationValue(
            SimpleWrapperComponentConstants.PROPERTY_RUN_COMMAND, String.class, instInformation);
        try {
            File folder = TempFileUtils.getDefaultInstance().createManagedTempDir();
            deployInputFiles(inputValues, folder);
            final boolean result = executeCommand(command, folder, inputValues);
            return result;
        } catch (IOException e) {
            final String message = "Failed to run component step:";
            logger.error(message, e);
            throw new ComponentException(message, e);
        }
    }

    protected boolean executeCommand(final String command) throws IOException {
        return executeCommand(command, null, null);
    }

    protected boolean executeCommand(final String command, final File folder, final Map<String, Deque<Input>> inputValues)
        throws IOException {
        try {
            final boolean result = wrapper.execute(command, folder, inputValues);
            logger.debug("Executing command of 'Simple Wrapper'");
            return result;
        } catch (ValidationFailureException e) {
            logger.error("Failed to start command:", e);
            throw new RuntimeException(e);
        }
    }

    private void deployInputFiles(final Map<String, Deque<Input>> inputValues, final File folder) throws IOException {
        final Map<String, String> inputFileMappings = getInputFileMapping();
        for (final Map.Entry<String, String> entry : inputFileMappings.entrySet()) {
            final String name = entry.getKey();
            final String path = entry.getValue();
            final File file = new File(folder, path);
            final Input input = inputValues.get(name).removeFirst();
            final DataManagementFileReference fileReference = (DataManagementFileReference) input.getValue();
            try {
                if (fileReference == null) {
                    throw new IOException("input file reference is null ");
                }
                dataManagementService.copyReferenceToLocalFile(fileReference.toString(), file);
            } catch (IOException e) {
                final String message = String.format("Failed to deploy input file '%s':", path);
                logger.error(message, e);
                throw new IOException(message, e);
            }
        }
    }

    private Map<String, String> getInputFileMapping() {
        if (inputFileMappingsCache == null) {
            final Map<String, String> result = new HashMap<String, String>();
            final String direction = "Input";
            Map<String, Class<? extends DataManagementFileReference>> inputs = ComponentInstanceInformationUtils
                .getInputs(DataManagementFileReference.class, instInformation);
            for (final String inputName : inputs.keySet()) {
                final String path;
                if (fileMappings.contains(direction, inputName)) {
                    path = fileMappings.getPath(direction, inputName);
                } else {
                    path = String.format("__input__%s", inputName);
                }
                result.put(inputName, path);
            }
            inputFileMappingsCache = result;
        }
        return inputFileMappingsCache;
    }

    private void distributeOutputFiles(final File folder) {
        final Map<String, String> outputFileMappings = getOutputFileMappings();
        for (final Map.Entry<String, String> entry : outputFileMappings.entrySet()) {
            final String name = entry.getKey();
            final String path = entry.getValue();
            final File file = new File(folder, path);
            try {
                final DataManagementFileReference fileReference;
                if (file.exists()) {
                    final String fileReferenceUUID = dataManagementService.createTaggedReferenceFromLocalFile(file, file.getName());
                    fileReference = new DataManagementFileReference(fileReferenceUUID, file.getName());
                } else {
                    throw new RuntimeException("Output file doesn't exist in execution folder: " + file.getAbsolutePath());
                }
                final Output output = instInformation.getOutput(name);
                output.write(fileReference);
            } catch (RuntimeException e) {
                final String message = String.format("Failed to distribute output file '%s':", path);
                logger.error(message, e);
                throw new RuntimeException(message, e);
            } catch (IOException e) {
                final String message = String.format("Failed to distribute output file '%s':", path);
                logger.error(message, e);
                throw new RuntimeException(message, e);
            }
        }
    }

    private Map<String, String> getOutputFileMappings() {
        if (outputFileMappingsCache == null) {
            final Map<String, String> result = new HashMap<String, String>();
            final String direction = "Output";
            Map<String, Class<? extends DataManagementFileReference>> outputs = ComponentInstanceInformationUtils
                .getOutputs(DataManagementFileReference.class, instInformation);
            for (final String name : outputs.keySet()) {
                final String path;
                if (fileMappings.contains(direction, name)) {
                    path = fileMappings.getPath(direction, name);
                } else {
                    path = String.format("__output__%s", name);
                }
                result.put(name, path);
            }
            outputFileMappingsCache = result;
        }
        return outputFileMappingsCache;
    }


    @Override
    public final void onCancel() {
        tearDownStaticEnvironment();
    }

    @Override
    public final void onFinish() {
        tearDownStaticEnvironment();
    }

    private static void uploadRecursively(final CommandLineExecutor executor, final File currentFolder) throws IOException {
        uploadRecursively(executor, currentFolder, "");
    }

    private static void uploadRecursively(final CommandLineExecutor executor, final File currentFolder, final String locationPrefix)
        throws IOException {
        for (final File subFolder : currentFolder.listFiles(FileFilters.getInstance(FileFilters.DIRECTORIES))) {
            uploadRecursively(executor, subFolder, locationPrefix + FILE_SEPERATOR + subFolder.getName());
        }
        for (final File file : currentFolder.listFiles(FileFilters.getInstance(FileFilters.FILES))) {
            executor.uploadToWorkdir(file, locationPrefix + file.getName());
        }
    }

    private void sendConsoleNotification(final Type notificationType, final String notificationMessage) {
        final String componentId = instInformation.getComponentIdentifier();
        final String componentName = instInformation.getName();
        final String notificationId = componentId + ConsoleRow.NOTIFICATION_SUFFIX;
        notificationService.send(notificationId,
            new ConsoleRow(instInformation.getComponentContextName(),
                componentName,
                notificationType,
                notificationMessage));
    }

    /**
     * Logs pre and post processing output of simple wrapper component to workflow console.
     * @author Doreen Seider
     */
    private class PrePostProcessingInfoLogWriter extends AbstractLogWriter {

        @Override
        protected void logLine(String line) {
            sendConsoleNotification(ConsoleRow.Type.STDOUT, line);
        }
    }

    /**
     * Logs pre and post processing error of simple wrapper component to workflow console.
     * @author Doreen Seider
     */
    private class PrePostProcessingErrorLogWriter extends AbstractLogWriter {

        @Override
        protected void logLine(String line) {
            sendConsoleNotification(ConsoleRow.Type.STDERR, line);
        }
    }

    /**
     * <code>SimpleWrapperConfigurationFactory</code> which uploads a folder recursively into the
     * new execution environment.
     * 
     * @author Christian Weiss
     */
    private static final class FolderSynchronizingWrapperConfigurationFactory extends DefaultWrapperConfigurationFactory {

        private final File folder;

        private FolderSynchronizingWrapperConfigurationFactory(final File folder) {
            assert folder != null;
            assert folder.isDirectory();
            this.folder = folder;
        }

        @Override
        public ExecutionEnvironment createExecutionEnvironment() {
            return new FolderSynchronizingLocalExecutionEnvironment();
        }

        /**
         * {@link LocalExecutionEnvironment} implementation that synchronizes the execution folder
         * with a local folder upon sandbox creation.
         * 
         * @author Christian Weiss
         */
        private final class FolderSynchronizingLocalExecutionEnvironment extends LocalExecutionEnvironment {

            @Override
            public CommandLineExecutor setupExecutorWithSandbox() throws IOException {
                final CommandLineExecutor executor = super.setupExecutorWithSandbox();
                uploadRecursively(executor, folder);
                return executor;
            }

        }

    }

    /**
     * Wrapper for simple command executions.
     * 
     * <p>
     * Note, that this wrapper triggers the execution of scripts in
     * {@link SimpleCommandWrapper#beforeExecution(String, CommandLineExecutor)} and
     * {@link SimpleCommandWrapper#afterExecution(String, CommandLineExecutor)}, if the trigger mode
     * is set to {@link TriggerMode#Manual}.
     * </p>
     * 
     * @author Christian Weiss
     */
    protected class SimpleCommandWrapper extends SandboxedWrapperBase<String, Boolean> {

        private File inputDataFolder;

        private Map<String, Deque<Input>> inputValues;

        public SimpleCommandWrapper(final WrapperConfigurationFactory configurationFactory,
            final StatefulComponentDataManagementService fileReferenceHandler, final MonitoringEventListener listener) {
            super(configurationFactory, fileReferenceHandler, listener);
        }

        public Boolean execute(final String command, final File folder, final Map<String, Deque<Input>> incInputValues)
            throws IOException, ValidationFailureException {
            this.inputDataFolder = folder;
            this.inputValues = incInputValues;
            return super.execute(command);
        }

        private ComponentRunMode getComponentRunMode() {
            final ComponentRunMode componentRunMode;
            if (inputValues == null) {
                componentRunMode = INIT;
            } else {
                componentRunMode = RUN;
            }
            return componentRunMode;
        }

        protected void beforeExecution(final String runConfiguration, final CommandLineExecutor executor) {
            if (inputDataFolder != null) {
                try {
                    uploadRecursively(executor, inputDataFolder);
                } catch (IOException e) {
                    final String message = "Failed to upload input data to execution environment:";
                    logger.error(message, e);
                    throw new RuntimeException(message, e);
                }
            }
            try {
                if (getTriggerMode() == TriggerMode.Manual) {
                    triggerScript(PRE, getComponentRunMode(), executor.getWorkDirPath(), inputValues);
                }
            } catch (final ComponentException e) {
                throw new RuntimeException(e);
            }
            transferSystemEnvironment(executor);
        }

        protected void afterExecution(final String runConfiguration, final CommandLineExecutor executor) {
            final File outputDataFolder;
            try {
                outputDataFolder = TempFileUtils.getDefaultInstance().createManagedTempDir();
            } catch (IOException e) {
                logger.fatal("Failed to create temporary folder to cache output files:", e);
                throw new RuntimeException(e);
            }
            try {
                if (getTriggerMode() == TriggerMode.Manual) {
                    triggerScript(POST, getComponentRunMode(), executor.getWorkDirPath(), inputValues);
                }
            } catch (final ComponentException e) {
                throw new RuntimeException(e);
            }
            final Map<String, String> outputFileMappings = getOutputFileMappings();
            for (final String filename : outputFileMappings.values()) {
                final File localFile = new File(outputDataFolder, filename);
                try {
                    // try to grab file from execution sandbox if generated during execution
                    executor.downloadFromWorkdir(filename, localFile);
                } catch (IOException e) {
                    logger.error(String.format("Failed to download output file '%s':", filename));
                }
            }
            distributeOutputFiles(outputDataFolder);
            
            // delete temp folder for cached input and output files. explicitly done, because they can be
            // large and Simple Wrapper component can be executed many times per workflow run
            if (inputDataFolder != null) {
                try {
                    TempFileUtils.getDefaultInstance().disposeManagedTempDirOrFile(inputDataFolder);
                } catch (IOException e) {
                    logger.error("Failed to dispose temporary folder to cached input files");
                }
            }
            try {
                TempFileUtils.getDefaultInstance().disposeManagedTempDirOrFile(outputDataFolder);
            } catch (IOException e) {
                logger.error("Failed to dispose temporary folder for cached output files");
            }
        }

        @Override
        protected Boolean executeInRunEnvironment(final String command, final CommandLineExecutor executor) {
            boolean result = false;
            try {
                beforeExecution(command, executor);
                // line separators: Windows: \r\n, Linux: \n, Mac: \r
                executor.startMultiLineCommand(command.split("\r?\n|\r"));

                InputStream stdout = executor.getStdout();
                InputStream stderr = executor.getStderr();

                // setup and start stream watchers
                final TextStreamWatcher stdoutWatcher = new TextStreamWatcher(stdout, stdoutMonitoringForwarder).start();
                final TextStreamWatcher stderrWatcher = new TextStreamWatcher(stderr, stderrMonitoringForwarder).start();

                final int returnValue = executor.waitForTermination();

                stdoutWatcher.waitForTermination();
                stderrWatcher.waitForTermination();
                stdout.close();
                stderr.close();

                afterExecution(command, executor);
                result = returnValue == 0;
            } catch (IOException e) {
                final String message = "Failed to start executor:";
                logger.error(message, e);
                throw new RuntimeException(message, e);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted during command execution:", e);
            } catch (Error e) {
                logger.error("Error:", e);
                throw new RuntimeException("Error during command execution:", e);
            }
            return result;
        }

        private void transferSystemEnvironment(final CommandLineExecutor executor) {
            for (final String key : SYSTEM_ENVIRONMENT) {
                final String value = System.getenv(key);
                executor.setEnv(key, value);
            }
        }

        @Override
        protected String getWorkDirPath() {
            return super.getWorkDirPath();
        }

    }

    /**
     * Implementation of {@link MonitoringEventListener}.
     *
     * @author Christian Weiss
     */
    protected class SimpleWrapperMonitoringEventListener implements MonitoringEventListener {

        @Override
        public void appendStdout(final String line) {
            sendConsoleNotification(ConsoleRow.Type.STDOUT, line);
        }

        @Override
        public void appendStderr(final String line) {
            sendConsoleNotification(ConsoleRow.Type.STDERR, line);
        }

        @Override
        public void appendUserInformation(final String line) {
            sendConsoleNotification(ConsoleRow.Type.META_INFO, line);
        }

    }

    /**
     * Factory for different {@link FileFiter}s.
     *
     * @author Christian Weiss
     */
    protected static final class FileFilters {

        protected static final int DIRECTORIES = 0x01;

        protected static final int FILES = 0x02;

        protected static final int READABLE = 0x04;

        protected static final int WRITEABLE = 0x08;

        protected static final int EXECUTABLE = 0x10;

        private FileFilters() {}

        protected static FileFilter getInstance(final int options) {
            return new FileFilter() {

                @Override
                public boolean accept(final File pathname) {
                    boolean accept = true;
                    if (accept && (options & DIRECTORIES) != 0) {
                        accept &= pathname.isDirectory();
                    }
                    if (accept && (options & FILES) != 0) {
                        accept &= pathname.isFile();
                    }
                    if (accept && (options & READABLE) != 0) {
                        accept &= pathname.canRead();
                    }
                    if (accept && (options & WRITEABLE) != 0) {
                        accept &= pathname.canWrite();
                    }
                    if (accept && (options & EXECUTABLE) != 0) {
                        accept &= pathname.canExecute();
                    }
                    return accept;
                }

            };
        }

    }

}
