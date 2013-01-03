/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utities for creating automatically cleaned-up temporary directories and files. <br/>
 * The major design goals were:
 * <ul>
 * <li>Provide central cleanup of temp files, to avoid temp files getting left behind after crashes
 * or components/tools that do not implement proper cleanup</li>
 * <li>Prevent collisions between multiple RCE instances on acquisition or cleanup</li>
 * <li>Prevent accidential deletes outside of the created temp folders, as far as possible</li>
 * <li>Heap usage and the number of file locks should not increase with the number of acquired temp
 * files and directories, to prevent resource drain in long-running instances</li>
 * <li>(to be continued: specific-filename temp files etc.)</li>
 * </ul>
 * <br/>
 * Basic approach:
 * <ul>
 * <li>TODO</li>
 * </ul>
 * 
 * @author Robert Mischke
 */
public class TempFileUtils {

    /**
     * The placeholder that marks the place of the "random" part in filename patterns.
     */
    public static final String FILENAME_PATTERN_PLACEHOLDER = "*";

    private static final String LOCK_FILE_NAME = "tmpdir.lock";

    private static final String DEFAULT_ROOT_DIR_PATH = "rce-temp/default";

    private static final String DEFAULT_TEST_ROOT_DIR_PATH = "rce-unittest/default";

    private static TempFileUtils defaultInstance;

    // TODO implement explicit cleanup

    private File globalRootDir;

    private File instanceRootDir;

    /**
     * The current directory where {@link #createTempFileWithFixedFilename(String)} tries to create
     * files; replaced with a new directory on a filename collision.
     */
    private File currentDirectoryForFixedFilenameFiles;

    /**
     * The lock file to mark a temporary directory as "in use". Note that Java {@link FileLock}s are
     * NOT necessarily "hard" OS file locks; they should be treated as "advisory" (see
     * {@link FileLock} documentation).
     */
    private FileLock instanceRootDirLock;

    private Log log = LogFactory.getLog(TempFileUtils.class);

    /**
     * Get the default global instance of this class.
     * 
     * @return the default instance
     */
    public static synchronized TempFileUtils getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new TempFileUtils();
        }
        return defaultInstance;
    }

    /**
     * Sets a new directory to use as the root of all managed temp files and folders. All previously
     * generated temp files and folders will be released for cleanup (deletion), so this method
     * should usually be called only once, typically on application startup.
     * 
     * If this method was not called before one of the utility methods is used, a default root is
     * chosen below the "java.io.tmpdir" path. This is undesirable from a cleanup standpoint, so a
     * warning is logged, but this avoids the hassle of defining temp file roots in affected unit
     * tests.
     * 
     * @param newRootDir the new root directory; may already exist
     * @throws IOException when the directory could not be created
     */
    public synchronized void setGlobalRootDir(File newRootDir) throws IOException {
        // if set, release the old lock to allow cleanup
        if (instanceRootDirLock != null) {
            // check if the same directory is already set (for example by a previous unit test)
            if (globalRootDir.getAbsolutePath().equals(newRootDir.getAbsolutePath())) {
                if (log.isTraceEnabled()) {
                    log.trace("New temp root directory is the same as the existing one; ignoring change request ("
                        + newRootDir.getAbsolutePath() + ")");
                }
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Releasing lock file in directory " + instanceRootDir.getAbsolutePath());
            }
            instanceRootDirLock.release();
        }
        instanceRootDir = null;
        globalRootDir = newRootDir;
    }

    /**
     * Convenience method for unit tests. Sets a hard-coded default root directory (inside the
     * system temporary directory) as the new "global root" via {@link #setGlobalRootDir(File)}.
     * 
     * @throws IOException see {@link #setGlobalRootDir(File)}
     */
    public synchronized void setDefaultTestRootDir() throws IOException {
        setGlobalRootDir(new File(System.getProperty("java.io.tmpdir"), DEFAULT_TEST_ROOT_DIR_PATH));
    }

    /**
     * Creates a new "managed" temporary directory with an information text. See
     * {@link #createManagedTempDir(String)} for details.
     * 
     * @return a new, empty directory that is guaranteed to exist
     * 
     * @throws IOException if creating the new directory fails
     */
    public File createManagedTempDir() throws IOException {
        return createManagedTempDir(null);
    }

    /**
     * Creates a new "managed" temporary directory with an optional information text that is added
     * to the directories filename. The latter is only for identifying temporary directories during
     * debugging; it has no effect at runtime (except for the fact that illegal characters will
     * break creation of the directory).
     * 
     * @param infoText the optional information text; ignored if null or empty
     * @return a new, empty directory that is guaranteed to exist
     * 
     * @throws IOException if creating the new directory fails
     */
    public File createManagedTempDir(String infoText) throws IOException {
        // generate filename
        String tempDirName = UUID.randomUUID().toString();
        if (infoText != null && infoText.length() != 0) {
            tempDirName = tempDirName + "-" + infoText;
        }
        // create dir and check
        File tempDir = new File(getInstanceRootDir(), tempDirName);
        if (!tempDir.mkdirs()) {
            // throw specific exceptions to track down a case where mkdirs() actually failed (Mantis
            // #6425)
            if (tempDir.isDirectory()) {
                throw new IOException("Unexpected collision: New UUID-named temporary directory does already exist: " + tempDir);
            } else if (tempDir.isFile()) {
                throw new IOException("Unexpected collision: New UUID-named temporary directory is blocked by a equally-named file: "
                    + tempDir);
            } else {
                throw new IOException("Failed to create new managed temporary directory "
                    + "(maybe lack of permissions, or the target drive is full?): " + tempDir);
            }
        }
        return tempDir;
    }

    /**
     * Creates a new temporary, managed file from the given filename pattern.
     * 
     * @param filenamePattern the pattern for the name of the temporary file; must contain the
     *        character "*", which is replaced by a generated string; relative paths are not
     *        permitted at this time
     * @return a new {@link File} pointing to a newly created, empty file
     * @throws IOException if creating the file or a containing directory fails
     */
    public File createTempFileFromPattern(String filenamePattern) throws IOException {
        // validate pattern
        if (filenamePattern == null || filenamePattern.length() == 0) {
            throw new IllegalArgumentException("Filename pattern must not be empty");
        }
        if (!filenamePattern.contains(FILENAME_PATTERN_PLACEHOLDER)) {
            throw new IllegalArgumentException("Filename pattern must contain the placeholder pattern " + FILENAME_PATTERN_PLACEHOLDER);
        }
        // generate filename
        String tempPart = UUID.randomUUID().toString();
        String filename = filenamePattern.replace(FILENAME_PATTERN_PLACEHOLDER, tempPart);
        File newFile = new File(getInstanceRootDir(), filename);
        // as long as UUIDs are truly unique (as they should), this should never cause a collision
        if (!newFile.createNewFile()) {
            throw new IOException("Failed to create temporary file " + newFile.getAbsolutePath());
        }
        return newFile;
    }

    /**
     * Creates a new file in a managed temporary directory with the given filename, ie the last
     * segment of the generated path will be the passed string.
     * 
     * @param filename the filename for the new temporary file; relative paths are not permitted
     * @return a new {@link File} pointing to a newly created, empty file
     * @throws IOException if creating the file or a containing directory fails
     */
    public synchronized File createTempFileWithFixedFilename(String filename) throws IOException {
        // catch some basic errors
        if (filename.contains("\\") || filename.contains("/")) {
            throw new IOException("Relative filenames are not allowed in this call");
        }
        // create a managed directory if not done yet
        if (currentDirectoryForFixedFilenameFiles == null) {
            currentDirectoryForFixedFilenameFiles = createManagedTempDir();
        }
        // try to generate new file
        File newFile = new File(currentDirectoryForFixedFilenameFiles, filename);
        if (!newFile.createNewFile()) {
            // on a filename collision, create a new temp directory...
            currentDirectoryForFixedFilenameFiles = createManagedTempDir();
            // ...and retry
            newFile = new File(currentDirectoryForFixedFilenameFiles, filename);
            // if creating the temp file fails again, something else is wrong; abort
            if (!newFile.createNewFile()) {
                throw new IOException("Failed to create unique temporary file " + newFile);
            }
        }
        return newFile;
    }

    /**
     * Creates a new temporary file with a randomly-generated filename, and copies the content of
     * the given {@link InputStream} into it. The stream is closed after copying its contents.
     * 
     * @param is the {@link InputStream} to read from
     * @return the {@link File} object representing the genrated file
     * @throws IOException if temp file creation or stream reading failed
     */
    public File writeInputStreamToTempFile(InputStream is) throws IOException {
        File file = createTempFileFromPattern("stream-to-file-" + FILENAME_PATTERN_PLACEHOLDER);
        FileUtils.copyInputStreamToFile(is, file);
        IOUtils.closeQuietly(is);
        return file;
    }

    // TODO additional file-related methods?

    /**
     * An optional method that can be used to release disk space used by temporary files as soon as
     * they are no longer needed, instead of leaving them for automatic cleanup. Whenever files of a
     * significant size are written and the end-of-use time can be determined, calling this method
     * is strongly recommended.
     * 
     * The main benefit over simply using commons-io FileUtils#deleteDirectory(File) is that this
     * method is safer: It can check if the provided {@link File} is indeed inside the managed
     * temporary directory, therefore avoiding accidential mixups leading to wrong data being
     * deleted.
     * 
     * Even after a successful call, no guarantee is made if the given file or directory was
     * immediately removed. (For example, deleting these files may have been delegated to a
     * background task.)
     * 
     * @param tempFileOrDir a {@link File} pointing to the directory or file to be deleted
     * 
     * @throws IOException on consistency errors, or when the delete operation failed
     */
    public void disposeManagedTempDirOrFile(File tempFileOrDir) throws IOException {
        if (instanceRootDir == null) {
            throw new IOException("disposeManagedTempDirOrFile() was called with no instanceRootDir set");
        }
        String givenPath = tempFileOrDir.getAbsolutePath();
        String rootPath = instanceRootDir.getAbsolutePath();
        if (!givenPath.startsWith(rootPath)) {
            throw new IOException(String
                .format("Temporary file or directory '%s' does not match "
                    + "the root temp directory '%s' -- ignoring delete request", givenPath, rootPath));
        }

        try {
            if (tempFileOrDir.isDirectory()) {
                FileUtils.deleteDirectory(tempFileOrDir);
            } else {
                // TODO react if return value is false?
                tempFileOrDir.delete();
            }
        } catch (IOException e) {
            throw new IOException("Error deleting temporary file or directory " + givenPath, e);
        }
    }

    private File getInstanceRootDir() throws IOException {
        // lazy init
        if (instanceRootDir == null) {
            instanceRootDir = initializeInstanceRootDir();
            // create an OS-level file lock
            File lockFile = new File(instanceRootDir, LOCK_FILE_NAME);
            instanceRootDirLock = new RandomAccessFile(lockFile, "rw").getChannel().tryLock();
            // should never happen, but catch it anyway
            if (instanceRootDirLock == null) {
                throw new IOException("Failed to acquire lock in new temporary directory: " + lockFile.getAbsolutePath());
            }
            if (log.isDebugEnabled()) {
                log.debug("Initialized top-level managed temp directory; acquired lock " + lockFile.getAbsolutePath());
            }
        }
        return instanceRootDir;
    }

    private File initializeInstanceRootDir() throws IOException {
        if (globalRootDir == null) {
            File defaultRootDir = new File(System.getProperty("java.io.tmpdir"), DEFAULT_ROOT_DIR_PATH);
            log.debug("No global root for managed temp files was defined, setting default: " + defaultRootDir.getAbsolutePath());
            setGlobalRootDir(defaultRootDir);
        }
        File newInstanceRootDir = new File(globalRootDir, UUID.randomUUID().toString());
        if (!newInstanceRootDir.mkdirs()) {
            throw new IOException("New managed temporary directory could not be created or did already exist: " + newInstanceRootDir);
        }
        return newInstanceRootDir;
    }

}
