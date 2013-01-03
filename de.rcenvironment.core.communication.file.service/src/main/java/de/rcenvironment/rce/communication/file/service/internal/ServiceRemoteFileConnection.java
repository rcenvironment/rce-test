/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.file.service.internal;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.text.MessageFormat;

import org.osgi.framework.BundleContext;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.CommunicationService;
import de.rcenvironment.rce.communication.file.RCEFileURIUtils;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnection;

/**
 * This class provides access remote files via the communication bundle service call concept.
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class ServiceRemoteFileConnection implements RemoteFileConnection {

    private static final int MINUS_ONE = -1;

    private static final long serialVersionUID = -3315352695999821776L;

    private static final String ERROR_PARAMETERS_NULL = "The parameter \"{0}\" must not be null.";

    /**
     * The {@link FileService} of the remote instance where the file is located.
     */
    private final FileService fileService;

    /**
     * The remote UUID of the {@link InputStream}.
     */
    private final String remoteInputStreamUUID;

    /**
     * Creates a new {@link ServiceRemoteFileConnection} of a remote file and initialize it.
     * 
     * @param user The user's certificate.
     * @param uri URI pointing to remote file. (rce://node-id/dataReferenceUUID/revision)
     * @throws IOException if the file could not be accessed remotely.
     */
    public ServiceRemoteFileConnection(User user, URI uri, CommunicationService communicationService, BundleContext context)
        throws IOException {

        try {
            fileService = (FileService) communicationService.getService(FileService.class,
                RCEFileURIUtils.getPlatformIdentifier(uri), context);

            remoteInputStreamUUID = fileService.open(user, RCEFileURIUtils.getType(uri), RCEFileURIUtils.getPath(uri));

        } catch (UndeclaredThrowableException e) {
            throw new IOException(e.getUndeclaredThrowable());
        } catch (IllegalStateException e) {
            throw new IOException(e);
        } catch (CommunicationException e) {
            throw new IOException(e);
        }

    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        Assertions.isDefined(b, MessageFormat.format(ERROR_PARAMETERS_NULL, "b"));

        Byte[] objectB = new Byte[b.length];
        for (int i = 0; i < b.length; i++) {
            objectB[i] = new Byte(b[i]);
        }
        int read = 0;
        // the invocation handler of the proxy objects puts the
        // communication exception into an undeclared throwable exception,
        // because it is not part of the invoked method's signature
        try {
            byte[] buffer = (byte[]) fileService.read(remoteInputStreamUUID, new Integer(len));

            if (buffer.length > 0) {
                System.arraycopy(buffer, 0, b, off, buffer.length);
                read = buffer.length;
            } else {
                read = MINUS_ONE;
            }
        } catch (UndeclaredThrowableException e) {
            throw new IOException(e.getUndeclaredThrowable());
        }

        return read;
    }

    @Override
    public int read() throws IOException {
        try {
            return fileService.read(remoteInputStreamUUID);
        } catch (UndeclaredThrowableException e) {
            throw new IOException(e.getUndeclaredThrowable());
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return fileService.skip(remoteInputStreamUUID, n);
        } catch (UndeclaredThrowableException e) {
            throw new IOException(e.getUndeclaredThrowable());
        }
    }

    @Override
    public void close() throws IOException {
        try {
            fileService.close(remoteInputStreamUUID);
        } catch (UndeclaredThrowableException e) {
            throw new IOException(e.getUndeclaredThrowable());
        }
    }

}
