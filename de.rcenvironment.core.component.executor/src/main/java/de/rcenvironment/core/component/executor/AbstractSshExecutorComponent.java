/*
 * Copyright (C) 2010-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.core.component.executor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.commons.executor.CommandLineExecutor;
import de.rcenvironment.commons.validation.ValidationFailureException;
import de.rcenvironment.core.utils.ssh.jsch.SshSessionConfiguration;
import de.rcenvironment.core.utils.ssh.jsch.SshSessionConfigurationFactory;
import de.rcenvironment.core.utils.ssh.jsch.executor.context.JSchExecutorContext;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ComponentInstanceInformationUtils;
import de.rcenvironment.rce.component.DefaultComponent;
import de.rcenvironment.rce.component.datamanagement.ComponentDataManagementService;

/**
 * Abstract component for remote execution via SSH.
 *
 * @author Doreen Seider
 */
public abstract class AbstractSshExecutorComponent extends DefaultComponent {

    private static final String CURRENT = ".";

    private static Log log = LogFactory.getLog(AbstractSshExecutorComponent.class);

    private static ComponentDataManagementService dataManagementService;
    
    private SshSessionConfiguration sshConfiguration;
    
    private JSchExecutorContext context;
    
    private CommandLineExecutor executor;
    
    private String scriptName = "";
    
    protected void bindDataManagementService(ComponentDataManagementService newDataManagementService) {
        dataManagementService = newDataManagementService;
    }

    protected void unbindDataManagementService(ComponentDataManagementService oldDataManagementService) {}

    protected abstract void execute(CommandLineExecutor commandLineExecutor, String scriptFileName)
        throws ComponentException;
    
    protected abstract void addHistoryObject(ComponentDataManagementService componentDataManagementService,
        File sandbox, String scriptFileName) throws ComponentException;

    @Override
    public void onPrepare(ComponentInstanceInformation compInstanceInformation) throws ComponentException {
        super.onPrepare(compInstanceInformation);
        
        String host = ComponentInstanceInformationUtils
                .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_HOST, String.class, instInformation);
        Integer port = ComponentInstanceInformationUtils
                .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_PORT, Integer.class, instInformation);
        String authUser = ComponentInstanceInformationUtils
                .getConfigurationValue("auth user", String.class, instInformation);
        String authPhrase = ComponentInstanceInformationUtils
                .getConfigurationValue("auth phrase", String.class, instInformation);
        String sandboRootWorkDir = ComponentInstanceInformationUtils
                .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_SANDBOXROOT, String.class, instInformation);
        
        sshConfiguration = SshSessionConfigurationFactory
                .createSshSessionConfigurationWithAuthPhrase(host, port, authUser, authPhrase);
        context = new JSchExecutorContext(sshConfiguration, sandboRootWorkDir);
        
        try {
            context.setUpSession();
            log.debug("Session established: " + authUser + "@" + host + ":" + port);
        } catch (IOException e) {
            throw new ComponentException("Establishing connection to remote host failed.", e);
        } catch (ValidationFailureException e) {
            throw new ComponentException("Validation of passed parameters failed", e);
        }

        try {
            executor = context.setUpSandboxedExecutor();
            log.debug("Remote sandbox created: " + authUser + "@" + host + ":" + port);
        } catch (IOException e) {
            throw new ComponentException("Setting up remote sandbox failed", e);
        }

    }
    
    @Override
    public boolean runInitial(boolean inputsConnected) throws ComponentException {
        
        uploadFiles();
        
        execute();
        
        downloadFiles();
        
        return inputsConnected;
    }

    @Override
    public void onCancel() {
        onFinish();
    }

    @Override
    public void onFinish() {
        try {
            context.tearDownSandbox(executor);
            context.tearDownSession();
            log.debug("Remote sandbox deleted");
        } catch (IOException e) {
            log.error("Deleting remote sandbox failed.", e);
        }
    }
    
    protected SshSessionConfiguration getSshSessionConfiguration() {
        return sshConfiguration;
    }
        
    private void uploadFiles() throws ComponentException {
        log.debug("Start uploading files...");
        boolean upload = ComponentInstanceInformationUtils
            .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_UPLOAD, Boolean.class, instInformation);
        if (upload) {
            
            String rawFilesToUpload = ComponentInstanceInformationUtils
                    .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_FILESTOUPLOAD, String.class, instInformation);
            List<File> files;
            try {
                FilesToUpload filesToUpload = FilesToUpload.valueAs(rawFilesToUpload);
                files = filesToUpload.retrieveFiles();    
            } catch (IOException e) {
                throw new ComponentException("Retrieving files to upload failed", e);
            }
            
            for (File file : files) {
                try {
                    executor.uploadToWorkdir(file, CURRENT);
                } catch (IOException e) {
                    throw new ComponentException("Uploading file to sandbox failed", e);
                }                
            }
        }
        log.debug("Uploading files finished");
    }

    private void execute() throws ComponentException {
        
        String uploadingErrorMessage = "Uploading script to sandbox failed";
        ScriptUsage jobScriptUsage = ScriptUsage.valueOf(ComponentInstanceInformationUtils
            .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_USAGEOFSCRIPT, String.class, instInformation));
        
        switch (jobScriptUsage) {
        case LOCAL:
            String localScript = ComponentInstanceInformationUtils
                .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_LOCALSCRIPT, String.class, instInformation);
            String localScriptFileName = ComponentInstanceInformationUtils
                .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_LOCALSCRIPTNAME, String.class, instInformation);
            
            try {
                File localFile = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(localScriptFileName);
                FileUtils.writeStringToFile(localFile, localScript);
                executor.uploadToWorkdir(localFile, CURRENT);
            } catch (IOException e) {
                throw new ComponentException(uploadingErrorMessage, e);
            }
            scriptName = localScriptFileName;
            break;
        case REMOTE:
            String remotePath = ComponentInstanceInformationUtils
                .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_REMOTEPATHOFSCRIPT, String.class, instInformation);
            try {
                executor.remoteCopy(remotePath, executor.getWorkDirPath());
            } catch (IOException e) {
                throw new ComponentException("Copying script to sandbox on remote host failed", e);
            }
            scriptName = new File(remotePath).getName();
            break;
        case NEW: 
            String fileName = ComponentInstanceInformationUtils
            .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_NAMEOFNEWJOBSCRIPT, String.class, instInformation);

            String fileContent = ComponentInstanceInformationUtils
                .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_SCRIPT, String.class, instInformation);
            try {
                File file = TempFileUtils.getDefaultInstance().createTempFileWithFixedFilename(fileName);
                FileUtils.writeStringToFile(file, fileContent);
                executor.uploadToWorkdir(file, CURRENT);
            } catch (IOException e) {
                throw new ComponentException(uploadingErrorMessage, e);
            }
            scriptName = fileName;
        default:
            // should not happen because of enum
            break;
        }
        
        execute(executor, scriptName);

    }
    
    private void downloadFiles() throws ComponentException {
        boolean download = ComponentInstanceInformationUtils
                .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_DOWNLOAD, Boolean.class, instInformation);
        boolean toRceDataManagement = ComponentInstanceInformationUtils
            .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_DOWNLOADTARGETISRCE, Boolean.class, instInformation);
        boolean toFileSystem = ComponentInstanceInformationUtils
            .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_DOWNLOADTARGETISFILESYSTEM, Boolean.class, instInformation);

        File directory;
        if (download) {
            if (toRceDataManagement) {
                try {
                    directory = TempFileUtils.getDefaultInstance().createManagedTempDir();
                    log.debug("Start downloading files...");
                    executor.downloadWorkdir(directory);
                    log.debug("Downloading files finished");
                } catch (IOException e) {
                    throw new ComponentException("Downloading remote work dir to temp directory failed", e);
                }
                
                addHistoryObject(dataManagementService, new File(directory, new File(executor.getWorkDirPath()).getName()), scriptName);
                
                if (toFileSystem) {
                    File targetDirectory = createDirectoryToDownloadTo();
                    try {
                        FileUtils.copyDirectory(directory, targetDirectory);
                    } catch (IOException e) {
                        throw new ComponentException("Copying remote work dir from temp directory to destination failed: "
                            + targetDirectory.getAbsolutePath(), e);
                    }
                }
                
            } else if (toFileSystem) {
                directory = createDirectoryToDownloadTo();
                try {
                    executor.downloadWorkdir(directory);
                } catch (IOException e) {
                    throw new ComponentException("Downloading remote work dir to local destination failed: "
                        + directory.getAbsolutePath(), e);
                }
            }      

        }
        
    }
    
    private File createDirectoryToDownloadTo() {
        String fileSystemPath = ComponentInstanceInformationUtils
            .getConfigurationValue(SshExecutorConstants.CONFIG_KEY_FILESYSTEMPATH, String.class, instInformation);
        File directory = new File(fileSystemPath);
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        return directory;
    }
    
}
