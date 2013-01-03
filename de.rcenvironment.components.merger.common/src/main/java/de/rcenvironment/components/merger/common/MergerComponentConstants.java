/*
 * Copyright (C) 2010-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
package de.rcenvironment.components.merger.common;
/**
 * MergerComponentConstants class.
 *
 * @author Sascha Zur
 */
public final class MergerComponentConstants {
    /**Constant. */
    public static final String INPUT_DATATYPE = "dataType"; 
   
    /**Constant. */
    public static final String[] INPUT_DATATYPE_LIST = {"Float", "Integer" , "String", "Serializable", "File Reference", "Array"};
    /**Constant. */
    public static final String INPUT_DATATYPE_DEFAULT = INPUT_DATATYPE_LIST[0]; 
    /**Constant. */
    public static final String INPUT_COUNT = "inputCount"; 
    /**Constant. */
    public static final String OUTPUT_NAME = "Merged"; 
    
    private MergerComponentConstants(){
        
    }

}
