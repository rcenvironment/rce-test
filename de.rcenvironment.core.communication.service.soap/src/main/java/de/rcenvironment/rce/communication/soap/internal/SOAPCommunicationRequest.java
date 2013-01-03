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

import de.rcenvironment.rce.communication.service.ServiceCallRequest;

/**
 * Wrapper class to transfer the {@link ServiceCallRequest} via SOAP.
 * 
 * @author Heinrich Wendel
 * @author Tobias Menden
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SOAPCommunicationRequest {

    /**
     * The data type to transfer via the wire in a byte array.
     */
    @XmlMimeType("application/octet-stream")
    private DataHandler myCommunicationRequest;
    
    /**
     * Empty default constructor for serialization.
     */
    public SOAPCommunicationRequest() {

    }
    
    /**
     * Constructor that takes all arguments.
     * 
     * @param request The {@link ServiceCallRequest} to transfer.
     * @throws SOAPException Thrown if {@link SOAPCommunicationRequest} gets an IOException, because
     *     could not wrote the byte array.
     */
    public SOAPCommunicationRequest(ServiceCallRequest request) throws SOAPException {
        setServiceCallRequest(request);
    }

    /**
     * Returns the {@link ServiceCallRequest} transferred from a byte array.
     * 
     * @return Returns the {@link ServiceCallRequest} transferred from a byte array.
     * @throws SOAPException  Thrown if {@link SOAPCommunicationRequest} couldn't read the byte array.
     */
    public ServiceCallRequest getServiceCallRequest() throws SOAPException {
        try {
            return (ServiceCallRequest) new ObjectInputStream(myCommunicationRequest.getInputStream()).readObject();
        } catch (IOException e) {
            throw new SOAPException("Error during deserialization of object", e);
        } catch (ClassNotFoundException e) {
            throw new SOAPException("Error during deserialization of object", e);
        }
    }
    
    /**
     * Converts the {@link ServiceCallRequest} to a byte array.
     * 
     * @param request The request to convert.
     * @throws CommunicationException Thrown if {@link SOAPCommunicationRequest} gets an IOException, because
     *      could not wrote the byte array.
     */
    private void setServiceCallRequest(ServiceCallRequest request) throws SOAPException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(request);
            oos.close();
        } catch (IOException e) {
            throw new SOAPException("Error during serialization of object", e);
        }
        ByteArrayDataSource bads = new ByteArrayDataSource(baos.toByteArray(), "application/octet-stream");
        myCommunicationRequest = new DataHandler(bads);
    }
}
