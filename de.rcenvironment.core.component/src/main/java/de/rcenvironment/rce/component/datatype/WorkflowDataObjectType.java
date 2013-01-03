/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.component.datatype;



/**
 * Representing a type of the workflow data type.
 *
 * @author Markus Kunde
 */
public enum WorkflowDataObjectType {

    /**
     * Anything that can be represented by a char sequence.
     */
    ShortText,
    
    /**
     * Integer or floating point number.
     */
    Number,
    
    /**
     * True or false.
     */
    Logic,
    
    /**
     * Date.
     */
    Date,

    /**
     * Empty type.
     */
    Empty,
    
    /**
     * A file reference.
     */
    File,
    
    /**
     * A 2-dimensional grid containing native types.
     */
    Table,
    
    /**
     * A table constrained to Number cells.
     */
    Matrix,
    
    /**
     * A matrix constrained to Number cells.
     */
    Vector,
    
    /**
     * Represents a list or subtree of Files, with sub-directories represented by 
     * relative paths inside the individual filenames; may contain entries without 
     * a data management reference.
     */
    Directory,
    
    /**
     * Hierarchical data composed of nested maps and arrays; similar to JSON.
     */
    StructuredData;
    
    
    
    @Override 
    public String toString() {
        //only capitalize the first letter
        String s = super.toString();
        return s.substring(0, 1) + s.substring(1).toLowerCase();
    }
    
    public static String[] getTypeNames() {
        return new String[] { 
            WorkflowDataObjectType.ShortText.toString(), 
            WorkflowDataObjectType.Number.toString(),
            WorkflowDataObjectType.Logic.toString(),
            WorkflowDataObjectType.Date.toString(),
            WorkflowDataObjectType.Empty.toString(),
            WorkflowDataObjectType.File.toString(),
            WorkflowDataObjectType.Table.toString(),
            WorkflowDataObjectType.Matrix.toString(),
            WorkflowDataObjectType.Vector.toString(),
            WorkflowDataObjectType.Directory.toString(),
            WorkflowDataObjectType.StructuredData.toString()};
    }
    
    /**
     * Gives back the enum value of a string.
     * 
     * @param s string representation or class name to convert to enum
     * @return enum value or null if not matching
     */
    public static WorkflowDataObjectType toEnum(final String s) {
        WorkflowDataObjectType resType = null;
        if (s.equals(WorkflowDataObjectType.ShortText.toString()) || s.equals(ShortText.class.getName())) {
            resType = ShortText;
        } else if (s.equals(WorkflowDataObjectType.Number.toString()) || s.equals(Number.class.getName())) {
            resType = Number;
        } else if (s.equals(WorkflowDataObjectType.Logic.toString()) || s.equals(Logic.class.getName())) {
            resType = Logic;
        } else if (s.equals(WorkflowDataObjectType.Date.toString()) || s.equals(Date.class.getName())) {
            resType = Date;
        } else if (s.equals(WorkflowDataObjectType.Empty.toString()) || s.equals(Empty.class.getName())) {
            resType = Empty;
        } else if (s.equals(WorkflowDataObjectType.File.toString()) || s.equals(File.class.getName())) {
            resType = File;
        } else if (s.equals(WorkflowDataObjectType.Table.toString()) || s.equals(Table.class.getName())) {
            resType = Table;
        } else if (s.equals(WorkflowDataObjectType.Matrix.toString()) || s.equals(Matrix.class.getName())) {
            resType = Matrix;
        } else if (s.equals(WorkflowDataObjectType.Vector.toString()) || s.equals(Vector.class.getName())) {
            resType = Vector;
        } else if (s.equals(WorkflowDataObjectType.Directory.toString()) || s.equals(Directory.class.getName())) {
            resType = Directory;
        }  else if (s.equals(WorkflowDataObjectType.StructuredData.toString()) || s.equals(StructuredData.class.getName())) {
            resType = StructuredData;
        }
        
        return resType;
    }
    
    /**
     * Returns java class names of data types.
     * 
     * @param dt specific DataTypes
     * @return java class name
     */
    public static String toClassName(WorkflowDataObjectType dt) {
        String name = null;
        switch(dt) {
        case ShortText:
            name = ShortText.class.getName();
            break;
        case Number:
            name = Number.class.getName();
            break;
        case Logic:
            name = Logic.class.getName();
            break;
        case Date:
            name = Date.class.getName();
            break;
        case Empty:
            name = Empty.class.getName();
            break;
        case File:
            name = File.class.getName();
            break;
        case Table:
            name =  Table.class.getName();
            break;
        case Matrix:
            name = Matrix.class.getName();
            break;
        case Vector:
            name = Vector.class.getName();
            break;
        case Directory:
            name = Directory.class.getName();
            break;            
        case StructuredData:
            name = StructuredData.class.getName();
            break;
        default:
            break;
        }
        
        return name;
    }
}
