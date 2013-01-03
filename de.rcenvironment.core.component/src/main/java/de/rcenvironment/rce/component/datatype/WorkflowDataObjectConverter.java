/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;


/**
 * Converter for workflow data objects.
 *
 * @author Markus Kunde
 */
public final class WorkflowDataObjectConverter {

    private WorkflowDataObjectConverter() {};
    
    /**
     * Converts WorkflowDataObjectAtomic objects into other WorkflowDataObjectAtomic objects.
     * Central set for converting rules.
     * 
     * @param <T> WorkflowDataObjectAtomic class
     * @param originDataObj origin data object
     * @param clazz class of target data object
     * @return target data object with declared class or null 
     */
    @SuppressWarnings("unchecked")
    public static <T extends WorkflowDataObjectAtomic> T convertWorkflowDataObjectAtomic(
        WorkflowDataObjectAtomic originDataObj, Class<T> clazz) {
        T returnValue = null;    
        
        try {
            switch (WorkflowDataObjectType.toEnum(clazz.getName())) {
            case ShortText: //Target
                switch(((WorkflowDataObjectAtomic) originDataObj).getType()) {
                case ShortText:
                    returnValue = (T) originDataObj;
                    break;
                case Number:
                    returnValue = (T) new ShortTextImpl(String.valueOf(((Number) originDataObj).getDoubleValue()));
                    break;
                case Logic:
                    returnValue = (T) new ShortTextImpl(String.valueOf(((Logic) originDataObj).getValue()));
                    break;
                case Date:
                    returnValue = (T) new ShortTextImpl(((Date) originDataObj).getValue());
                    break;
                case Empty:
                    returnValue = (T) new ShortTextImpl("");
                    break;
                default:
                    returnValue = null;
                    break;
                }
                break;
            case Number: //Target
                switch(((WorkflowDataObjectAtomic) originDataObj).getType()) {
                case ShortText:
                    returnValue = (T) new NumberImpl(Double.valueOf(((ShortText) originDataObj).getValue()));
                    break;
                case Number:
                    returnValue = (T) originDataObj;
                    break;
                case Logic:
                    returnValue = (T) new NumberImpl(Double.valueOf(String.valueOf(((Logic) originDataObj).getValue())));
                    break;
                case Date:
                    returnValue = (T) new NumberImpl(Double.valueOf(((Date) originDataObj).getValue()));
                    break;
                case Empty:
                    returnValue = null;
                    break;
                default:
                    returnValue = null;
                    break;
                }
                break;
            case Logic: //Target
                switch(((WorkflowDataObjectAtomic) originDataObj).getType()) {
                case ShortText:
                    returnValue = (T) new LogicImpl(Boolean.parseBoolean(((ShortText) originDataObj).getValue()));
                    break;
                case Number:
                    returnValue = null;
                    break;
                case Logic:
                    returnValue = (T) originDataObj;
                    break;
                case Date:
                    returnValue = null; 
                    break;
                case Empty:
                    returnValue = null;
                    break;
                default:
                    returnValue = null;
                    break;
                }
                break;
            case Date: //Target
                switch(((WorkflowDataObjectAtomic) originDataObj).getType()) {
                case ShortText:
                    returnValue = (T) new LogicImpl(Boolean.parseBoolean(((ShortText) originDataObj).getValue()));
                    break;
                case Number:
                    returnValue = (T) new DateImpl(String.valueOf(((Number) originDataObj).getDoubleValue()));
                    break;
                case Logic:
                    returnValue = null;
                    break;
                case Date:
                    returnValue = (T) originDataObj; 
                    break;
                case Empty:
                    returnValue = null;
                    break;
                default:
                    break;
                }
                break;
            case Empty: //Target
                returnValue = (T) new EmptyImpl();
                break;
            default:
                returnValue = null;
                break;
            }
        } catch (NumberFormatException e) {
            returnValue = null;
        }

        return returnValue;
    }
}
