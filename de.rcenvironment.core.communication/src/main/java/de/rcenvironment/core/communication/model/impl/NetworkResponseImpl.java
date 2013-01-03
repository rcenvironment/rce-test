/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.communication.model.impl;

import java.io.Serializable;
import java.util.Map;

import de.rcenvironment.core.communication.model.NetworkResponse;
import de.rcenvironment.core.communication.model.internal.AbstractNetworkMessage;
import de.rcenvironment.core.communication.utils.SerializationException;

/**
 * Implementation of a transport-independent network response. Currently, such responses are
 * exclusively sent in response to received {@link NetworkRequestImpl}s.
 * 
 * @author Robert Mischke
 */
public class NetworkResponseImpl extends AbstractNetworkMessage implements NetworkResponse, Serializable {

    /**
     * Response-specific metadata key for the result code.
     */
    public static final String METADATA_KEY_RESULT_CODE = "response.resultCode";

    // TODO made this class Serializable for quick prototyping; rework so this is not used anymore
    private static final long serialVersionUID = 5984970957378933267L;

    /**
     * Creates an instance with the given body and meta data.
     * 
     * @param body the response body to use
     * @param metaData the meta data to use
     */
    public NetworkResponseImpl(byte[] body, Map<String, String> metaData) {
        super(metaData);
        setContentBytes(body);
    }

    /**
     * Creates an instance with the given body and meta data.
     * 
     * @param body the response body to use
     * @param metaData the meta data to use
     * @throws SerializationException on serialization failure
     */
    @Deprecated
    public NetworkResponseImpl(Serializable body, Map<String, String> metaData) throws SerializationException {
        super(metaData);
        setContent(body);
    }

    /**
     * Creates an instance with empty metadata, except for the given request id.
     * 
     * @param body the response body
     * @param requestId the request id to set
     * @param resultCode the result code to set
     */
    public NetworkResponseImpl(byte[] body, String requestId, int resultCode) {
        super();
        setContentBytes(body);
        setRequestId(requestId);
        setResultCode(resultCode);
    }

    /**
     * Creates an instance with empty metadata, except for the given request id.
     * 
     * @param body the response body
     * @param requestId the request id to set
     * @param resultCode the result code to set
     * @throws SerializationException on serialization failure
     */
    @Deprecated
    public NetworkResponseImpl(Serializable body, String requestId, int resultCode) throws SerializationException {
        super();
        setContent(body);
        setRequestId(requestId);
        setResultCode(resultCode);
    }

    @Override
    public boolean isSuccess() {
        return getResultCode() == NetworkResponse.RESULT_CODE_SUCCESS;
    }

    @Override
    public int getResultCode() {
        try {
            return Integer.parseInt(metaDataWrapper.getValue(METADATA_KEY_RESULT_CODE));
        } catch (NumberFormatException e) {
            return NetworkResponse.RESULT_CODE_UNDEFINED;
        }
    }

    private void setResultCode(int code) {
        metaDataWrapper.setValue(METADATA_KEY_RESULT_CODE, Integer.toString(code));
    }

}
