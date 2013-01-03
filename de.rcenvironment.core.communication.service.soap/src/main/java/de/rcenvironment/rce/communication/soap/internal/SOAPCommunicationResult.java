/*
 * Copyright (C) 2006-2010 DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.communication.soap.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.soap.SOAPException;

import de.rcenvironment.rce.communication.service.ServiceCallResult;

/**
 * Wrapper class to transfer the {@link ServiceCallResult} via SOAP.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SOAPCommunicationResult {

    /**
     * The data type to transfer via the wire in a byte array.
     */
    @XmlMimeType("application/octet-stream")
    private DataHandler myCommunicationResult;

    /**
     * Empty default constructor for serialization.
     */
    public SOAPCommunicationResult() {}

    /**
     * Constructor that takes a {@link ServiceCallResult}.
     * 
     * @param result The {@link ServiceCallResult} to transfer.
     * @throws CommunicationException Thrown if {@link SOAPCommunicationRequest} gets an IOException, because
     *      could not wrote the byte array.
     */
    public SOAPCommunicationResult(ServiceCallResult result) throws SOAPException {
        setServiceCallResult(result);
    }
    
    /**
     * Returns the {@link ServiceCallResult} transferred from a byte array.
     * 
     * @return Returns the {@link ServiceCallResult} transferred from a byte array
     * @throws SOAPException Thrown if {@link SOAPCommunicationRequest} couldn't read the byte array.
     */
    public ServiceCallResult getServiceCallResult() throws SOAPException {
        try {
            return (ServiceCallResult) new ObjectInputStream(myCommunicationResult.getInputStream()).readObject();
        } catch (IOException e) {
            throw new SOAPException("Error during deserialization of object", e);
        } catch (ClassNotFoundException e) {
            throw new SOAPException("Error during deserialization of object", e);
        }
    }
    
    /**
     * Converts the {@link ServiceCallResult} to a byte array.
     * 
     * @param result The result to convert.
     * @throws SOAPException Thrown if {@link SOAPCommunicationRequest} gets an IOException, because
     *      could not wrote the byte array.
     */
    private void setServiceCallResult(ServiceCallResult result) throws SOAPException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(result);
            oos.close();
        } catch (IOException e) {
            throw new SOAPException("Error during serialization of object", e);
        }

        ByteArrayDataSource bads = new ByteArrayDataSource(baos.toByteArray(), "application/octet-stream");
        myCommunicationResult = new DataHandler(bads);
    }
}
