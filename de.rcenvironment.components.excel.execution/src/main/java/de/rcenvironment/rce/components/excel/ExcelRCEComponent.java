/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.excel;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Deque;
import java.util.Map;

import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.DefaultComponent;
import de.rcenvironment.rce.component.datamanagement.stateful.SimpleComponentDataManagementService;
import de.rcenvironment.rce.component.datamanagement.stateful.StatefulComponentDataManagementService;
import de.rcenvironment.rce.component.endpoint.Input;
import de.rcenvironment.rce.component.endpoint.Output;
import de.rcenvironment.rce.components.excel.commons.ExcelComponentConstants;
import de.rcenvironment.rce.components.excel.commons.ExcelUtils;
import de.rcenvironment.rce.components.excel.commons.ExcelService;
import de.rcenvironment.rce.notification.DistributedNotificationService;


/**
 * Excel component parent (abstract) class for handling RCE specific things.
 *
 * @author Markus Kunde
 */
public abstract class ExcelRCEComponent extends DefaultComponent {

    protected static DistributedNotificationService notificationService;

    protected static ExcelService excelService;

    private static final int NOTIFICATION_BUFFER = 1000;
    
    /** The data management service. */
    protected StatefulComponentDataManagementService dataManagementService;
    
    /*
     * 
     * Service bindings.
     * 
     */
    
    protected void bindExcelService(ExcelService newExcelService) {
        excelService = newExcelService;
    }
    
    protected void unbindExcelService(ExcelService oldExcelService) {
        // nothing to do here, but if this unbind method is missing, OSGi DS failed when disposing component
        // (seems to be a DS bug)
        //excelService.closeExcel();
    }
    
    protected void bindDistributedNotificationService(DistributedNotificationService newNotificationService) {
        notificationService = newNotificationService;
    }
    
    protected void unbindDistributedNotificationService(DistributedNotificationService oldNotificationService) {
        /* nothing to do here, this unbind method is only needed, 
         * because DS is throwing an exception when disposing otherwise. properly a bug
         */
    }
    
    
    
    
    /**
     * Executing one process step in Excel component.
     * 
     * @param newInput {@link #runStep(Input, Map) newInput}
     * @param inputValues {@link #runStep(Input, Map) inputValues}
     * @param ignoreInputs true if input channel values should be ignored
     * @return true if successful
     * @throws ComponentException {@link #runStep(Input, Map) ComponentException}
     */
    protected abstract boolean executingOneStep(Input newInput, Map<String, Deque<Input>> inputValues, final boolean ignoreInputs) 
        throws ComponentException;
    
    
    
    
    @Override
    public void onPrepare(ComponentInstanceInformation information) throws ComponentException {
        super.onPrepare(information);
        
        dataManagementService = new SimpleComponentDataManagementService(information);
        
        File originExcelFile = ExcelUtils.getAbsoluteFile(getConfigurationValue(ExcelComponentConstants.XL_FILENAME, String.class));
        if (originExcelFile == null) {
            throw new ComponentException("Cannot prepare Excel component. Maybe filename/path is wrong?");
        }
        
        notificationService.setBufferSize(instInformation.getIdentifier() + ExcelComponentConstants.NOTIFICATION_SUFFIX, 
            NOTIFICATION_BUFFER);
    }
    
    
    
    
    
    @Override
    public boolean runInitial(final boolean inputsConnected) throws ComponentException {       
        boolean returnCode = false; //false if Component is FINISHED, else true
        if (inputsConnected) {
            returnCode = true;
        }
        
        boolean isDriver = getConfigurationValue(ExcelComponentConstants.DRIVER, Boolean.class).booleanValue();
        if (isDriver || !inputsConnected) {
            executingOneStep(null, null, true);
        }
                      
        return returnCode;
    }
    
    
    
    
    @Override
    public boolean runStep(Input newInput, Map<String, Deque<Input>> inputValues) throws ComponentException {
        boolean returnCode = true; //false if Component is FINISHED, else true
        
        executingOneStep(newInput, inputValues, false);       
        
        return returnCode;
    }
    
    
    /**
     * Returns metadata of a concrete output in specified type.
     * 
     * @param output concrete output
     * @param key key of metadata
     * @param clazz class type of metadata
     * @return metadata of specified type or null if not successful
     * @throws ComponentException thrown if reading metadata of output channel has gone wrong
     */
    protected <T extends Serializable> T getMetaData(final Output output, final String key, Class<T> clazz) throws ComponentException {
        T returnValue = null;
        String loggerMessage = "Cannot read metadata of output channel " + output.getName() +  "."
            + "Please try to recreate/reconfigure the output channel.";
        
        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            for (Constructor<?> c : constructors) {
                if (c.getParameterTypes().length == 0) {
                    returnValue = clazz.newInstance();
                    break;
                }
            }
        } catch (InstantiationException e) {
            throw new ComponentException(loggerMessage, e);
        } catch (IllegalAccessException e) {
            throw new ComponentException(loggerMessage, e);
        }
        Map<String, Serializable> metaData = instInformation.getOutputMetaData(output.getName());
       
        Serializable serial = metaData.get(key);
        if (serial != null && clazz.isInstance(serial)) {
            returnValue = clazz.cast(serial);
        } else if (!(serial == null && returnValue != null)) {
            throw new ComponentException(loggerMessage);
        }
        return returnValue;
    }
    
    
    
    
    
    /**
     * Returns metadata of a concrete input in specified type.
     * 
     * @param input concrete input
     * @param key key of metadata
     * @param clazz class type of metadata
     * @return metadata of specified type or null if not successful
     * @throws ComponentException thrown if reading metadata of input channel has gone wrong
     */
    protected <T extends Serializable> T getMetaData(final Input input, final String key, Class<T> clazz) throws ComponentException {
        T returnValue = null;
        String loggerMessage = "Cannot read metadata of input channel " + input.getName() +  "."
            + "Please try to recreate/reconfigure the input channel.";
        
        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            for (Constructor<?> c : constructors) {
                if (c.getParameterTypes().length == 0) {
                    returnValue = clazz.newInstance();
                    break;
                }
            }
        } catch (InstantiationException e) {
            throw new ComponentException(loggerMessage, e);
        } catch (IllegalAccessException e) {
            throw new ComponentException(loggerMessage, e);
        }
        Map<String, Serializable> metaData = instInformation.getInputMetaData(input.getName());
       
        Serializable serial = metaData.get(key);
        if (serial != null && clazz.isInstance(serial)) {
            returnValue = clazz.cast(serial);
        } else if (!(serial == null && returnValue != null)) {
            throw new ComponentException(loggerMessage);
        }
        return returnValue;
    }
    
    
    
    
    
    
    /**
     * Returns concrete configuration value in specified type.
     * 
     * @param key key of configuration value
     * @param clazz class type of configuration value
     * @return configuration value of specified type or null if not successful
     * @throws ComponentException thrown if reading configuration value has gone wrong
     */
    protected <T extends Serializable> T getConfigurationValue(final String key, Class<T> clazz) throws ComponentException {
        T returnValue = null;
        String loggerMessage = "Cannot read configuration value. Is configuration of component correct?";

        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            for (Constructor<?> c : constructors) {
                if (c.getParameterTypes().length == 0) {
                    returnValue = clazz.newInstance();
                    break;
                }
            }
        } catch (InstantiationException e) {
            throw new ComponentException(loggerMessage, e);
        } catch (IllegalAccessException e) {
            throw new ComponentException(loggerMessage, e);
        }
        Serializable serial = instInformation.getConfigurationValue(key);
        
        if (serial != null && clazz.isInstance(serial)) {
            returnValue = clazz.cast(serial);
        } else if (!(serial == null && returnValue != null)) {
            throw new ComponentException(loggerMessage);
        }
        return returnValue;
    }
}
