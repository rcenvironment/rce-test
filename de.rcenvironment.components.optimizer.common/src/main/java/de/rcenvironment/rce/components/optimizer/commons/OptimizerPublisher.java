/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.optimizer.commons;

import java.io.Serializable;


/**
 * Responsible for announcing study values.
 * @author Christian Weiss
 */
public interface OptimizerPublisher extends Serializable {

    /** Default number of data sets which are buffered by the {@link NotificationService}. */
    int BUFFER_SIZE = 500;
    
    /**
     * @return the adequate {@link ResultSet}.
     */
    ResultSet getStudy();

    /**
     * @param bufferSize the number of {@link StudyDataset}s to store.
     */
    void setBufferSize(int bufferSize);

    /**
     * @param dataset adds a new {@link StudyDataset}, i.e. announce and store.
     */
    void add(OptimizerResultSet dataset);
}
