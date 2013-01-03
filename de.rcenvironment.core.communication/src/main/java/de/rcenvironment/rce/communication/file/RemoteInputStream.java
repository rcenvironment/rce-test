/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.text.MessageFormat;

import de.rcenvironment.commons.Assertions;
import de.rcenvironment.rce.authentication.User;
import de.rcenvironment.rce.communication.CommunicationException;
import de.rcenvironment.rce.communication.file.internal.RemoteFileConnectionSupport;
import de.rcenvironment.rce.communication.file.spi.RemoteFileConnection;

/**
 * This class provides access to remote file by using a specified {@link RemoteFileConnection}
 * implementation got from the supportive class {@link RemoteFileConnectionSupport}.
 * 
 * This class is not meant to be thread safe!
 * 
 * @author Heinrich Wendel
 * @author Doreen Seider
 */
public class RemoteInputStream extends InputStream implements Serializable {

    private static final long serialVersionUID = 8592687526532347895L;

    private static final String ERROR_PARAMETERS_NULL = "The parameter \"{0}\" must not be null.";

    private final RemoteFileConnection remoteFileConnection;

    /**
     * Creates a new {@link RemoteInputStream} of a remote file.
     * 
     * @param uri URI pointing to remote file. (rce://host:platformNo/dataReferenceUUID/revision or
     *        file://host:platformNo/absolutePathToFile)
     * @throws IOException if the file could not be accessed remotely.
     */
    public RemoteInputStream(User cert, URI uri) throws IOException {

        Assertions.isDefined(uri, MessageFormat.format(ERROR_PARAMETERS_NULL, "uri"));
        Assertions.isDefined(uri, MessageFormat.format(ERROR_PARAMETERS_NULL, "certificate"));

        try {
            remoteFileConnection = RemoteFileConnectionSupport.getRemoteFileConnection(cert, uri);
        } catch (CommunicationException e) {
            throw new IOException(e);
        }

    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return remoteFileConnection.read(b, off, len);
    }

    @Override
    public int read() throws IOException {
        return remoteFileConnection.read();

    }

    @Override
    public long skip(long n) throws IOException {
        return remoteFileConnection.skip(n);
    }

    @Override
    public void close() throws IOException {
        remoteFileConnection.close();
    }

}
